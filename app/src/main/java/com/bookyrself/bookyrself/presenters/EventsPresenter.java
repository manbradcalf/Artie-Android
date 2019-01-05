package com.bookyrself.bookyrself.presenters;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.models.SerializedModels.User.EventInfo;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 3/11/18.
 */

public class EventsPresenter implements EventsInteractor.EventsInteractorListener {

    private final CalendarPresenterListener listener;
    private final EventsInteractor eventsInteractor;
    private final FirebaseService service;

    /**
     * Constructor
     */
    public EventsPresenter(CalendarPresenterListener listener) {
        this.listener = listener;
        this.service = new FirebaseService();
        this.eventsInteractor = new EventsInteractor(this);
    }

    /**
     * Methods
     */
    public void loadUsersEventInfo(final String userId) {
        service.getAPI().getUsersEventInfo(userId).enqueue(new Callback<HashMap<String, EventInfo>>() {
            @Override
            public void onResponse(@NonNull Call<HashMap<String, EventInfo>> call, @NonNull Response<HashMap<String, EventInfo>> response) {
                if (response.body() != null) {
                    getEventDetails(new ArrayList<>(response.body().keySet()));
                }

            }

            @Override
            public void onFailure(Call<HashMap<String, EventInfo>> call, Throwable t) {
                Log.e(this.toString(), "Failed to load user events for user " + userId);
            }
        });
    }

    private void getEventDetails(List<String> eventIds) {
        for (String id : eventIds){
            eventsInteractor.getEventDetail(id);
        }

    }

    @Override
    public void eventDetailReturned(EventDetail event, String eventId) {

    }

    @Override
    public void usersEventsReturned(List<Event> events) {

    }

    @Override
    public void eventCreated(String eventId, List<String> usersToInvite) {

    }

    @Override
    public void presentError(String error) {

    }

    @Override
    public void oneEventDetailOfManyReturned(EventDetail body, List<String> eventIds, String eventId) {

    }

    /**
     * Contract / Listener
     */
    public interface CalendarPresenterListener {
        void selectEventOnCalendar(String eventId);

        void goToEventDetail(String eventId);

        void userEventsReady(List<Event> events);
    }

}
