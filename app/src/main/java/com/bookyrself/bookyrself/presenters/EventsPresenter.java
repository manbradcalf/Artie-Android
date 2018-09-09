package com.bookyrself.bookyrself.presenters;

import android.support.annotation.NonNull;

import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 3/11/18.
 */

public class EventsPresenter {

    private final CalendarPresenterListener listener;
    private final FirebaseService service;

    /**
     * Constructor
     */
    public EventsPresenter(CalendarPresenterListener listener) {
        this.listener = listener;
        this.service = new FirebaseService();
    }

    /**
     * Methods
     */
    public void loadUserEvents(String userId) {
        service.getAPI().getUserEvents(userId).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                listener.eventsReady(response.body());
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {

            }
        });
    }

    /**
     * Contract / Listener
     */
    public interface CalendarPresenterListener {
        void selectEventOnCalendar(String eventId);

        void goToEventDetail(String eventId);

        void eventsReady(List<Event> events);
    }

}
