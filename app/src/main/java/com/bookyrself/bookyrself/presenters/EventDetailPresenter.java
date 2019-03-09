package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.MiniUser;
import com.bookyrself.bookyrself.data.ResponseModels.User.EventInviteInfo;
import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.AbstractMap;
import java.util.NoSuchElementException;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benmedcalf on 11/22/17.
 */

public class EventDetailPresenter implements BasePresenter {


    private final EventDetailPresenterListener listener;
    private CompositeDisposable compositeDisposable;
    private String eventId;

    /**
     * Constructor
     */
    public EventDetailPresenter(EventDetailPresenterListener listener, String eventId) {
        this.listener = listener;
        this.compositeDisposable = new CompositeDisposable();
        this.eventId = eventId;
    }

    /**
     * Methods
     */
    private void getEventDetailData(String eventId) {

        compositeDisposable.add(

                FirebaseService.getAPI()
                        .getEventData(eventId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(listener::showEventData)

                        // Get the userIds of invited users
                        .flatMap(eventDetail -> Flowable.fromIterable(eventDetail.getUsers().entrySet()))

                        // Get user details for each user
                        .map(stringBooleanEntry ->
                                Flowable.just(
                                        FirebaseService.getAPI().getUserDetails(stringBooleanEntry.getKey())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .firstOrError()
                                                .subscribe(
                                                        user -> {
                                                            // Show the view the minified user
                                                            MiniUser miniUser = minifyUserDetailsForEventDetailDisplay(stringBooleanEntry.getKey(), user);
                                                            listener.showInvitedUser(new AbstractMap.SimpleEntry<>(stringBooleanEntry.getKey(), miniUser));
                                                        },
                                                        throwable -> {

                                                            // Present error
                                                            if (throwable instanceof NoSuchElementException) {
                                                                listener.presentError(String.format("We were unable to find a user with id %s", stringBooleanEntry.getKey()));
                                                            } else if (throwable.getMessage() != null) {
                                                                listener.presentError(throwable.getMessage());
                                                            } else {
                                                                //TODO: Think of something better
                                                                listener.presentError("Whoosp!");
                                                            }
                                                        }
                                                )))
                        .subscribe(
                                disposableFlowable -> {},
                                throwable -> listener.presentError(throwable.getMessage())));
    }

    private MiniUser minifyUserDetailsForEventDetailDisplay(String userId, User user) {
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
                    status = "Not attending";
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

    @Override
    public void subscribe() {
        getEventDetailData(eventId);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }


    /**
     * Contract / Listener
     */
    public interface EventDetailPresenterListener {
        void showEventData(EventDetail data);

        void showInvitedUser(AbstractMap.SimpleEntry<String, MiniUser> user);

        void presentError(String message);
    }
}
