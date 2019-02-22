package com.bookyrself.bookyrself.presenters;

import android.support.v4.util.Pair;

import com.bookyrself.bookyrself.data.EventInvites.EventInvitesRepo;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Predicate;


public class EventInvitesFragmentPresenter implements BasePresenter {

    private final EventInvitesViewListener listener;
    private EventInvitesRepo eventInvitesRepo;
    private CompositeDisposable compositeDisposable;
    private String userId;

    public EventInvitesFragmentPresenter(EventInvitesViewListener listener, String userId) {
        this.userId = userId;
        this.listener = listener;
        this.compositeDisposable = new CompositeDisposable();
        this.eventInvitesRepo = new EventInvitesRepo();
    }

    public void loadPendingInvites(String userId) {
        compositeDisposable
                .add(eventInvitesRepo.getPendingEventInvites(userId)

                        // Find a way to handle the fact that this stream may not return
                        // anything. How do I turn off the loading state?

                        .forEach(stringEventDetailPair ->
                                //The first and second are flipped because
                                // I need to use the detail in the view as the hashmap's key
                                // in order to get the eventId to pass the to the eventdetail activity
                                //TODO This seems bass-ackwards. Figure out a different solution
                                listener.eventPendingInvitationResponseReturned(
                                        stringEventDetailPair.second, stringEventDetailPair.first)));
    }

    public void acceptEventInvite(final String userId, final String eventId) {
        compositeDisposable.add(
                eventInvitesRepo.acceptEventInvite(userId,eventId)
                        .doOnNext(aBoolean -> listener.removeEventFromList(eventId))
                        .doOnError(throwable -> listener.presentError(throwable.getMessage()))
                        .subscribe());
    }

    public void rejectEventInvite(String userId, final String eventId) {
        compositeDisposable.add(
                eventInvitesRepo.rejectEventInvite(userId,eventId)
                        .doOnNext(aBoolean -> listener.removeEventFromList(eventId))
                        .doOnError(throwable -> listener.presentError(throwable.getMessage()))
                        .subscribe());
    }


    @Override
    public void subscribe() {
        loadPendingInvites(userId);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    /**
     * Contract / Listener
     */
    public interface EventInvitesViewListener {
        void eventPendingInvitationResponseReturned(EventDetail event, String eventId);

        void presentError(String message);

        void removeEventFromList(String eventId);

        void noInvitesReturnedForUser();
    }
}
