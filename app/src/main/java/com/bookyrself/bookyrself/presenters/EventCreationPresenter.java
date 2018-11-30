package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.interactors.ContactsInteractor;
import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.interactors.UsersInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.models.SerializedModels.User.EventInfo;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by benmedcalf on 3/9/18.
 */

public class EventCreationPresenter implements ContactsInteractor.ContactsInteractorListener, EventsInteractor.EventsInteractorListener, UsersInteractor.UsersInteractorListener {

    private HashMap<String, Long> userEventCountHashMap;
    private EventCreationPresenterListener presenterListener;
    private ContactsInteractor contactsInteractor;
    private EventsInteractor eventsInteractor;
    private UsersInteractor usersInteractor;
    List<String> contactIds;
    List<User> contactsList;
    Map<User, String> usersIdAndDetailMap;

    /**
     * Constructor
     */
    public EventCreationPresenter(EventCreationPresenterListener listener) {
        this.contactsInteractor = new ContactsInteractor(this);
        this.eventsInteractor = new EventsInteractor(this);
        this.usersInteractor = new UsersInteractor(this);
        this.presenterListener = listener;
        contactsList = new ArrayList<>();
        usersIdAndDetailMap = new HashMap<>();
    }

    /**
     * Methods
     */

    public void createEvent(EventDetail event) {

        // Iterate through the hashmap of users and their events array size.
        // For each user in the hashmpap, add the event to their
        eventsInteractor.createEvent(event);
    }

    public void getContacts(String userId) {

        contactsInteractor.getContactIds(userId);
    }

    public void setDate(String date) {
        presenterListener.dateAdded(date);
    }


    /**
     * ContactsInteractor Listeners
     */
    @Override
    public void contactsReturned(HashMap<String, Boolean> contacts) {
        if (contacts != null) {
            contactIds = new ArrayList<>(contacts.keySet());
            contactsInteractor.getUsers(contactIds);
        }
    }

    @Override
    public void userReturned(String id, User user) {
        usersIdAndDetailMap.put(user, id);
        contactsList.add(user);

        if (usersIdAndDetailMap.size() == contactIds.size()) {
            presenterListener.contactsReturned(usersIdAndDetailMap, contactsList);
        }
    }

    @Override
    public void noUsersReturned() {

    }

    /**
     * EventsInteractorListener
     */

    @Override
    public void eventDetailReturned(EventDetail event) {

    }

    @Override
    public void usersEventsReturned(List<Event> events) {

    }

    @Override
    public void eventCreated(String eventId, List<String> userIdsOfAttendees) {
        if (userIdsOfAttendees.size() != 0) {
            for (String userId : userIdsOfAttendees) {
                EventInfo eventInfo = new EventInfo();
                eventInfo.setIsInviteAccepted(false);
                usersInteractor.addEventToUser(eventInfo, userId, eventId);
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
    public void oneEventDetailOfManyReturned(EventDetail body, List<String> eventIds, String eventId) {

    }

    @Override
    public void eventAddedToUserSuccessfully() {
        //TODO: Find a way to determine it was added to _all_ invited users
    }

    @Override
    public void userDetailReturned(User user, String userId) {

    }

    /**
     * Contract / Listener
     */
    public interface EventCreationPresenterListener {
        void addToPotentialUsers(String userId);

        void contactsReturned(Map<User, String> contactsAndUserIdMap, List<User> contactsList);

        void eventCreated();

        void removeFromPotentialUsers(String userId);

        void dateAdded(String date);
    }
}
