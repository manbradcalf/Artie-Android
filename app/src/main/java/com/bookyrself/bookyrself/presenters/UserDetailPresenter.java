package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.data.UsersInteractor;
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benmedcalf on 1/13/18.
 */

public class UserDetailPresenter implements UsersInteractor.UserDetailInteractorListener {
    private final UserDetailPresenterListener listener;
    private final UsersInteractor usersInteractor;
    private final CompositeDisposable compositeDisposable;
    private final String userId;

    /**
     * Constructor
     */

    public UserDetailPresenter(String userId, UserDetailPresenterListener listener) {
        this.listener = listener;
        this.usersInteractor = new UsersInteractor(this);
        this.compositeDisposable = new CompositeDisposable();
        this.userId = userId;
    }

    /**
     * Methods
     */
    public void loadUserInfoWithEvents(final String userId) {

        compositeDisposable.add(
                usersInteractor
                        .getUserDetails(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(throwable -> {
                            listener.presentError(throwable.getMessage());
                        })
                        .flatMap(user -> {

                            // Notify view that user details have been returned
                            listener.displayUserInfo(user, userId);

                            return Flowable.fromIterable(user.getEvents().entrySet());
                        })

                        // Map the event infos into event details
                        .map(stringEventInviteInfoEntry ->
                                FirebaseService
                                        .getAPI()
                                        .getEventData(stringEventInviteInfoEntry.getKey())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .doOnNext(eventDetail -> {
                                            // notify the view
                                            listener.displayUserEvents(eventDetail, stringEventInviteInfoEntry.getKey());
                                        })
                                        .doOnError(throwable -> { listener.presentError(throwable.getMessage());
                                        })
                                        .subscribe())
                        .subscribe());
    }

    @Override
    public void contactSuccessfullyAdded() {
        listener.presentSuccess("Added contact!");
    }

    public void addContactToUser(String contactId, String userId) {
        usersInteractor.addContactToUser(contactId, userId);
    }

    @Override
    public void presentError(String error) {

    }

    @Override
    public void userDetailReturned(User user, String userId) {

    }

    @Override
    public void subscribe() {
        loadUserInfoWithEvents(userId);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.dispose();
    }


    /**
     * Contract / Listener
     */
    public interface UserDetailPresenterListener {
        void displayUserInfo(User userInfo, String userId);

        void displayUserEvents(EventDetail event, String eventId);

        void displayLoadingState();

        void presentError(String message);

        void presentSuccess(String message);

    }
}
