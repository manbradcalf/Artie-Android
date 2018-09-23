package com.bookyrself.bookyrself.interactors;

import com.bookyrself.bookyrself.models.SerializedModels.User.MiniEvent;
import com.bookyrself.bookyrself.services.FirebaseService;

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

    public void addEventToUser(MiniEvent miniEvent, String userId, String eventId) {
        service.getAPI().addEventToUser(miniEvent, userId, eventId).enqueue(new Callback<MiniEvent>() {
            @Override
            public void onResponse(Call<MiniEvent> call, Response<MiniEvent> response) {
                listener.eventAddedToUserSuccessfully();
            }

            @Override
            public void onFailure(Call<MiniEvent> call, Throwable t) {

            }
        });
    }

    public interface UsersInteractorListener {
        void eventAddedToUserSuccessfully();
    }
}
