package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.data.Events.EventsRepo;
import com.bookyrself.bookyrself.data.Profile.ProfileRepo;
import com.bookyrself.bookyrself.data.ProfileInteractor;
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.bookyrself.bookyrself.views.MainActivity;

import java.util.NoSuchElementException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ProfileFragmentPresenter implements BasePresenter, ProfileInteractor.ProfileInteractorListener {

    private final ProfilePresenterListener listener;
    private final ProfileInteractor profileInteractor;
    private final ProfileRepo profileRepo;
    private CompositeDisposable compositeDisposable;
    private EventsRepo eventsRepo;
    private String userId;

    /**
     * Construction
     */
    public ProfileFragmentPresenter(final ProfilePresenterListener listener, String userId) {
        this.listener = listener;
        this.userId = userId;
        this.eventsRepo = MainActivity.getEventsRepo();
        this.profileRepo = MainActivity.getProfileRepo();
        this.profileInteractor = new ProfileInteractor(this);
        this.compositeDisposable = new CompositeDisposable();
    }

    /**
     * Methods
     */
    public void createUser(User user, final String userId) {
        profileInteractor.createUser(user, userId);
    }

    public void patchUser(User user, final String userId) {
        profileRepo.updateProfileInfo(userId, user)
                .subscribe();
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
                .subscribe(stringEventDetailPair -> listener.eventReady(stringEventDetailPair.first, stringEventDetailPair.second),
                        throwable -> listener.presentError(throwable.getMessage())));
    }

    @Override
    public void profileReturned(User user, String userId) {
        //TODO: Delete this once I RXify create and patch user
    }

    public void presentError(String error) {
        listener.presentError(error);
    }

    @Override
    public void subscribe() {
        loadProfile();
        loadEventDetails();
    }

    @Override
    public void unsubscribe() {

    }


    /**
     * Contract / Listener
     */
    public interface ProfilePresenterListener {

        void profileInfoReady(String userId, User user);

        void eventReady(String eventId, EventDetail event);

        void presentError(String error);
    }
}
