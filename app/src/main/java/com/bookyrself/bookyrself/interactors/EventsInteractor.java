package com.bookyrself.bookyrself.interactors;

import android.util.Log;

import com.bookyrself.bookyrself.models.SerializedModels.EventCreationResponse;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsInteractor {

    //TODO: What are the reprecussions of making the service static?
    private final FirebaseService service;
    private final EventsInteractorListener listener;

    public EventsInteractor(EventsInteractorListener listener) {
        this.service = new FirebaseService();
        this.listener = listener;
    }

    public void getEventDetail(final String eventId) {
        service.getAPI().getEventData(eventId).enqueue(new Callback<EventDetail>() {
            @Override
            public void onResponse(Call<EventDetail> call, Response<EventDetail> response) {
                if (response.body() != null) {

                    listener.eventDetailReturned(response.body(), eventId);
                }
            }

            @Override
            public void onFailure(Call<EventDetail> call, Throwable t) {
                listener.presentError(t.getMessage());
            }
        });
    }

    public void getMultipleEventDetails(final List<String> eventIds) {
        for (final String id : eventIds) {
            service.getAPI().getEventData(id).enqueue(new Callback<EventDetail>() {
                @Override
                public void onResponse(Call<EventDetail> call, Response<EventDetail> response) {
                    if (response.body() != null) {
                        listener.oneEventDetailOfManyReturned(response.body(), eventIds, id);
                    }
                }

                @Override
                public void onFailure(Call<EventDetail> call, Throwable t) {
                    listener.presentError(t.getMessage());
                }
            });

        }
    }

    public void createEvent(final EventDetail event) {

        // Create the list of userIds of attendees
        // We'll use this list of ids to add the event to the users.
        // An event's users are stored as a hashmap due to Firebase DB limitations
        final List<String> userIds = new ArrayList<>();
        if (event.getUsers() != null) {
            for (String key : event.getUsers().keySet()) {
                userIds.add(key);
            }
        }

        service.getAPI().createEvent(event).enqueue(new Callback<EventCreationResponse>() {
            @Override
            public void onResponse(Call<EventCreationResponse> call, Response<EventCreationResponse> response) {
                String eventId = response.body().getName();
                listener.eventCreated(eventId, userIds);
            }

            @Override
            public void onFailure(Call<EventCreationResponse> call, Throwable t) {
                Log.e("EventInteractor:", "MiniEvent Creation Failed!!!");
            }
        });
    }

    public interface EventsInteractorListener {

        void eventDetailReturned(EventDetail event, String eventId);

        void usersEventsReturned(List<Event> events);

        void eventCreated(String eventId, List<String> usersToInvite);

        void presentError(String error);

        void oneEventDetailOfManyReturned(EventDetail body, List<String> eventIds, String eventId);
    }


}
