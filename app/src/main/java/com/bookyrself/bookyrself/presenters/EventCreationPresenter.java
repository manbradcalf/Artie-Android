package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.interactors.ContactsInteractor;
import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.interactors.UsersInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.User.EventInviteInfo;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by benmedcalf on 3/9/18.
 */

public class EventCreationPresenter implements ContactsInteractor.ContactsInteractorListener, EventsInteractor.EventCreationInteractorListener, UsersInteractor.UsersInteractorListener {

    private EventCreationPresenterListener presenterListener;
    private ContactsInteractor contactsInteractor;
    private EventsInteractor eventsInteractor;
    private UsersInteractor usersInteractor;

    /**
     * Constructor
     */
    public EventCreationPresenter(EventCreationPresenterListener listener) {
        this.contactsInteractor = new ContactsInteractor(this);
        this.eventsInteractor = new EventsInteractor(this);
        this.usersInteractor = new UsersInteractor(this);
        this.presenterListener = listener;
    }

    /**
     * Methods
     */

    public void createEvent(EventDetail event) {
        eventsInteractor.createEvent(event);
    }

    public void getContacts(String userId) {

        contactsInteractor.getContactIds(userId);
    }

    public void setDate(String date) {
        if (date != null) {
            presenterListener.dateAdded(date);
        }
    }


    /**
     * ContactsInteractor Listeners
     */
    @Override
    public void contactsReturned(HashMap<String, Boolean> contacts) {
        if (contacts != null) {
            List<String> contactIds = new ArrayList<>(contacts.keySet());
            contactsInteractor.getUsersAsContacts(contactIds);
        }
    }

    @Override
    public void userReturned(String id, User user) {
        if (user != null) {
            presenterListener.contactReturned(user, id);
        }
    }

    @Override
    public void noUsersReturned() {

    }

    /**
     * EventsInteractorListener
     */
    @Override
    public void eventDetailReturned(EventDetail event, String eventId) {

    }

    @Override
    public void addNewlyCreatedEventToUsers(String eventId, List<String> userIdsOfAttendees, String hostUserId) {
        EventInviteInfo hostEventInviteInfo = new EventInviteInfo();
        hostEventInviteInfo.setIsInviteAccepted(true);
        hostEventInviteInfo.setIsHost(true);
        hostEventInviteInfo.setIsInviteRejected(false);
        usersInteractor.addEventToUser(hostEventInviteInfo, hostUserId, eventId);

        if (userIdsOfAttendees.size() != 0) {
            for (String userId : userIdsOfAttendees) {
                EventInviteInfo eventInviteInfo = new EventInviteInfo();
                eventInviteInfo.setIsInviteAccepted(false);
                eventInviteInfo.setIsInviteRejected(false);
                eventInviteInfo.setIsHost(false);
                usersInteractor.addEventToUser(eventInviteInfo, userId, eventId);
            }
            presenterListener.eventCreated();
        }
        //TODO: Wut? I'm tired
        else {
            presenterListener.eventCreated();
        }
    }

    @Override
    public void presentError(String error) {

    }

    @Override
    public void userDetailReturned(User user, String userId) {

    }

    /**
     * Contract / Listener
     */
    public interface EventCreationPresenterListener {

        void contactReturned(User contact, String userId);

        void eventCreated();

        void dateAdded(String date);
    }
}
