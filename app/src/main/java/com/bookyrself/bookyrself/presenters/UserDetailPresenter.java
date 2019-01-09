package com.bookyrself.bookyrself.presenters;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 1/13/18.
 */

public class UserDetailPresenter implements EventsInteractor.EventsInteractorListener {
    private final UserDetailPresenterListener mListener;
    private final FirebaseService mService;
    private final EventsInteractor eventsInteractor;
    /**
     * Constructor
     */

    public UserDetailPresenter(UserDetailPresenterListener listener) {
        this.mListener = listener;
        this.mService = new FirebaseService();
        this.eventsInteractor = new EventsInteractor(this);
    }

    /**
     * Methods
     */
    //TODO: Should this be in @UsersInteractor
    public void getUserInfo(final String id) {
        mService.getAPI().getUserDetails(id).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                //TODO: I hate all of these null checks. Can I fix?
                if (response.body() != null) {
                    mListener.userInfoReady(response.body());
                    if (response.body().getEvents() != null)
                        eventsInteractor.getMultipleEventDetails(new ArrayList<>(response.body().getEvents().keySet()));
                } else {
                    mListener.presentError(id);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                mListener.presentError(t.getMessage());
                Log.e("getUserInfo:", String.format("userId %s is null", id));
            }
        });
    }

    public void addContactToUser(String userId, String contactId) {
        HashMap<String, Boolean> request = new HashMap<>();
        request.put(contactId, true);
        mService.getAPI().addContactToUser(request, userId).enqueue(new Callback<HashMap<String, Boolean>>() {
            @Override
            public void onResponse(Call<HashMap<String, Boolean>> call, Response<HashMap<String, Boolean>> response) {
                mListener.presentSuccess("added to contacts!");
            }

            @Override
            public void onFailure(Call<HashMap<String, Boolean>> call, Throwable t) {
                presentError(t.getMessage());
            }
        });
    }

    @Override
    public void eventDetailReturned(EventDetail event, String eventId) {
        mListener.usersEventReturned(event, eventId);
    }

    @Override
    public void eventCreated(String eventId, List<String> usersToInvite) {

    }

    @Override
    public void presentError(String error) {

    }


    /**
     * Contract / Listener
     */
    public interface UserDetailPresenterListener {
        void userInfoReady(User userInfo);

        void presentError(String message);

        void loadingState();

        void emailUser();

        void presentSuccess(String message);

        void usersEventReturned(EventDetail event, String eventId);
        }
}
