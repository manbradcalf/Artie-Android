package com.bookyrself.bookyrself.interactors;

import android.util.Log;

import com.bookyrself.bookyrself.models.SerializedModels.EventCreationResponse;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsInteractor {

    private FirebaseService service;
    private EventsInteractorListener eventsInteractorListener;
    private EventCreationInteractorListener eventCreationInteractorListener;
    private EventInvitesInteractorListener eventInvitesInteractorListener;

    public EventsInteractor(EventsInteractorListener listener) {
        this.service = new FirebaseService();
        this.eventsInteractorListener = listener;
    }

    public EventsInteractor(EventCreationInteractorListener listener) {
        this.service = new FirebaseService();
        this.eventsInteractorListener = listener;
        this.eventCreationInteractorListener = listener;
    }

    public EventsInteractor(EventInvitesInteractorListener listener) {
        this.service = new FirebaseService();
        this.eventsInteractorListener = listener;
        this.eventInvitesInteractorListener = listener;
    }

    public void getEventDetail(final String eventId) {
        service.getAPI().getEventData(eventId).enqueue(new Callback<EventDetail>() {
            @Override
            public void onResponse(Call<EventDetail> call, Response<EventDetail> response) {
                if (response.body() != null) {
                    eventsInteractorListener.eventDetailReturned(response.body(), eventId);
                }
            }

            @Override
            public void onFailure(Call<EventDetail> call, Throwable t) {
                eventsInteractorListener.presentError(t.getMessage());
            }
        });
    }

    //TODO: Is this needed?
    public void getMultipleEventDetails(final List<String> eventIds) {
        for (final String id : eventIds) {
            service.getAPI().getEventData(id).enqueue(new Callback<EventDetail>() {
                @Override
                public void onResponse(Call<EventDetail> call, Response<EventDetail> response) {
                    if (response.body() != null) {
                        eventsInteractorListener.eventDetailReturned(response.body(), id);
                    }
                }

                @Override
                public void onFailure(Call<EventDetail> call, Throwable t) {
                    eventsInteractorListener.presentError(t.getMessage());
                }
            });

        }
    }

    // Create the list of userIds of attendees
    // We'll use this list of ids to add the event to the users.
    // An event's users are stored as a hashmap due to Firebase DB limitations
    public void createEvent(final EventDetail event) {

        final List<String> userIds = new ArrayList<>();
        // Add the host to the list of userIds so it is added to their events in Firebase
        final String hostUserId = FirebaseAuth.getInstance().getUid();

        if (event.getUsers() != null) {
            userIds.addAll(event.getUsers().keySet());
        }

        service.getAPI().createEvent(event).enqueue(new Callback<EventCreationResponse>() {
            @Override
            public void onResponse(Call<EventCreationResponse> call, Response<EventCreationResponse> response) {
                String eventId = response.body().getName();
                eventCreationInteractorListener.addNewlyCreatedEventToUsers(eventId, userIds, hostUserId);
            }

            @Override
            public void onFailure(Call<EventCreationResponse> call, Throwable t) {
                Log.e("EventInteractor: ", "Event Creation Failed!!!");
            }
        });
    }

    public void acceptEventInvite(final String eventId, final String userId) {
        service.getAPI().acceptInvite(true, userId, eventId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.body() != null) {
                    setEventUserAsAttending(userId, eventId);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {

            }
        });
    }

    //TODO: Figure out how to handle this if you are the host and accepting/rejcting invites to your own
    private void setEventUserAsAttending(String userId, final String eventId) {
        service.getAPI().setEventUserAsAttending(true, userId, eventId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.body() != null) {
                    eventInvitesInteractorListener.eventInviteAccepted(true, eventId);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("EventsInteractor ", t.getMessage());
            }
        });
    }

    public void rejectEventInvite(String userId, final String eventId) {
        service.getAPI().rejectInvite(true, userId, eventId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.body() != null) {
                    eventInvitesInteractorListener.eventInviteAccepted(false, eventId);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {

            }
        });
    }

    /**
     * Contracts / Listeners
     */
    public interface EventsInteractorListener {

        void eventDetailReturned(EventDetail event, String eventId);

        void presentError(String error);

    }

    public interface EventCreationInteractorListener extends EventsInteractorListener {

        void addNewlyCreatedEventToUsers(String eventId, List<String> attendeesToInvite, String hostUserId);
    }

    public interface EventInvitesInteractorListener extends EventsInteractorListener {

        void eventInviteAccepted(boolean accepted, String eventId);
    }
}
