package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.interactors.UsersInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 1/13/18.
 */

public class UserDetailPresenter implements EventsInteractor.EventsInteractorListener, UsersInteractor.UserDetailInteractorListener {
    private final UserDetailPresenterListener listener;
    private final FirebaseService service;
    private final EventsInteractor eventsInteractor;
    private final UsersInteractor usersInteractor;

    /**
     * Constructor
     */

    public UserDetailPresenter(UserDetailPresenterListener listener) {
        this.listener = listener;
        this.service = new FirebaseService();
        this.eventsInteractor = new EventsInteractor(this);
        this.usersInteractor = new UsersInteractor(this);
    }

    /**
     * Methods
     */
    public void getUserInfo(final String userId) {
        usersInteractor.getUserDetails(userId);
    }


    //TODO: Put this in the interactor
    public void addContactToUser(String userId, String contactId) {
        HashMap<String, Boolean> request = new HashMap<>();
        request.put(contactId, true);
        service.getAPI().addContactToUser(request, userId).enqueue(new Callback<HashMap<String, Boolean>>() {
            @Override
            public void onResponse(Call<HashMap<String, Boolean>> call, Response<HashMap<String, Boolean>> response) {
                listener.presentSuccess("added to contacts!");
            }

            @Override
            public void onFailure(Call<HashMap<String, Boolean>> call, Throwable t) {
                presentError(t.getMessage());
            }
        });
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
        listener.userInfoReady(user);
        if (user.getEvents() != null) {
            eventsInteractor.getMultipleEventDetails(new ArrayList<>(user.getEvents().keySet()));
        }
    }

    @Override
    public void contactSuccessfullyAdded() {

    }


    /**
     * Contract / Listener
     */
    public interface UserDetailPresenterListener {
        void userInfoReady(User userInfo);

        void presentError(String message);

        void loadingState();

        void emailUser();

        void presentSuccess(String message);

        void usersEventReturned(EventDetail event, String eventId);
    }
}
