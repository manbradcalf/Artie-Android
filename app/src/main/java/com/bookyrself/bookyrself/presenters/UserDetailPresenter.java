package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.interactors.UsersInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;

import java.util.ArrayList;

/**
 * Created by benmedcalf on 1/13/18.
 */

public class UserDetailPresenter implements EventsInteractor.EventsInteractorListener, UsersInteractor.UserDetailInteractorListener{
    private final UserDetailPresenterListener listener;
    private final EventsInteractor eventsInteractor;
    private final UsersInteractor usersInteractor;

    /**
     * Constructor
     */

    public UserDetailPresenter(UserDetailPresenterListener listener) {
        this.listener = listener;
        this.eventsInteractor = new EventsInteractor(this);
        this.usersInteractor = new UsersInteractor(this);
    }

    /**
     * Methods
     */
    public void getUserInfo(final String userId) {
        usersInteractor.getUserDetails(userId);
    }

    public void addContactToUser(String contactId, String userId) {
        usersInteractor.addContactToUser(contactId,userId);
    }

    @Override
    public void eventDetailReturned(EventDetail event, String eventId) {
        listener.usersEventReturned(event, eventId);
    }

    @Override
    public void presentError(String error) {

    }

    @Override
    public void userDetailReturned(User user, String userId) {
        listener.userInfoReady(user,userId);
        if (user.getEvents() != null) {
            eventsInteractor.getMultipleEventDetails(new ArrayList<>(user.getEvents().keySet()));
        }
    }

    @Override
    public void contactSuccessfullyAdded() {
        listener.presentSuccess("Added contact!");
    }


    /**
     * Contract / Listener
     */
    public interface UserDetailPresenterListener {
        void userInfoReady(User userInfo, String userId);

        void presentError(String message);

        void loadingState();

        void emailUser();

        void presentSuccess(String message);

        void usersEventReturned(EventDetail event, String eventId);
    }
}
