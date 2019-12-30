package com.bookyrself.bookyrself.presenters

import android.util.Log
import com.bookyrself.bookyrself.data.contacts.ContactsRepoRxJava
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.serverModels.user.EventInviteInfo
import com.bookyrself.bookyrself.data.serverModels.user.User
import com.bookyrself.bookyrself.services.FirebaseService
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import com.bookyrself.bookyrself.views.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by benmedcalf on 3/9/18.
 */

class EventCreationPresenter(private val presenterListener: EventCreationPresenterListener) : BasePresenter {

    private val contactsRepoRxJava: ContactsRepoRxJava = MainActivity.contactsRepo
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    /**
     * Methods
     */
    override fun subscribe() {
        compositeDisposable.add(
                contactsRepoRxJava
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
                            if (event.users != null) {
                                createAndSendInvitations(event, eventCreationResponse.name!!)
                            }
                        }
                        .subscribe(
                                { eventCreationResponse -> eventCreationResponse.name?.let { presenterListener.eventCreated(it) } },
                                { throwable -> throwable.message?.let { presenterListener.presentError(it) } }
                        ))
    }

    fun updateEventAndInvites(event: EventDetail, eventId: String, originalInvitations: List<String>?) {
        compositeDisposable.add(
                FirebaseService.instance.updateEvent(event, eventId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { eventCreationResponse ->
                                    eventCreationResponse?.let {
                                        if (originalInvitations != null) {
                                            val updatedInvitations = ArrayList(event.users.keys)
                                            if (originalInvitations != updatedInvitations) {
                                                updateInvitations(originalInvitations, updatedInvitations, eventId)
                                            }
                                        } else if (eventCreationResponse.users != null) {
                                            createAndSendInvitations(event, eventId)
                                        }
                                        presenterListener.eventUpdated()
                                    }
                                },
                                { throwable -> throwable.message?.let { presenterListener.presentError(it) } }
                        ))
    }


    private fun createAndSendInvitations(event: EventDetail, eventId: String) {
        val inviteeEventInfo = EventInviteInfo()
        inviteeEventInfo.isHost = false
        inviteeEventInfo.isInviteAccepted = false
        inviteeEventInfo.isInviteRejected = false

        for (userId in event.users.keys) {
            Flowable.just<Disposable>(FirebaseService.instance
                    .addEventToUser(inviteeEventInfo, userId, eventId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe())
                    .subscribe()
        }
    }

    // Called by DatePickerDialogFragment, not the actual EventCreationActivity
    fun setDate(date: String) {
        presenterListener.dateSelectedFromDatePickerDialog(date)
    }

    private fun updateInvitations(initialInvitations: List<String>, updatedInvitations: List<String>, eventId: String) {
        val newInvitedUsers = updatedInvitations.filterNot { initialInvitations.contains(it) }
        val uninvitedUsers = initialInvitations.filterNot { updatedInvitations.contains(it) }
        removeInvitationsFromExistingInvitedUsers(uninvitedUsers, eventId)
        sendInvitationsToNewInvitedUsers(newInvitedUsers, eventId)
    }

    private fun removeInvitationsFromExistingInvitedUsers(uninvitedUserIds: List<String>, eventId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            uninvitedUserIds.forEach { userIdOfTheUninvited ->
                val deleteResponse = FirebaseServiceCoroutines.instance.removeEventFromUser(userIdOfTheUninvited, eventId)
                if (deleteResponse.isSuccessful) {
                    Log.i("EventCreationPresenter", "Successfully removed eventId $eventId from userId $userIdOfTheUninvited")
                } else {
                    Log.e("EventCreationPresenter", "Error removing eventId $eventId from userId $userIdOfTheUninvited")
                }
            }
        }
    }

    private fun sendInvitationsToNewInvitedUsers(userIdsOfTheInvited: List<String>, eventId: String) {
        val inviteInfo = EventInviteInfo()
        inviteInfo.isHost = false
        inviteInfo.isInviteAccepted = false
        inviteInfo.isInviteRejected = false

        CoroutineScope(Dispatchers.IO).launch {
            userIdsOfTheInvited.forEach { userId ->
                val invitationResponse = FirebaseServiceCoroutines.instance.addEventToUser(inviteInfo, userId, eventId)
                if (invitationResponse.isSuccessful) {
                    Log.i("EventCreationPresenter", "Successfully added eventId $eventId from userId $userId")
                } else {
                    Log.e("EventCreationPresenter", "Error adding eventId $eventId from userId $userId")
                }
            }
        }
    }


    /**
     * Contract / Listener
     */
    interface EventCreationPresenterListener {

        fun contactReturned(contact: User, userId: String)

        fun eventCreated(eventId: String)

        fun eventUpdated()

        fun dateSelectedFromDatePickerDialog(date: String)

        fun presentError(message: String)
    }
}
