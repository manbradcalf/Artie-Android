package com.bookyrself.bookyrself.presenters;

import android.content.Context;
import android.util.Log;

import com.bookyrself.bookyrself.data.Events.EventsRepo;
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.views.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.NoSuchElementException;

import io.reactivex.disposables.CompositeDisposable;


public class EventInvitesFragmentPresenter implements BasePresenter {

    private final EventInvitesViewListener presenterListener;
    private EventsRepo eventsRepo;
    private CompositeDisposable compositeDisposable;
    private String userId;

    public EventInvitesFragmentPresenter(EventInvitesViewListener listener, Context context) {
        this.userId = FirebaseAuth.getInstance().getUid();
        this.presenterListener = listener;
        this.compositeDisposable = new CompositeDisposable();
        this.eventsRepo = MainActivity.getEventsRepo(context);
    }


    public void acceptEventInvite(final String userId, final String eventId, EventDetail eventDetail) {
        compositeDisposable.add(
                eventsRepo.acceptEventInvite(userId, eventId)
                        .doOnNext(aBoolean -> presenterListener.removeEventFromList(eventId, eventDetail))
                        .doOnError(throwable -> presenterListener.presentError(throwable.getMessage()))
                        .subscribe());
    }

    public void rejectEventInvite(String userId, final String eventId, EventDetail eventDetail) {
        compositeDisposable.add(
                eventsRepo.rejectEventInvite(userId, eventId)
                        .doOnNext(aBoolean -> presenterListener.removeEventFromList(eventId, eventDetail))
                        .doOnError(throwable -> presenterListener.presentError(throwable.getMessage()))
                        .subscribe());
    }

    private void loadPendingInvites() {

            compositeDisposable
                    .add(eventsRepo.getEventsWithPendingInvites(userId)
                            .subscribe(
                                    //onNext
                                    stringEventDetailSimpleEntry -> presenterListener.eventPendingInvitationResponseReturned(
                                            stringEventDetailSimpleEntry.getKey(),
                                            stringEventDetailSimpleEntry.getValue()),

                                    //onError
                                    throwable -> {
                                        if (throwable instanceof NoSuchElementException) {
                                            presenterListener.showEmptyStateForNoInvites();
                                        } else {
                                            presenterListener.presentError(throwable.getMessage());
                                            Log.e("EventsInvitesFragment: ", throwable.getMessage(), throwable.fillInStackTrace());
                                        }
                                    }));
    }


    @Override
    public void subscribe() {
        if (FirebaseAuth.getInstance().getUid() != null) {
            userId = FirebaseAuth.getInstance().getUid();
            loadPendingInvites();
        } else {
            presenterListener.showSignedOutEmptyState();
        }

    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    /**
     * PresenterListener Definition
     */
    public interface EventInvitesViewListener extends BasePresenterListener {
        void eventPendingInvitationResponseReturned(String eventId, EventDetail event);

        void presentError(String message);

        void removeEventFromList(String eventId, EventDetail eventDetail);

        void showEmptyStateForNoInvites();
    }
}
