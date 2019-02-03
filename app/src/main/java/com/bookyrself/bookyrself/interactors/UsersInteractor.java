package com.bookyrself.bookyrself.interactors;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bookyrself.bookyrself.models.SerializedModels.User.EventInviteInfo;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersInteractor {
    private FirebaseService service;
    private UsersInteractorListener usersInteractorListener;
    private UsersEventInvitesInteractorListener usersEventInvitesInteractorListener;
    private UserDetailInteractorListener userDetailInteractorListener;
    private UsersEventsInteractorListener usersEventsInteractorListener;

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

    public UsersInteractor(UsersEventsInteractorListener listener) {
        this.usersEventsInteractorListener = listener;
        service = new FirebaseService();
    }

    public void addEventToUser(EventInviteInfo eventInviteInfo, final String userId, final String eventId) {
        service.getAPI().addEventToUser(eventInviteInfo, userId, eventId).enqueue(new Callback<EventInviteInfo>() {
            @Override
            public void onResponse(Call<EventInviteInfo> call, Response<EventInviteInfo> response) {
                Log.i("UsersInteractor", String.format("User %s successfully invited to Event %s", userId, eventId));
            }

            @Override
            public void onFailure(Call<EventInviteInfo> call, Throwable t) {

            }
        });
    }

    public void getUserInvites(final String userId) {
        service.getAPI().getUsersEventInvites(userId).enqueue(new Callback<HashMap<String, EventInviteInfo>>() {
            @Override
            public void onResponse(Call<HashMap<String, EventInviteInfo>> call, Response<HashMap<String, EventInviteInfo>> response) {

                if (response.body() != null) {
                    List<String> eventIdsOfPendingInvites = getEventIdsOfPendingInvites(response.body());
                    if (eventIdsOfPendingInvites.size() != 0) {
                        usersEventInvitesInteractorListener.eventIdsOfEventsWithPendingInvitesReturned(eventIdsOfPendingInvites);
                    } else {
                        usersEventInvitesInteractorListener.noInvitesReturnedForUser();
                    }
                } else {
                    usersEventInvitesInteractorListener.noInvitesReturnedForUser();
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, EventInviteInfo>> call, Throwable t) {
                Log.e("getUserInvites:", t.getMessage());
            }
        });
    }

    private List <String> getEventIdsOfPendingInvites(HashMap<String, EventInviteInfo> eventsMap) {
        List<String> eventIds = new ArrayList<>();

        for (Map.Entry<String, EventInviteInfo> entry : eventsMap.entrySet()) {
            // If the required event invite information exists
            if (entry.getValue().getIsInviteRejected() != null && entry.getValue().getIsInviteAccepted() != null
                    && entry.getValue().getIsHost() != null) {
                // If the user hasn't responded to the invite (hasn't accepted or rejected invites)
                if (!entry.getValue().getIsInviteAccepted() && !entry.getValue().getIsInviteRejected()
                        && !entry.getValue().getIsHost()) {
                    eventIds.add(entry.getKey());
                }
            }
        }
        return eventIds;
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
                } else {
                    usersInteractorListener.presentError(String.format("User %s doesn't exist!", userId));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                usersInteractorListener.presentError(t.getMessage());
            }
        });
    }

    public void createUser(User user, final String uid) {
        service.getAPI().addUser(user, uid).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // response
                usersInteractorListener.userDetailReturned(response.body(), uid);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // failure
            }
        });
    }

    public void patchUser(User user, final String uID) {
        service.getAPI().patchUser(user, uID).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // response
                usersInteractorListener.userDetailReturned(response.body(), uID);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // failure
            }
        });
    }

    public void getUserEvents(String userId) {
        service.getAPI().getUsersEventInfo(userId).enqueue(new Callback<HashMap<String, EventInviteInfo>>() {
            @Override
            public void onResponse(@NonNull Call<HashMap<String, EventInviteInfo>> call, @NonNull Response<HashMap<String, EventInviteInfo>> response) {
                if (response.body() != null) {
                    usersEventsInteractorListener.eventsReturned(new ArrayList<>(response.body().keySet()));
                } else {
                    usersEventsInteractorListener.noEventsReturned();
                }

            }

            @Override
            public void onFailure(Call<HashMap<String, EventInviteInfo>> call, Throwable t) {
                Log.e("events presenter:", t.getMessage());
                usersEventsInteractorListener.presentError(t.getMessage());
            }
        });
    }

    public interface UsersInteractorListener {

        void userDetailReturned(User user, String userId);

        void presentError(String error);

    }

    public interface UserDetailInteractorListener extends UsersInteractorListener {
        void contactSuccessfullyAdded();
    }

    public interface UsersEventInvitesInteractorListener {

        void eventIdsOfEventsWithPendingInvitesReturned(List<String> eventIds);

        void presentError(String error);

        void noInvitesReturnedForUser();

    }

    public interface UsersEventsInteractorListener {
        void eventsReturned(List<String> eventIds);

        void presentError(String error);

        void noEventsReturned();
    }
}
