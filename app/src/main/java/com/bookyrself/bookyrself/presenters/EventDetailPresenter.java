package com.bookyrself.bookyrself.presenters;

import android.support.annotation.NonNull;

import com.bookyrself.bookyrself.data.UsersInteractor;
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.MiniUser;
import com.bookyrself.bookyrself.data.ResponseModels.User.EventInviteInfo;
import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 11/22/17.
 */

public class EventDetailPresenter implements UsersInteractor.UsersInteractorListener {


    private final EventDetailPresenterListener mListener;
    private final List<MiniUser> mMiniUsers;
    private String eventId;

    /**
     * Constructor
     */
    public EventDetailPresenter(EventDetailPresenterListener listener) {
        this.mListener = listener;
        this.mMiniUsers = new ArrayList<>();
    }

    /**
     * Methods
     */
    public void getEventDetailData(String eventId) {
        this.eventId = eventId;
        mListener.showProgressbar(true);
        FirebaseService.getAPI().getEventData(eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(eventDetail -> {
                    // do something
                })
                .doOnError(throwable -> mListener.presentError(throwable.getMessage()))
                .subscribe();
    }

    //TODO: Will the final "id" variable here be stuck on the first id assigned?
    public void getUserThumbUrl(final String id) {
        FirebaseService.getAPI().getUserThumbUrl(id)
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
        miniUser.setAttendingStatus(getAttendingStatus(user.getEvents().get(eventId)));
        return miniUser;
    }

    private String getAttendingStatus(EventInviteInfo eventInviteInfo) {
        String status = "Invited";
        if (eventInviteInfo != null) {
            if (eventInviteInfo.getIsInviteRejected() != null) {
                if (eventInviteInfo.getIsInviteRejected()) {
                    status = "Rejected";
                }
            }

            if (eventInviteInfo.getIsInviteAccepted() != null) {
                if (eventInviteInfo.getIsInviteAccepted()) {
                    status = "Attending";
                }
            }
        }
        return status;
    }

    public void eventDetailReturned(EventDetail eventDetail, String eventId) {

//        mEventDetail = eventDetail;
//
//        // If users exist, iterate through and retrieve their details
//        if (eventDetail.getUsers() != null) {
//            mUserCount = eventDetail.getUsers().keySet().size();
//            for (String userId : eventDetail.getUsers().keySet()) {
//                mUsersInteractor.getUserDetails(userId);
//            }
//        } else {
//            presentError("Event " + eventId + "has no users");
//            Log.e("EventDetailPresenter", "Event " + eventId + "has no users");
//        }
    }

    @Override
    public void presentError(String error) {
        // Surface the error sent from the interactor to the activity
        mListener.presentError(error);
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
