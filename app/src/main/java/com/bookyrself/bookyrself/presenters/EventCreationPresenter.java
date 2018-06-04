package com.bookyrself.bookyrself.presenters;

import android.support.annotation.NonNull;

import com.bookyrself.bookyrself.models.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 3/9/18.
 */

public class EventCreationPresenter {

    private HashMap<String, Long> userEventCountHashMap;
    private EventCreationPresenterListener mListener;
    private FirebaseService mService;

    /**
     * Contract / Listener
     */
    public interface EventCreationPresenterListener {
        void createEvent(String[] userIds);

        void addToPotentialUsers(String userId);

        void removeFromPotentialUsers(String userId);
    }

    /**
     * Constructor
     */
    public EventCreationPresenter(EventCreationPresenterListener listener) {
        this.mListener = listener;
        this.mService = new FirebaseService();
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
}
