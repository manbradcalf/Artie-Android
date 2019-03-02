package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benmedcalf on 1/13/18.
 */

public class UserDetailPresenter implements BasePresenter {
    private final UserDetailPresenterListener listener;
    private final CompositeDisposable compositeDisposable;
    private String userId;

    /**
     * Constructor
     */
    public UserDetailPresenter(String userId, UserDetailPresenterListener listener) {
        this.userId = userId;
        this.listener = listener;
        this.compositeDisposable = new CompositeDisposable();
    }

    /**
     * Methods
     */
    private void loadUserInfoWithEvents(final String userId) {

        compositeDisposable.add(
                FirebaseService.getAPI().getUserDetails(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(throwable -> listener.presentError(throwable.getMessage()))
                        .flatMap(user -> {

                            // Notify view that user details have been returned
                            listener.displayUserInfo(user, userId);

                            //TODO: Fix NPE here when loading a user detail
                            return Flowable.fromIterable(user.getEvents().entrySet());
                        })

                        // Map the eventInviteInfos into eventDetails
                        .map(stringEventInviteInfoEntry ->
                                FirebaseService
                                        .getAPI()
                                        .getEventData(stringEventInviteInfoEntry.getKey())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(eventDetail -> listener.displayUserEvent(eventDetail, stringEventInviteInfoEntry.getKey()),
                                                throwable -> listener.presentError(throwable.getMessage())))
                        .subscribe());
    }

    public void addContactToUser(String contactId, String userId) {

        HashMap<String, Boolean> request = new HashMap<>();
        request.put(contactId, true);

        Flowable.just(FirebaseService.getAPI()
                .addContactToUser(request, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        stringBooleanHashMap -> {
                            listener.presentSuccess("Added contact!");
                        },
                        throwable -> listener.presentError(throwable.getMessage())))
                .subscribe();
    }


    @Override
    public void subscribe() {
        loadUserInfoWithEvents(userId);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }


    /**
     * Contract / Listener
     */
    public interface UserDetailPresenterListener {
        void displayUserInfo(User userInfo, String userId);

        void displayUserEvent(EventDetail event, String eventId);

        void displayLoadingState();

        void presentError(String message);

        void presentSuccess(String message);

    }
}
