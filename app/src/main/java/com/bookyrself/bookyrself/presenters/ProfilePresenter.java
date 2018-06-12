package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.models.SearchResponseUsers.Hit;
import com.bookyrself.bookyrself.models.SearchResponseUsers._source;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.bookyrself.bookyrself.models.User.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilePresenter {
    private final ProfilePresenterListener listener;
    private final FirebaseService service;

    /**
     * Contract / Listener
     */
    public interface ProfilePresenterListener {

        void profileInfoReady(_source response);

        void presentToast(String message);

        void loadingState();

        void successfulAuth();
    }

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
    public void createUser(_source user, String UID) {
        service.getAPI().addUser(user, UID).enqueue(new Callback<_source>() {
            @Override
            public void onResponse(Call<_source> call, Response<_source> response) {
                // response
                listener.profileInfoReady(response.body());
            }

            @Override
            public void onFailure(Call<_source> call, Throwable t) {
                // failure
            }
        });
    }

    public void patchUser(_source user, String UID) {
        service.getAPI().patchUser(user, UID).enqueue(new Callback<_source>() {
            @Override
            public void onResponse(Call<_source> call, Response<_source> response) {
                // response
                listener.profileInfoReady(response.body());
            }

            @Override
            public void onFailure(Call<_source> call, Throwable t) {
                // failure
            }
        });
    }

    public void getUser(String UID) {
        service.getAPI().getUserDetails(UID).enqueue(new Callback<_source>() {
            @Override
            public void onResponse(Call<_source> call, Response<_source> response) {
                listener.profileInfoReady(response.body());
            }

            @Override
            public void onFailure(Call<_source> call, Throwable t) {

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


}
