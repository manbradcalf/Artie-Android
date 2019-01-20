package com.bookyrself.bookyrself.interactors;

import android.util.Log;

import com.bookyrself.bookyrself.models.SerializedModels.User.EventInfo;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersInteractor {
    private FirebaseService service;
    private UsersInteractorListener usersInteractorListener;
    private UsersEventInvitesInteractorListener usersEventInvitesInteractorListener;

    /**
     * Constructors
     */
    public UsersInteractor(UsersInteractorListener listener) {
        this.usersInteractorListener = listener;
        service = new FirebaseService();
    }

    public UsersInteractor(UsersEventInvitesInteractorListener listener) {
        this.usersEventInvitesInteractorListener = listener;
        service = new FirebaseService();
    }

    public void addEventToUser(EventInfo eventInfo, String userId, String eventId) {
        service.getAPI().addEventToUser(eventInfo, userId, eventId).enqueue(new Callback<EventInfo>() {
            @Override
            public void onResponse(Call<EventInfo> call, Response<EventInfo> response) {
                usersInteractorListener.eventAddedToUserSuccessfully();
            }

            @Override
            public void onFailure(Call<EventInfo> call, Throwable t) {

            }
        });
    }

    public void getUserInvites(final String userid) {
        service.getAPI().getUsersEventInvites(userid).enqueue(new Callback<HashMap<String, EventInfo>>() {
            @Override
            public void onResponse(Call<HashMap<String, EventInfo>> call, Response<HashMap<String, EventInfo>> response) {
                if (response.body() != null) {
                    for (Map.Entry<String, EventInfo> entry : response.body().entrySet()) {
                        // If invite is not accepted, tell the listener
                        if (!entry.getValue().getIsInviteAccepted() && !entry.getValue().getIsHost()) {
                            usersEventInvitesInteractorListener.eventIdOfEventWithPendingInvitesReturned(entry.getKey());
                        }
                    }
                } else {
                    Log.e("getUserInvites:", "response is null");
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, EventInfo>> call, Throwable t) {
                Log.e("getUserInvites:", t.getMessage());
            }
        });
    }

    public void getUserDetails(final String userId) {
        service.getAPI().getUserDetails(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                //TODO: Will the userId here always be the one that made the call?
                usersInteractorListener.userDetailReturned(response.body(), userId);
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

    public interface UsersEventInvitesInteractorListener {
        void eventIdOfEventWithPendingInvitesReturned(String eventId);

    }
}
