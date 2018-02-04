package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.models.SearchResponseUsers.SearchResponseUsers;
import com.bookyrself.bookyrself.models.SearchResponseUsers._source;
import com.bookyrself.bookyrself.services.FirebaseService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 1/13/18.
 */

public class UserDetailPresenter {
    private final UserDetailPresenterListener mListener;
    private final FirebaseService mService;

    /**
     * Contract / Listener
     */
    public interface UserDetailPresenterListener {
        void userInfoReady(_source response);

    }

    /**
     * Constructor
     */

    public UserDetailPresenter(UserDetailPresenterListener listener) {
        this.mListener = listener;
        this.mService = new FirebaseService();
    }

    /**
     * Methods
     */
    public void getUserInfo(String id) {
        mService.getAPI().getUserDetails(id).enqueue(new Callback<_source>() {
            @Override
            public void onResponse(Call<_source> call, Response<_source> response) {
                mListener.userInfoReady(response.body());
            }

            @Override
            public void onFailure(Call<_source> call, Throwable t) {

            }
        });
    }
}
