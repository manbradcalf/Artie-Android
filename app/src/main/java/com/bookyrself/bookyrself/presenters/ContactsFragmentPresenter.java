package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.data.Contacts.ContactsRepository;
import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.views.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

import io.reactivex.disposables.CompositeDisposable;

public class ContactsFragmentPresenter implements ContactsRepository.ContactsInteractorListener {
    private final ContactsPresenterListener presenterListener;
    private final ContactsRepository contactsRepository;
    private final CompositeDisposable compositeDisposable;
    private final String userId = FirebaseAuth.getInstance().getUid();

    /**
     * Constructor
     */
    public ContactsFragmentPresenter(ContactsPresenterListener listener) {
        this.presenterListener = listener;
        this.contactsRepository = MainActivity.getContactsRepo();
        this.compositeDisposable = new CompositeDisposable();
    }

    /**
     * Methods
     */
    public void loadContacts(String userId) {

        compositeDisposable
                .add(contactsRepository.getContactsForUser(userId)
                        .forEach(stringUserPair ->
                                contactReturned(stringUserPair.first, stringUserPair.second)));
    }

    /**
     * Interactor Listener
     */

    @Override
    public void subscribe() {
        loadContacts(userId);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    @Override
    public void contactReturned(String userId, User user) {
        presenterListener.contactReturned(userId, user);
    }

    @Override
    public void noContactsReturned() {
        presenterListener.noContactsReturned();
    }

    /**
     * Presenter Listener Definition
     */
    public interface ContactsPresenterListener {

        void noContactsReturned();

        void contactReturned(String id, User user);

        void presentError(String error);

    }
}
