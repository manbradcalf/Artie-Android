package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.interactors.UsersInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;


public class EventInvitesFragmentPresenter implements UsersInteractor.UsersEventInvitesInteractorListener, EventsInteractor.EventInvitesInteractorListener {

    private final EventInvitesUserDetailPresenterListener listener;
    private UsersInteractor userInteractor;
    private EventsInteractor eventsInteractor;

    public EventInvitesFragmentPresenter(EventInvitesUserDetailPresenterListener listener) {
        this.listener = listener;

        //TODO: Should i do this elsewhere?
        this.userInteractor = new UsersInteractor(this);
        this.eventsInteractor = new EventsInteractor(this);
    }

    public void getEventInvites(String userId) {
        userInteractor.getUserInvites(userId);
    }

    public void acceptEventInvite(String userId, String eventId) {
        eventsInteractor.acceptEventInvite(eventId, userId);
    }

    @Override
    public void eventInviteAccepted(String eventId) {
        listener.eventInviteAccepted(eventId);
    }

    @Override
    public void eventIdOfEventWithPendingInvitesReturned(String eventId) {
        eventsInteractor.getEventDetail(eventId);
    }

    @Override
    public void presentError(String error) {
    }

    @Override
    public void noInvitesReturnedForUser() {
        listener.noInvitesReturnedForUser();
    }

    /**
     * Contract / Listener
     */
    public interface EventInvitesUserDetailPresenterListener {
        void eventsPendingInvitationResponseReturned(EventDetail event, String eventId);

        void presentError(String message);

        void eventInviteAccepted(String eventId);

        void noInvitesReturnedForUser();
    }
}
