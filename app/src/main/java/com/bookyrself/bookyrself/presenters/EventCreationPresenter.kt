package com.bookyrself.bookyrself.presenters

import com.bookyrself.bookyrself.data.Contacts.ContactsRepository
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.User.EventInviteInfo
import com.bookyrself.bookyrself.data.ServerModels.User.User
import com.bookyrself.bookyrself.services.FirebaseService
import com.bookyrself.bookyrself.views.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by benmedcalf on 3/9/18.
 */

class EventCreationPresenter(private val presenterListener: EventCreationPresenterListener) : BasePresenter {

    private val contactsRepository: ContactsRepository = MainActivity.contactsRepo
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    /**
     * Methods
     */
    override fun subscribe() {
        compositeDisposable.add(
                contactsRepository
                        .getContactsForUser(FirebaseAuth.getInstance().uid!!)
                        .subscribe(
                                { stringUserEntry -> presenterListener.contactReturned(stringUserEntry.value, stringUserEntry.key) },
                                { throwable -> throwable.message?.let { presenterListener.presentError(it) } }))
    }

    override fun unsubscribe() {
        compositeDisposable.clear()
    }


    fun createEvent(event: EventDetail) {
        compositeDisposable.add(
                FirebaseService.instance.createEvent(event)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())

                        // Set the event to the Host's user
                        .doOnNext { eventCreationResponse ->
                            val hostEventInviteInfo = EventInviteInfo()
                            hostEventInviteInfo.isInviteAccepted = true
                            hostEventInviteInfo.isHost = true
                            hostEventInviteInfo.isInviteRejected = false

                            eventCreationResponse.name?.let { eventName ->
                                FirebaseAuth.getInstance().uid?.let { userId ->
                                    FirebaseService.instance
                                            .addEventToUser(hostEventInviteInfo,
                                                    userId,
                                                    eventName)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe()
                                }
                            }
                        }

                        // Set the event to the invitees
                        .doOnNext { eventCreationResponse ->
                            val inviteeEventInfo = EventInviteInfo()
                            inviteeEventInfo.isHost = false
                            inviteeEventInfo.isInviteAccepted = false
                            inviteeEventInfo.isInviteRejected = false

                            for (userId in event.users.keys) {
                                Flowable.just<Disposable>(FirebaseService.instance
                                        .addEventToUser(inviteeEventInfo, userId, eventCreationResponse.name!!)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe())
                            }
                        }
                        .subscribe(
                                { eventCreationResponse -> eventCreationResponse.name?.let { presenterListener.eventCreated(it) } },
                                { throwable -> throwable.message?.let { presenterListener.presentError(it) } }
                        ))
    }


    // Called by DatePickerDialogFragment, not the actual EventCreationActivity
    fun setDate(date: String) {
        presenterListener.dateSelectedFromDatePickerDialog(date)
    }


    /**
     * Contract / Listener
     */
    interface EventCreationPresenterListener {

        fun contactReturned(contact: User, userId: String)

        fun eventCreated(eventId: String)

        fun dateSelectedFromDatePickerDialog(date: String)

        fun presentError(message: String)
    }
}
