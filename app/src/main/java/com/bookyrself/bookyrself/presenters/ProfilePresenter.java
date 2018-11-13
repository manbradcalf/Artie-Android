package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilePresenter {
    private final ProfilePresenterListener listener;
    private final FirebaseService service;

    /**
     * Construction
     */
    public ProfilePresenter(ProfilePresenterListener listener) {
        this.listener = listener;
        this.service = new FirebaseService();
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
                listener.profileInfoReady(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    public void uploadPhoto() {

    }

    public void updateCityState() {

    }

    public void updateTags() {

    }

    public void updateWebsite() {

    }

    public void viewContacts() {

    }

    public void updateBio() {

    }

    /**
     * Contract / Listener
     */
    public interface ProfilePresenterListener {

        void profileInfoReady(User response);

        void presentToast(String message);

        void loadingState();

        void successfulAuth();
    }


}
