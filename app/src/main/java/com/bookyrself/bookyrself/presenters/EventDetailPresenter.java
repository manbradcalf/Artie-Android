package com.bookyrself.bookyrself.presenters;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.interactors.UsersInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.MiniUser;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 11/22/17.
 */

public class EventDetailPresenter implements EventsInteractor.EventsInteractorListener, UsersInteractor.UsersInteractorListener {

    private final EventsInteractor mEventsInteractor;
    private final UsersInteractor mUsersInteractor;
    private final EventDetailPresenterListener mListener;
    private final FirebaseService mFirebaseService;
    private final List<MiniUser> mMiniUsers;
    private EventDetail mEventDetail;
    private Integer mUserCount;

    /**
     * Constructor
     */
    public EventDetailPresenter(EventDetailPresenterListener listener) {
        this.mListener = listener;
        this.mFirebaseService = new FirebaseService();
        this.mEventsInteractor = new EventsInteractor(this);
        this.mUsersInteractor = new UsersInteractor(this);
        this.mMiniUsers = new ArrayList<>();
    }

    /**
     * Methods
     */
    public void getEventDetailData(String eventId) {
        mListener.showProgressbar(true);
        mEventsInteractor.getEventDetail(eventId);
    }

    //TODO: Will the final "id" variable here be stuck on the first id assigned?
    public void getUserThumbUrl(final String id) {
        mFirebaseService.getAPI().getUserThumbUrl(id)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        String data = response.body();
                        mListener.userThumbReady(data, id);
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                    }
                });
    }

    private MiniUser minifyUserDetailsForEventDetailDisplay(User user, String userId) {
        MiniUser miniUser = new MiniUser();
        miniUser.setCitystate(user.getCitystate());
        miniUser.setUrl(user.getUrl());
        miniUser.setUsername(user.getUsername());
        miniUser.setUserId(userId);
        return miniUser;
    }

    @Override
    public void eventDetailReturned(EventDetail eventDetail, String eventId) {

        mEventDetail = eventDetail;

        // If users exist, iterate through and retrieve their details
        if (eventDetail.getUsers() != null) {
            mUserCount = eventDetail.getUsers().keySet().size();
            for (String userId : eventDetail.getUsers().keySet()) {
                mUsersInteractor.getUserDetails(userId);
            }
        } else {
            presentError("Event " + eventId + "has no users");
            Log.e("EventDetailPresenter", "Event " + eventId + "has no users");
        }
    }

    @Override
    public void usersEventsReturned(List<Event> events) {

    }

    @Override
    public void eventCreated(String eventId, List<String> usersToInvite) {

    }

    @Override
    public void presentError(String error) {
        // Surface the error sent from the interactor to the activity
        mListener.presentError(error);
    }

    @Override
    public void oneEventDetailOfManyReturned(EventDetail body, List<String> eventIds, String
            eventId) {

    }

    @Override
    public void eventAddedToUserSuccessfully() {

    }

    @Override
    public void userDetailReturned(User user, String userId) {
        if (user != null) {
            MiniUser miniUser = minifyUserDetailsForEventDetailDisplay(user, userId);
            mMiniUsers.add(miniUser);
            if (mUserCount.equals(mMiniUsers.size())) {
                mListener.eventDataResponseReady(mEventDetail, mMiniUsers);
            }
        } else {
            mListener.presentError(String.format("User %s was null", userId));
        }
    }

    /**
     * Contract / Listener
     */
    public interface EventDetailPresenterListener {
        void eventDataResponseReady(EventDetail data, List<MiniUser> miniUsers);

        void showProgressbar(Boolean bool);

        void userThumbReady(String response, String id);

        void presentError(String message);
    }
}
