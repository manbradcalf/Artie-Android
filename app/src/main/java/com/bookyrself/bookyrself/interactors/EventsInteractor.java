package com.bookyrself.bookyrself.interactors;

import com.bookyrself.bookyrself.models.SerializedModels.EventDetailResponse.EventDetailResponse;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsInteractor {

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
        service.getAPI().getEventData(eventId).enqueue(new Callback<EventDetailResponse>() {
            @Override
            public void onResponse(Call<EventDetailResponse> call, Response<EventDetailResponse> response) {
                if (response.body() != null) {
                    listener.eventDetailReturned(response.body());
                }
            }

            @Override
            public void onFailure(Call<EventDetailResponse> call, Throwable t) {
                    listener.presentError(t.getMessage());
            }
        });
    }

    public interface EventsInteractorListener {

        void eventDetailReturned(EventDetailResponse event);

        void usersEventsReturned(List<Event> events);

        void presentError(String error);
    }


}
