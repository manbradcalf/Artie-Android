package com.bookyrself.bookyrself.data;

import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersInteractor {
    private UsersInteractorListener usersInteractorListener;

    /**
     * Constructors
     */
    public UsersInteractor(UsersInteractorListener listener) {
        this.usersInteractorListener = listener;
    }

    public void createUser(User user, final String uid) {
        FirebaseService.getAPI().addUser(user, uid).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // response
                usersInteractorListener.userDetailReturned(response.body(), uid);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // failure
                usersInteractorListener.presentError(t.getMessage());
            }
        });
    }

    public void patchUser(User user, final String uID) {
        FirebaseService.getAPI().patchUser(user, uID).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // response
                usersInteractorListener.userDetailReturned(response.body(), uID);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // failure
                usersInteractorListener.presentError(t.getMessage());
            }
        });
    }



    /**
     * Interfaces
     */
    public interface UsersInteractorListener {

        void userDetailReturned(User user, String userId);

        void presentError(String error);

    }
}
