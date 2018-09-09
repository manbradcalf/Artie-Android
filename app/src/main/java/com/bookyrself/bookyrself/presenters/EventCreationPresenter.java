package com.bookyrself.bookyrself.presenters;

import android.support.annotation.NonNull;

import com.bookyrself.bookyrself.interactors.ContactsInteractor;
import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetailResponse.EventDetailResponse;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers._source;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 3/9/18.
 */

public class EventCreationPresenter implements ContactsInteractor.ContactsInteractorListener, EventsInteractor.EventsInteractorListener {

    private HashMap<String, Long> userEventCountHashMap;
    private EventCreationPresenterListener presenterListener;
    private ContactsInteractor contactsInteractor;
    private EventsInteractor eventsInteractor;

    /**
     * Constructor
     */
    public EventCreationPresenter(EventCreationPresenterListener listener) {
        this.contactsInteractor = new ContactsInteractor(this);
        this.eventsInteractor = new EventsInteractor(this);
        this.presenterListener = listener;
    }

    /**
     * Methods
     */
    public void addToUserEventsCountHashMap(final String userid) {

        mService.getAPI().getUserEvents(userid).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                if (response.body() != null) {
                    userEventCountHashMap.put(userid, (long) response.body().size());
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {

            }
        });
    }

    public void createEvent(Event event) {

        // Iterate through the hashmap of users and their events array size.
        // For each user in the hashmpap, add the event to their
        for (String userId : userEventCountHashMap.keySet()) {
            mService.getAPI().addEventToUser(event, userId, userEventCountHashMap.get(userId));
        }
    }


    /**
     * ContactsInteractor Listeners
     */
    @Override
    public void contactsReturned(List<String> ids) {
        for (String id : ids) {
            addToUserEventsCountHashMap(id);
        }
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
    public void eventDetailReturned(EventDetailResponse event) {

    }

    @Override
    public void usersEventsReturned(List<Event> events) {

    }

    @Override
    public void presentError(String error) {

    }

    /**
     * Contract / Listener
     */
    public interface EventCreationPresenterListener {
        void createEvent(String[] userIds);

        void addToPotentialUsers(String userId);

        void removeFromPotentialUsers(String userId);
    }
}
