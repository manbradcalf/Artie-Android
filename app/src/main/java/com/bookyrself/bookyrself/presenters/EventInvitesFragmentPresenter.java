package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.data.EventInvites.EventInvitesRepo;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.views.MainActivity;

import java.util.NoSuchElementException;

import io.reactivex.disposables.CompositeDisposable;


public class EventInvitesFragmentPresenter implements BasePresenter {

    private final EventInvitesViewListener listener;
    private EventInvitesRepo eventInvitesRepo;
    private CompositeDisposable compositeDisposable;
    private String userId;

    public EventInvitesFragmentPresenter(EventInvitesViewListener listener, String userId) {
        this.userId = userId;
        this.listener = listener;
        this.compositeDisposable = new CompositeDisposable();
        this.eventInvitesRepo = MainActivity.getEventInvitesRepo();
    }

    public void loadPendingInvites(String userId) {
        compositeDisposable
                .add(eventInvitesRepo.getPendingEventInvites(userId)
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
                                    }
                                }));

    }

    public void acceptEventInvite(final String userId, final String eventId, EventDetail eventDetail) {
        compositeDisposable.add(
                eventInvitesRepo.acceptEventInvite(userId, eventId)
                        .doOnNext(aBoolean -> listener.removeEventFromList(eventId, eventDetail))
                        .doOnError(throwable -> listener.presentError(throwable.getMessage()))
                        .subscribe());
    }

    public void rejectEventInvite(String userId, final String eventId, EventDetail eventDetail) {
        compositeDisposable.add(
                eventInvitesRepo.rejectEventInvite(userId, eventId)
                        .doOnNext(aBoolean -> listener.removeEventFromList(eventId, eventDetail))
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
        void eventPendingInvitationResponseReturned(String eventId, EventDetail event);

        void presentError(String message);

        void removeEventFromList(String eventId, EventDetail eventDetail);

        void showEmptyStateForNoInvites();
    }
}
