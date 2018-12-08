package com.bookyrself.bookyrself.interactors;

import com.bookyrself.bookyrself.models.SerializedModels.User.EventInfo;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersInteractor {
    private FirebaseService service;
    private UsersInteractorListener listener;

    /**
     * Constructor
     */
    public UsersInteractor(UsersInteractorListener listener) {
        this.listener = listener;
        service = new FirebaseService();
    }

    public void addEventToUser(EventInfo eventInfo, String userId, String eventId) {
        service.getAPI().addEventToUser(eventInfo, userId, eventId).enqueue(new Callback<EventInfo>() {
            @Override
            public void onResponse(Call<EventInfo> call, Response<EventInfo> response) {
                listener.eventAddedToUserSuccessfully();
            }

            @Override
            public void onFailure(Call<EventInfo> call, Throwable t) {

            }
        });
    }

    public void getUserDetails(final String userId) {
        service.getAPI().getUserDetails(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                //TODO: Will the userId here always be the one that made the call?
                listener.userDetailReturned(response.body(), userId);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    public interface UsersInteractorListener {
        void eventAddedToUserSuccessfully();

        void userDetailReturned(User user, String userId);
    }
}
