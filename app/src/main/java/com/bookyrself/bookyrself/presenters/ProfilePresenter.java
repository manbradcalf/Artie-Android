package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.User.EventInfo;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilePresenter implements EventsInteractor.EventsInteractorListener {
    private final ProfilePresenterListener listener;
    private final FirebaseService service;
    private final EventsInteractor eventsInteractor;

    /**
     * Construction
     */
    public ProfilePresenter(ProfilePresenterListener listener) {
        this.listener = listener;
        this.service = new FirebaseService();
        this.eventsInteractor = new EventsInteractor(this);
    }

    /**
     * Methods
     */
    public void createUser(User user, String UID) {
        service.getAPI().addUser(user, UID).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // response
                listener.profileInfoReady(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // failure
            }
        });
    }

    public void patchUser(User user, String UID) {
        service.getAPI().patchUser(user, UID).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // response
                listener.profileInfoReady(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // failure
            }
        });
    }

    public void getUser(String UID) {
        service.getAPI().getUserDetails(UID).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                HashMap<String, EventInfo> events = response.body().getEvents();
                if (events != null) {
                    getEventDetails(new ArrayList<>(events.keySet()));
                }
                listener.profileInfoReady(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    private void getEventDetails(List<String> eventIds) {
        for (String id : eventIds) {
            eventsInteractor.getEventDetail(id);
        }
    }

    @Override
    public void eventDetailReturned(EventDetail event, String eventId) {
        listener.eventReady(event, eventId);
    }

    @Override
    public void addNewlyCreatedEventToUsers(String eventId, List<String> attendeesToInvite, String hostUserId) {

    }

    @Override
    public void presentError(String error) {

    }

    @Override
    public void eventInviteAccepted(String eventId) {

    }


    /**
     * Contract / Listener
     */
    public interface ProfilePresenterListener {

        void profileInfoReady(User user);

        void eventReady(EventDetail event, String eventId);

        void presentToast(String message);

        void loadingState(Boolean show);
    }
}
