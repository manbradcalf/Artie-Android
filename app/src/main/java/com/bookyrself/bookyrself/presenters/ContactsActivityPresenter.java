package com.bookyrself.bookyrself.presenters;

import android.util.Log;

import com.bookyrself.bookyrself.interactors.BaseInteractor;
import com.bookyrself.bookyrself.interactors.ContactsInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers._source;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsActivityPresenter extends BasePresenter implements ContactsInteractor.ContactsInteractorListener {
    private final ContactsPresenterListener presenterListener;
    private final ContactsInteractor contactsInteractor;

    /**
     * Constructor
     */
    public ContactsActivityPresenter(ContactsPresenterListener listener) {
        this.presenterListener = listener;
        this.contactsInteractor = new ContactsInteractor(this);
    }

    /**
     * Methods
     */
    public void getContactIds(String userId) {
        contactsInteractor.getContactIds(userId);
    }

    public void getUsers(final List<String> ids) {
        contactsInteractor.getUsers(ids);
    }


    /**
     * Interactor Listener
     */

    @Override
    public void contactsReturned(List<String> ids) {
        presenterListener.contactsReturned(ids);
    }

    @Override
    public void userReturned(String id, User user) {
        presenterListener.userReturned(id, user);
    }

    @Override
    public void noUsersReturned() {
        presenterListener.noUsersReturned();
    }

    @Override
    public void presentError(String error) {
        presenterListener.presentError();
    }

    /**
     * Presenter Listener Definition
     */
    public interface ContactsPresenterListener {

        void presentError();

        void loadingState();

        void contactsReturned(List<String> ids);

        void userReturned(String id, User user);

        void noUsersReturned();

    }
}
