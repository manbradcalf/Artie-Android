package com.bookyrself.bookyrself.presenters;

import android.support.v4.util.Pair;
import android.util.Log;

import com.bookyrself.bookyrself.data.EventInvites.EventInvitesRepo;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EventInvitesFragmentPresenter implements BasePresenter {

    private final EventInvitesViewListener listener;
    private EventInvitesRepo eventInvitesRepo;
    private CompositeDisposable compositeDisposable;
    private String userId;

    public EventInvitesFragmentPresenter(EventInvitesViewListener listener, String userId) {
        this.userId = userId;
        this.listener = listener;
    }

    public void loadPendingInvites(String userId) {
        compositeDisposable
                .add(eventInvitesRepo.getPendingEventInvites(userId)
                        .forEach(stringEventDetailPair ->
                                //The first and second are flipped because
                                // I need to use the detail in the view as the hashmap's key
                                // in order to get the eventId to pass the to the eventdetail activity
                                //TODO This seems bass-ackwards. Figure out a different solution
                                eventDetailReturned(stringEventDetailPair.second, stringEventDetailPair.first));
    }

    public void acceptEventInvite(final String userId, final String eventId) {
        FirebaseService.getAPI().acceptInvite(true, userId, eventId).enqueue(new Callback<Boolean>() {
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

    public void rejectEventInvite(String userId, final String eventId) {
        FirebaseService.getAPI().rejectInvite(true, userId, eventId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.body() != null) {
                    eventInviteAccepted(false, eventId);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {

            }
        });
    }

    private void setEventUserAsAttending(String userId, final String eventId) {
     FirebaseService.getAPI().setEventUserAsAttending(true, userId, eventId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.body() != null) {
                    eventInviteAccepted(true, eventId);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("EventsInteractor ", t.getMessage());
            }
        });
    }

    private void eventInviteAccepted(boolean accepted, String eventId) {
        listener.eventInviteAccepted(accepted, eventId);
    }

    @Override
    public void eventDetailReturned(EventDetail event, String eventId) {
        listener.eventPendingInvitationResponseReturned(event, eventId);
    }

    @Override
    public void eventIdsOfEventsWithPendingInvitesReturned(List<String> eventIds) {
        for (String eventId : eventIds) {
            eventsInteractor.getEventDetail(eventId);
        }
    }

    @Override
    public void presentError(String error) {
        listener.presentError(error);
    }

    @Override
    public void noInvitesReturnedForUser() {
        listener.noInvitesReturnedForUser();
    }

    @Override
    public void subscribe() {
        loadPendingInvites(userId);
    }

    @Override
    public void unsubscribe() {

    }

    /**
     * Contract / Listener
     */
    public interface EventInvitesViewListener {
        void eventPendingInvitationResponseReturned(EventDetail event, String eventId);

        void presentError(String message);

        void eventInviteAccepted(boolean accepted, String eventId);

        void noInvitesReturnedForUser();
    }
}
