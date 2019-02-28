package com.bookyrself.bookyrself.presenters;

import android.util.Log;

import com.bookyrself.bookyrself.data.Events.EventsRepo;
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.views.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.NoSuchElementException;

import io.reactivex.disposables.CompositeDisposable;


public class EventInvitesFragmentPresenter implements BasePresenter {

    private final EventInvitesViewListener listener;
    private EventsRepo eventsRepo;
    private CompositeDisposable compositeDisposable;
    private String userId;

    public EventInvitesFragmentPresenter(EventInvitesViewListener listener) {
        this.userId = FirebaseAuth.getInstance().getUid();
        this.listener = listener;
        this.compositeDisposable = new CompositeDisposable();
        this.eventsRepo = MainActivity.getEventsRepo();
    }

    public void loadPendingInvites() {
        compositeDisposable
                .add(eventsRepo.getEventsWithPendingInvites(userId)
                        .subscribe(
                                //onNext
                                stringEventDetailPair -> listener.eventPendingInvitationResponseReturned(
                                        stringEventDetailPair.first,
                                        stringEventDetailPair.second),

                                //onError
                                throwable -> {
                                    if (throwable instanceof NoSuchElementException) {
                                        listener.showEmptyStateForNoInvites();
                                    } else {
                                        listener.presentError(throwable.getMessage());
                                        Log.e("EventsInvitesFragment: ", throwable.getMessage(), throwable.fillInStackTrace());
                                    }
                                }));

    }


    public void acceptEventInvite(final String userId, final String eventId, EventDetail eventDetail) {
        compositeDisposable.add(
                eventsRepo.acceptEventInvite(userId, eventId)
                        .doOnNext(aBoolean -> listener.removeEventFromList(eventId, eventDetail))
                        .doOnError(throwable -> listener.presentError(throwable.getMessage()))
                        .subscribe());
    }

    public void rejectEventInvite(String userId, final String eventId, EventDetail eventDetail) {
        compositeDisposable.add(
                eventsRepo.rejectEventInvite(userId, eventId)
                        .doOnNext(aBoolean -> listener.removeEventFromList(eventId, eventDetail))
                        .doOnError(throwable -> listener.presentError(throwable.getMessage()))
                        .subscribe());
    }


    @Override
    public void subscribe() {
        loadPendingInvites();
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    /**
     * Contract / Listener
     */
    public interface EventInvitesViewListener {
        void eventPendingInvitationResponseReturned(String eventId, EventDetail event);

        void presentError(String message);

        void removeEventFromList(String eventId, EventDetail eventDetail);

        void showEmptyStateForNoInvites();
    }
}
