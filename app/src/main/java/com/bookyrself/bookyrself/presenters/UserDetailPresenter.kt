package com.bookyrself.bookyrself.presenters

import android.util.Log
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ResponseModels.User.EventInviteInfo
import com.bookyrself.bookyrself.data.ResponseModels.User.User
import com.bookyrself.bookyrself.services.FirebaseService
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by benmedcalf on 1/13/18.
 */

class UserDetailPresenter
/**
 * Constructor
 */
(private val userId: String, private val listener: UserDetailPresenterListener) : BasePresenter {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    /**
     * Methods
     */
    private fun loadUserInfoWithEvents(userId: String) {

        compositeDisposable.add(
                FirebaseService.getAPI().getUserDetails(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError { throwable -> throwable.message?.let { listener.presentError(it) } }
                        .flatMap<Map.Entry<String, EventInviteInfo>> { user ->

                            // Notify view that user details have been returned
                            listener.displayUserInfo(user, userId)

                            Flowable.fromIterable<Map.Entry<String, EventInviteInfo>>(user.events.entries)
                        }

                        // Map the eventInviteInfos into eventDetails
                        .map<Disposable> { stringEventInviteInfoEntry ->
                            FirebaseService
                                    .getAPI()
                                    .getEventData(stringEventInviteInfoEntry.key)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ eventDetail -> listener.displayUserEvent(eventDetail, stringEventInviteInfoEntry.key) },
                                            { throwable -> throwable.message?.let { listener.presentError(it) } })
                        }
                        .subscribe(
                                { disposable -> },
                                { throwable -> Log.e(javaClass.name, throwable.message) }))
    }

    fun addContactToUser(contactId: String, userId: String) {

        val request = HashMap<String, Boolean>()
        request[contactId] = true

        Flowable.just<Disposable>(FirebaseService.getAPI()
                .addContactToUser(request, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { listener.presentSuccess("Added contact!") },
                        { throwable -> throwable.message?.let { listener.presentError(it) } }))
                .subscribe()
    }


    override fun subscribe() {
        loadUserInfoWithEvents(userId)
    }

    override fun unsubscribe() {
        compositeDisposable.clear()
    }


    /**
     * Contract / Listener
     */
    interface UserDetailPresenterListener {
        fun displayUserInfo(userInfo: User, userId: String)

        fun displayUserEvent(event: EventDetail, eventId: String)

        fun displayLoadingState()

        fun presentError(message: String)

        fun presentSuccess(message: String)

    }
}
