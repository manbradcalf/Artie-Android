package com.bookyrself.bookyrself.presenters

import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.MiniUser
import com.bookyrself.bookyrself.data.ServerModels.User.EventInviteInfo
import com.bookyrself.bookyrself.data.ServerModels.User.User
import com.bookyrself.bookyrself.services.FirebaseService
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by benmedcalf on 11/22/17.
 */

class EventDetailPresenter(private val listener: EventDetailPresenterListener, private val eventId: String) : BasePresenter {

    private val compositeDisposable = CompositeDisposable()

    /**
     * Methods
     */
    private fun getEventDetailData(eventId: String) {

        compositeDisposable.add(

                FirebaseService.getAPI()
                        .getEventData(eventId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext { listener.showEventData(it) }

                        // Get the userIds of invited users
                        .flatMap<kotlin.collections.Map.Entry<String, Boolean>> { eventDetail: EventDetail -> eventDetail.users.entries.toFlowable() }

                        // Get user details for each user
                        .map { stringBooleanEntry ->
                            Flowable.just<Disposable>(
                                    FirebaseService.getAPI().getUserDetails(stringBooleanEntry.key)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .firstOrError()
                                            .subscribe(
                                                    { user ->
                                                        // Show the view the minified user
                                                        val miniUser = minifyUserDetailsForEventDetailDisplay(stringBooleanEntry.key, user)
                                                        listener.showInvitedUser(AbstractMap.SimpleEntry(stringBooleanEntry.key, miniUser))
                                                    },
                                                    { throwable ->

                                                        // Present error
                                                        if (throwable is NoSuchElementException) {
                                                            listener.presentError(String.format("We were unable to find a user with id %s", stringBooleanEntry.key))
                                                        } else if (throwable.message != null) {
                                                            listener.presentError(throwable.message!!)
                                                        }
                                                        throwable.printStackTrace()
                                                    }
                                            ))
                        }
                        .subscribe(
                                { },
                                { throwable -> throwable.message?.let { listener.presentError(it) } }))
    }

    private fun minifyUserDetailsForEventDetailDisplay(userId: String, user: User): MiniUser {
        val miniUser = MiniUser()
        miniUser.citystate = user.citystate
        miniUser.url = user.url
        miniUser.username = user.username
        miniUser.userId = userId
        miniUser.attendingStatus = getAttendingStatus(user.events[eventId])
        return miniUser
    }

    private fun getAttendingStatus(eventInviteInfo: EventInviteInfo?): String {
        var status = "Invited"
        if (eventInviteInfo != null) {
            if (eventInviteInfo.isInviteRejected != null) {
                if (eventInviteInfo.isInviteRejected!!) {
                    status = "Not attending"
                }
            }

            if (eventInviteInfo.isInviteAccepted != null) {
                if (eventInviteInfo.isInviteAccepted!!) {
                    status = "Attending"
                }
            }
        }
        return status
    }

    override fun subscribe() {
        getEventDetailData(eventId)
    }

    override fun unsubscribe() {
        compositeDisposable.clear()
    }


    /**
     * Contract / Listener
     */
    interface EventDetailPresenterListener {
        fun showEventData(data: EventDetail)

        fun showInvitedUser(user: AbstractMap.SimpleEntry<String, MiniUser>)

        fun presentError(message: String)
    }
}
