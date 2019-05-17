package com.bookyrself.bookyrself.presenters

import android.content.Context

import com.bookyrself.bookyrself.data.Events.EventsRepository
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.views.MainActivity
import com.google.firebase.auth.FirebaseAuth

import java.util.NoSuchElementException

import io.reactivex.disposables.CompositeDisposable

/**
 * Created by benmedcalf on 3/11/18.
 */

class EventsFragmentPresenter(private val presenterListener: EventsPresenterListener, context: Context) : BasePresenter {
    private val eventsRepo: EventsRepository = MainActivity.getEventsRepo(context)
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var userId: String? = null


    /**
     * Methods
     */
    private fun loadUsersEventInfo() {

        compositeDisposable
                .add(eventsRepo.getAllEvents(userId)
                        .subscribe(
                                //onNext
                                { stringEventDetailEntry ->
                                    presenterListener.eventDetailReturned(
                                            stringEventDetailEntry.value,
                                            stringEventDetailEntry.key)
                                },

                                //onError
                                { throwable ->
                                    if (throwable is NoSuchElementException) {
                                        presenterListener.noEventDetailsReturned()
                                    } else {
                                        throwable.message?.let { presenterListener.presentError(it) }
                                    }
                                }))
    }

    override fun subscribe() {
        if (FirebaseAuth.getInstance().uid != null) {
            userId = FirebaseAuth.getInstance().uid
            loadUsersEventInfo()
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
    interface EventsPresenterListener : BasePresenterListener {

        fun eventDetailReturned(event: EventDetail, eventId: String)

        fun noEventDetailsReturned()

        fun presentError(error: String)
    }

}
