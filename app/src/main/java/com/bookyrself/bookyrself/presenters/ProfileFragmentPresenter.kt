package com.bookyrself.bookyrself.presenters

import android.content.Context
import android.util.Log

import com.bookyrself.bookyrself.data.Events.EventsRepository
import com.bookyrself.bookyrself.data.Profile.ProfileRepo
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.User.User
import com.bookyrself.bookyrself.services.FirebaseService
import com.bookyrself.bookyrself.views.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth

import java.util.NoSuchElementException

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ProfileFragmentPresenter
/**
 * Construction
 */
(private val listener: ProfilePresenterListener, context: Context) : BasePresenter {
    private val profileRepo: ProfileRepo
    private val compositeDisposable: CompositeDisposable
    private val eventsRepo: EventsRepository
    private var userId: String? = null

    init {
        this.eventsRepo = MainActivity.getEventsRepo(context)
        this.profileRepo = MainActivity.profileRepo
        this.compositeDisposable = CompositeDisposable()
    }

    /**
     * Methods
     */
    fun updateUser(user: User, userId: String) {
        compositeDisposable.add(
                FirebaseService.instance.updateUser(user, userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ listener.profileInfoReady(userId, user) },
                                { throwable -> throwable.message?.let { listener.presentError(it) } }))
    }

    private fun loadProfile() {
        compositeDisposable.add(

                profileRepo.getProfileInfo(userId!!).subscribe(
                        { user ->
                            // Notify view the profile is ready
                            listener.profileInfoReady(userId, user)
                        },
                        { throwable ->
                            if (throwable is NoSuchElementException) {
                                listener.presentError("We were unable to find your profile")
                            } else {
                                throwable.message?.let { listener.presentError(it) }
                            }
                        }))
    }

    private fun loadEventDetails() {
        compositeDisposable.add(eventsRepo.getAllEvents(userId!!)
                .subscribe(
                        // Success
                        { stringEventDetailEntry ->
                            listener.eventReady(
                                    stringEventDetailEntry.key, stringEventDetailEntry.value)
                        },

                        // Error
                        { throwable ->
                            if (throwable is NoSuchElementException) {
                                Log.e(javaClass.name, String.format("User %s has no events", userId))
                            } else {
                                throwable.message?.let { listener.presentError(it) }
                            }
                        }))
    }

    override fun subscribe() {
        if (FirebaseAuth.getInstance().uid != null) {
            userId = FirebaseAuth.getInstance().uid
            loadProfile()
            loadEventDetails()
        } else {
            listener.showSignedOutEmptyState()
        }
    }

    override fun unsubscribe() {
        compositeDisposable.clear()
    }


    fun markDateAsUnavailable(userId: String, date: String) {
        FirebaseService.instance.setDateUnavailableForUser(true, userId, date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }


    /**
     * PresenterListener Definition
     */
    interface ProfilePresenterListener : BasePresenterListener {

        fun profileInfoReady(userId: String?, user: User)

        fun eventReady(eventId: String, event: EventDetail)

        fun presentError(error: String)
    }
}
