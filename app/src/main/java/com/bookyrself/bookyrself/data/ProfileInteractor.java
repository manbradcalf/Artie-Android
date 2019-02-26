package com.bookyrself.bookyrself.data;

import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileInteractor {
    private ProfileInteractorListener profileInteractorListener;

    /**
     * Constructors
     */
    public ProfileInteractor(ProfileInteractorListener listener) {
        this.profileInteractorListener = listener;
    }

    public void createUser(User user, final String uid) {
        FirebaseService.getAPI().addUser(user, uid).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // response
                profileInteractorListener.profileReturned(response.body(), uid);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // failure
                profileInteractorListener.presentError(t.getMessage());
            }
        });
    }

    /**
     * Interfaces
     */
    public interface ProfileInteractorListener {

        void profileReturned(User user, String userId);

        void presentError(String error);

    }
}
