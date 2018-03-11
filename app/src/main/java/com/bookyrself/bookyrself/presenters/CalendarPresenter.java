package com.bookyrself.bookyrself.presenters;

import android.support.annotation.NonNull;

import com.bookyrself.bookyrself.models.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 3/11/18.
 */

public class CalendarPresenter {

    private final CalendarPresenterListener listener;
    private final FirebaseService service;

    /**
     * Contract / Listener
     */
    public interface CalendarPresenterListener {
        void selectEventonCalendar(String eventId);

        void goToEventDetail(String eventId);

        void calendarReady(List<Event> events);
    }

    /**
     * Constructor
     */
    public CalendarPresenter(CalendarPresenterListener listener) {
        this.listener = listener;
        this.service = new FirebaseService();
    }

    /**
     * Methods
     */
    public void loadUserCalender(String userId) {
        service.getAPI().getUserEvents(userId).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                listener.calendarReady(response.body());
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {

            }
        });
    }

}
