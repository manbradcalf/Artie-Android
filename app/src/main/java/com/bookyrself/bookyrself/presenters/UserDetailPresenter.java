package com.bookyrself.bookyrself.presenters;

import android.support.annotation.NonNull;

import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers._source;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Constructor
     */

    public UserDetailPresenter(UserDetailPresenterListener listener) {
        this.mListener = listener;
        this.mService = new FirebaseService();
    }

    /**
     * Methods
     */
    public void getUserInfo(final String id) {
        mService.getAPI().getUserDetails(id).enqueue(new Callback<_source>() {
            @Override
            public void onResponse(@NonNull Call<_source> call, @NonNull Response<_source> response) {
                if (response.body() != null) {
                    mListener.userInfoReady(response.body());
                } else {
                    mListener.presentError();
                }

            }

            @Override
            public void onFailure(Call<_source> call, Throwable t) {

            }
        });
    }

    public void addContact(final String userId, final String contactId) {
        mService.getAPI().getUserContacts(userId).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                long size = 0;
                if (response.body() != null) {
                    size = response.body().size();
                }

                actuallyAddContact(userId, contactId, size);
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                mListener.presentError();
            }
        });
    }

    private void actuallyAddContact(String userId, String contactId, long index) {
//        RequestBody request = RequestBody.create(MediaType.parse("text/plain"), contactId);
        Map<String, String> request = new HashMap<>();
        request.put(Long.toString(index), contactId);
        mService.getAPI().addContactToUser(request, userId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                mListener.presentSuccess("added to contacts!");
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {

            }
        });
    }

    /**
     * Contract / Listener
     */
    public interface UserDetailPresenterListener {
        void userInfoReady(_source response);

        void presentError();

        void loadingState();

        void emailUser();

        void presentSuccess(String message);
    }
}
