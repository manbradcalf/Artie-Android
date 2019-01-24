package com.bookyrself.bookyrself.presenters;

import android.support.annotation.NonNull;

import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.User.EventInfo;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;

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
    private final FirebaseAuth auth;

    /**
     * Construction
     */
    public ProfilePresenter(final ProfilePresenterListener listener) {
        this.listener = listener;
        this.service = new FirebaseService();
        this.eventsInteractor = new EventsInteractor(this);
        this.auth = FirebaseAuth.getInstance();
        
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (auth.getCurrentUser() == null) {
                    listener.signOut();
                }
            }
        });
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
                if (response.body() != null){
                    HashMap<String, EventInfo> events = response.body().getEvents();
                    if (events != null) {
                        getEventDetails(new ArrayList<>(events.keySet()));
                    }
                    listener.profileInfoReady(response.body());
                } else {
                    listener.presentError("User was null");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                    listener.presentError(t.getMessage());
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
    public void presentError(String error) {
        listener.presentError(error);
    }

    /**
     * Contract / Listener
     */
    public interface ProfilePresenterListener {

        void profileInfoReady(User user);

        void eventReady(EventDetail event, String eventId);

        void presentError(String error);

        void loadingState(Boolean show);

        void signOut();
    }
}
