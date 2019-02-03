package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.interactors.UsersInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;

import java.util.List;


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
        eventsInteractor.acceptEventInvite(userId, eventId);
    }

    public void rejectInvite(String userId, String eventId) {
        eventsInteractor.rejectEventInvite(userId, eventId);
    }

    @Override
    public void eventInviteAccepted(boolean accepted, String eventId) {
        listener.eventInviteAccepted(accepted, eventId);
    }

    @Override
    public void eventDetailReturned(EventDetail event, String eventId) {
        listener.eventPendingInvitationResponseReturned(event, eventId);
    }

    @Override
    public void eventIdsOfEventsWithPendingInvitesReturned(List<String> eventIds) {
        for (String eventId : eventIds) {
            eventsInteractor.getEventDetail(eventId);
        }
    }

    @Override
    public void presentError(String error) {
        listener.presentError(error);
    }

    @Override
    public void noInvitesReturnedForUser() {
        listener.noInvitesReturnedForUser();
    }

    /**
     * Contract / Listener
     */
    public interface EventInvitesUserDetailPresenterListener {
        void eventPendingInvitationResponseReturned(EventDetail event, String eventId);

        void presentError(String message);

        void eventInviteAccepted(boolean accepted, String eventId);

        void noInvitesReturnedForUser();
    }
}
