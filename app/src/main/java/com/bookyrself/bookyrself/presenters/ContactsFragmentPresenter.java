package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.interactors.ContactsInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactsFragmentPresenter implements ContactsInteractor.ContactsInteractorListener {
    private final ContactsPresenterListener presenterListener;
    private final ContactsInteractor contactsInteractor;

    /**
     * Constructor
     */
    public ContactsFragmentPresenter(ContactsPresenterListener listener) {
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
    public void contactsReturned(HashMap<String, Boolean> contacts) {
        if (contacts != null) {
            List<String> contactIds = new ArrayList<>(contacts.keySet());
            presenterListener.contactsReturned(contactIds);
        } else {
            noUsersReturned();
        }
    }

    @Override
    public void userReturned(String id, User user) {
        presenterListener.userReturned(id, user);
    }

    @Override
    public void noUsersReturned() {
        presenterListener.presentError("Add contacts by clicking \"Add Contact\" on User profiles");
    }

    @Override
    public void presentError(String error) {
        presenterListener.presentError(error);
    }

    /**
     * Presenter Listener Definition
     */
    public interface ContactsPresenterListener {

        void presentError(String message);

        void contactsReturned(List<String> ids);

        void userReturned(String id, User user);

    }
}
