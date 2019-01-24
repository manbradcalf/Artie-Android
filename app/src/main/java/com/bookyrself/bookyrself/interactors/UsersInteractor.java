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
    private UserDetailInteractorListener userDetailInteractorListener;

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

    public UsersInteractor(UserDetailInteractorListener listener) {
        this.userDetailInteractorListener = listener;
        service = new FirebaseService();
    }

    public void addEventToUser(EventInfo eventInfo, final String userId, final String eventId) {
        service.getAPI().addEventToUser(eventInfo, userId, eventId).enqueue(new Callback<EventInfo>() {
            @Override
            public void onResponse(Call<EventInfo> call, Response<EventInfo> response) {
                Log.i("UsersInteractor", String.format("User %s successfully invited to Event %s", userId, eventId));
            }

            @Override
            public void onFailure(Call<EventInfo> call, Throwable t) {

            }
        });
    }

    public void getUserInvites(final String userId) {
        service.getAPI().getUsersEventInvites(userId).enqueue(new Callback<HashMap<String, EventInfo>>() {
            @Override
            public void onResponse(Call<HashMap<String, EventInfo>> call, Response<HashMap<String, EventInfo>> response) {
                if (response.body() != null) {
                    for (Map.Entry<String, EventInfo> entry : response.body().entrySet()) {
                        // If invite is not accepted, tell the listener
                        //TODO: If all invites are accepted, how  to show empty state?
                        if (!entry.getValue().getIsInviteAccepted() && !entry.getValue().getIsHost()) {
                            //TODO: Rename eventIdOfEvent...
                            usersEventInvitesInteractorListener.eventIdOfEventWithPendingInvitesReturned(entry.getKey());
                        }
                    }
                } else {
                    //TODO: Find a better solution
                    usersEventInvitesInteractorListener.noInvitesReturnedForUser();
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
                if (response.body() != null) {
                    //TODO: How to determine which listener to use? This is fukt
                    if (userDetailInteractorListener != null) {
                        userDetailInteractorListener.userDetailReturned(response.body(), userId);
                    } else {
                        usersInteractorListener.userDetailReturned(response.body(), userId);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    public void addContactToUser(String userId, String contactId) {
        HashMap<String, Boolean> request = new HashMap<>();
        request.put(contactId, true);
        service.getAPI().addContactToUser(request, userId).enqueue(new Callback<HashMap<String, Boolean>>() {
            @Override
            public void onResponse(Call<HashMap<String, Boolean>> call, Response<HashMap<String, Boolean>> response) {
                userDetailInteractorListener.contactSuccessfullyAdded();
            }

            @Override
            public void onFailure(Call<HashMap<String, Boolean>> call, Throwable t) {
                userDetailInteractorListener.presentError(t.getMessage());
            }
        });
    }

    public interface UsersInteractorListener {

        void userDetailReturned(User user, String userId);

        void presentError(String error);
    }

    public interface UsersEventInvitesInteractorListener {
        void eventIdOfEventWithPendingInvitesReturned(String eventId);

        void presentError(String error);

        void noInvitesReturnedForUser();
    }

    public interface UserDetailInteractorListener extends UsersInteractorListener {
        void contactSuccessfullyAdded();
    }
}
