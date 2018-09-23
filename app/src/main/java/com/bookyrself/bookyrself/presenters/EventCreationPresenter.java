package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.interactors.ContactsInteractor;
import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.interactors.UsersInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.User;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers._source;
import com.bookyrself.bookyrself.models.SerializedModels.User.MiniEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by benmedcalf on 3/9/18.
 */

public class EventCreationPresenter implements ContactsInteractor.ContactsInteractorListener, EventsInteractor.EventsInteractorListener, UsersInteractor.UsersInteractorListener {

    private HashMap<String, Long> userEventCountHashMap;
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

    //TODO: I may not need this mularkey if I can keep ES from automapping
//    public void addToUserEventsCountHashMap(final String userid) {
//
//        mService.getAPI().getUserEvents(userid).enqueue(new Callback<List<MiniEvent>>() {
//            @Override
//            public void onResponse(@NonNull Call<List<MiniEvent>> call, @NonNull Response<List<MiniEvent>> response) {
//                if (response.body() != null) {
//                    userEventCountHashMap.put(userid, (long) response.body().size());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<MiniEvent>> call, Throwable t) {
//
//            }
//        });
//    }
    public void createEvent(EventDetail event) {

        // Iterate through the hashmap of users and their events array size.
        // For each user in the hashmpap, add the event to their
        eventsInteractor.createEvent(event);
    }


    /**
     * ContactsInteractor Listeners
     */
    @Override
    public void contactsReturned(List<String> ids) {
        //TODO: I may not need this mularkey if I can keep ES from automapping
//        for (String id : ids) {
//            addToUserEventsCountHashMap(id);
//        }
    }

    @Override
    public void userReturned(String id, _source user) {

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
    public void eventCreated(String eventId, MiniEvent miniEvent, List<User> usersToInvite) {
        //        for (User user : usersToInvite) {
//              //TODO: Fix the value of eventId. Needs to be string across the board
//            usersInteractor.addEventToUser(miniEvent, user.getUserId(), String.valueOf(miniEvent.getId()));
//        }
        ArrayList<String> testList = new ArrayList<>(Arrays.asList("1", "2", "3", "4"));
        for (String item : testList) {
            usersInteractor.addEventToUser(miniEvent, item, eventId);
        }
    }

    @Override
    public void presentError(String error) {

    }

    @Override
    public void eventAddedToUserSuccessfully() {
        //TODO: Find a way to determine it was added to _all_ invited users
    }

    /**
     * Contract / Listener
     */
    public interface EventCreationPresenterListener {
        void eventCreated();

        void addToPotentialUsers(String userId);

        void removeFromPotentialUsers(String userId);
    }
}
