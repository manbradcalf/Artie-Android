package com.bookyrself.bookyrself.data;

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

    private EventCreationInteractorListener eventCreationInteractorListener;
    private EventInvitesInteractorListener eventInvitesInteractorListener;

    public EventsInteractor(EventCreationInteractorListener listener) {
        this.eventCreationInteractorListener = listener;
    }

    public EventsInteractor(EventInvitesInteractorListener listener) {
        this.eventInvitesInteractorListener = listener;
    }

    public void getEventDetail(final String eventId) {
//        service.getAPI().getEventData(eventId).enqueue(new Callback<EventDetail>() {
//            @Override
//            public void onResponse(Call<EventDetail> call, Response<EventDetail> response) {
//                if (response.body() != null) {
//                    eventsInteractorListener.eventDetailReturned(response.body(), eventId);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<EventDetail> call, Throwable t) {
//                eventsInteractorListener.presentError(t.getMessage());
//            }
//        });
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

        FirebaseService.getAPI().createEvent(event).enqueue(new Callback<EventCreationResponse>() {
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

    //TODO: Figure out how to handle this if you are the host and accepting/rejcting invites to your own

}

/**
 * Contracts / Listeners
 */
interface EventsInteractorListener {

    void eventDetailReturned(EventDetail event, String eventId);

    void presentError(String error);

}

interface EventCreationInteractorListener extends EventsInteractorListener {

    void addNewlyCreatedEventToUsers(String eventId, List<String> attendeesToInvite, String hostUserId);
}

interface EventInvitesInteractorListener extends EventsInteractorListener {

    void eventInviteAccepted(boolean accepted, String eventId);
}

