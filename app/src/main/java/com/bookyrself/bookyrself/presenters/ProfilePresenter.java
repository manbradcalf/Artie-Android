package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.interactors.UsersInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.User.EventInfo;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilePresenter implements EventsInteractor.EventsInteractorListener, UsersInteractor.UsersInteractorListener {
    private final ProfilePresenterListener listener;
    private final EventsInteractor eventsInteractor;
    private final UsersInteractor usersInteractor;

    /**
     * Construction
     */
    public ProfilePresenter(final ProfilePresenterListener listener) {
        this.listener = listener;
        this.eventsInteractor = new EventsInteractor(this);
        this.usersInteractor = new UsersInteractor(this);
    }

    /**
     * Methods
     */
    // uID here is the FBUser's unique ID generated on Sign Up.
    // This is how we link our FBUsers to our FBDB
    public void createUser(User user, final String uID) {
        usersInteractor.createUser(user, uID);
    }

    public void patchUser(User user, final String uID) {
        usersInteractor.patchUser(user, uID );
    }

    public void getUser(final String uID) {
        usersInteractor.getUserDetails(uID);
    }

    private void getEventDetails(List<String> eventIds) {
        for (String id : eventIds) {
            eventsInteractor.getEventDetail(id);
        }
    }

    @Override
    public void eventDetailReturned(EventDetail event, String eventId) {
        listener.eventReady(event, eventId);
    }

    @Override
    public void userDetailReturned(User user, String userId) {
        listener.profileInfoReady(user, userId);
    }

    @Override
    public void presentError(String error) {
        listener.presentError(error);
    }


    /**
     * Contract / Listener
     */
    public interface ProfilePresenterListener {

        void profileInfoReady(User user, String userId);

        void eventReady(EventDetail event, String eventId);

        void presentError(String error);

        void loadingState(Boolean show);
    }
}
