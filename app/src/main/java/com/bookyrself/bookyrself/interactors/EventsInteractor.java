package com.bookyrself.bookyrself.interactors;

import android.util.Log;

import com.bookyrself.bookyrself.models.SerializedModels.EventCreationResponse;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.User;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.models.SerializedModels.User.MiniEvent;
import com.bookyrself.bookyrself.services.FirebaseService;

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

    public void getUsersEvents(String userId) {
        service.getAPI().getUserEvents(userId).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                //TODO: Revisit this logic
                if (response.body() != null) {
                    if (response.body().isEmpty()) {
                        listener.presentError("User has no events!");
                    } else {
                        listener.usersEventsReturned(response.body());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                listener.presentError(t.getMessage());
            }
        });
    }

    public void getEventDetails(String eventId) {
        service.getAPI().getEventData(eventId).enqueue(new Callback<EventDetail>() {
            @Override
            public void onResponse(Call<EventDetail> call, Response<EventDetail> response) {
                if (response.body() != null) {
                    listener.eventDetailReturned(response.body());
                }
            }

            @Override
            public void onFailure(Call<EventDetail> call, Throwable t) {
                listener.presentError(t.getMessage());
            }
        });
    }

    public void createEvent(final EventDetail event) {
        service.getAPI().createEvent(event).enqueue(new Callback<EventCreationResponse>() {
            @Override
            public void onResponse(Call<EventCreationResponse> call, Response<EventCreationResponse> response) {
                String eventId = response.body().getName();
                MiniEvent miniEvent = createMiniEventFromFullEvent(event);
                listener.eventCreated(eventId, miniEvent, event.getUsers());
            }

            @Override
            public void onFailure(Call<EventCreationResponse> call, Throwable t) {
                Log.e("EventInteractor:", "MiniEvent Creation Failed!!!");
            }
        });
    }


    private MiniEvent createMiniEventFromFullEvent(EventDetail fullEvent) {
        MiniEvent miniEvent = new MiniEvent();
        miniEvent.setDate(fullEvent.getDate());
        miniEvent.setEventname(fullEvent.getEventname());
        miniEvent.setmIsInviteAccepted(false);
        //TODO: Id needs to change from Long to String. Big job. Need ES remapping and new jSON fb data
        return miniEvent;
    }

    public interface EventsInteractorListener {

        void eventDetailReturned(EventDetail event);

        void usersEventsReturned(List<Event> events);

        void eventCreated(String eventId, MiniEvent miniEvent, List<User> usersToInvite);

        void presentError(String error);
    }


}
