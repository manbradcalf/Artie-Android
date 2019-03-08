package com.bookyrself.bookyrself.presenters;

import android.content.Context;
import android.util.Log;

import com.bookyrself.bookyrself.data.Events.EventsRepo;
import com.bookyrself.bookyrself.data.Profile.ProfileRepo;
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.bookyrself.bookyrself.views.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.NoSuchElementException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ProfileFragmentPresenter implements BasePresenter {

    private final ProfilePresenterListener listener;
    private final ProfileRepo profileRepo;
    private CompositeDisposable compositeDisposable;
    private EventsRepo eventsRepo;
    private String userId;

    /**
     * Construction
     */
    public ProfileFragmentPresenter(final ProfilePresenterListener listener, Context context) {
        this.listener = listener;
        this.eventsRepo = MainActivity.getEventsRepo(context);
        this.profileRepo = MainActivity.getProfileRepo();
        this.compositeDisposable = new CompositeDisposable();
    }

    /**
     * Methods
     */
    public void updateUser(User user, final String userId) {
        compositeDisposable.add(
                FirebaseService.getAPI().addUser(user, userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(userResponse -> listener.profileInfoReady(userId, user),
                                throwable -> listener.presentError(throwable.getMessage())));
    }

    private void loadProfile() {
        compositeDisposable.add(

                profileRepo.getProfileInfo(userId).subscribe(
                        user -> {
                            // Notify view the profile is ready
                            listener.profileInfoReady(userId, user);
                        },
                        throwable -> {
                            if (throwable instanceof NoSuchElementException) {
                                listener.presentError("We were unable to find your profile");
                            } else {
                                listener.presentError(throwable.getMessage());
                            }
                        }));
    }

    private void loadEventDetails() {
        compositeDisposable.add(eventsRepo.getAllEvents(userId)
                .subscribe(
                        // Success
                        stringEventDetailEntry ->
                                listener.eventReady(
                                        stringEventDetailEntry.getKey(), stringEventDetailEntry.getValue()),

                        // Error
                        throwable -> {
                            if (throwable instanceof NoSuchElementException) {
                                Log.e(getClass().getName(), String.format("User %s has no events", userId));
                            } else {
                                listener.presentError(throwable.getMessage());
                            }
                        }));
    }

    @Override
    public void subscribe() {
        if (FirebaseAuth.getInstance().getUid() != null) {
            userId = FirebaseAuth.getInstance().getUid();
            loadProfile();
            loadEventDetails();
        } else {
            listener.showSignedOutEmptyState();
        }
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }


    /**
     * PresenterListener Definition
     */
    public interface ProfilePresenterListener extends BasePresenterListener {

        void profileInfoReady(String userId, User user);

        void eventReady(String eventId, EventDetail event);

        void presentError(String error);
    }
}
