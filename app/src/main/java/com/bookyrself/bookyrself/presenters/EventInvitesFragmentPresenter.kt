package com.bookyrself.bookyrself.presenters

import android.content.Context
import android.util.Log
import com.bookyrself.bookyrself.data.Events.EventsRepository
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.views.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.disposables.CompositeDisposable
import java.util.*


class EventInvitesFragmentPresenter(private val presenterListener: EventInvitesViewListener, context: Context) : BasePresenter {
    private val eventsRepo: EventsRepository
    private val compositeDisposable: CompositeDisposable
    var userId: String

    init {
        this.userId = FirebaseAuth.getInstance().uid.toString()
        this.compositeDisposable = CompositeDisposable()
        this.eventsRepo = MainActivity.getEventsRepo(context)
    }

    fun acceptEventInvite(userId: String, eventId: String, eventDetail: EventDetail) {
        compositeDisposable.add(
                eventsRepo.acceptEventInvite(userId, eventId)
                        .doOnNext { presenterListener.removeEventFromList(eventId, eventDetail) }
                        .doOnError { throwable -> throwable.message?.let { presenterListener.presentError(it) } }
                        .subscribe())
    }

    fun rejectEventInvite(userId: String, eventId: String, eventDetail: EventDetail) {
        compositeDisposable.add(
                eventsRepo.rejectEventInvite(userId, eventId)
                        .doOnNext { presenterListener.removeEventFromList(eventId, eventDetail) }
                        .doOnError { throwable -> throwable.message?.let { presenterListener.presentError(it) } }
                        .subscribe())
    }

    private fun loadPendingInvites() {

        compositeDisposable
                .add(eventsRepo.getEventsWithPendingInvites(userId)!!.subscribe(
                                //onNext
                                { stringEventDetailSimpleEntry ->
                                    presenterListener.eventPendingInvitationResponseReturned(
                                            stringEventDetailSimpleEntry.key,
                                            stringEventDetailSimpleEntry.value)
                                },

                                //onError
                                { throwable ->
                                    if (throwable is NoSuchElementException) {
                                        presenterListener.showEmptyStateForNoInvites()
                                    } else {
                                        throwable.message?.let { presenterListener.presentError(it) }
                                        Log.e("EventsInvitesFragment: ", throwable.message, throwable.fillInStackTrace())
                                    }
                                }))
    }


    override fun subscribe() {
        if (FirebaseAuth.getInstance().uid != null) {
            userId = FirebaseAuth.getInstance().uid!!
            loadPendingInvites()
        } else {
            presenterListener.showSignedOutEmptyState()
        }

    }

    override fun unsubscribe() {
        compositeDisposable.clear()
    }

    /**
     * PresenterListener Definition
     */
    interface EventInvitesViewListener : BasePresenterListener {
        fun eventPendingInvitationResponseReturned(eventId: String, event: EventDetail)

        fun presentError(message: String)

        fun removeEventFromList(eventId: String, eventDetail: EventDetail)

        fun showEmptyStateForNoInvites()
    }
}
