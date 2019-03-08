package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.data.Contacts.ContactsRepository;
import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.views.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.NoSuchElementException;

import io.reactivex.disposables.CompositeDisposable;

public class ContactsFragmentPresenter implements BasePresenter {
    private final ContactsPresenterListener presenterListener;
    private final ContactsRepository contactsRepository;
    private final CompositeDisposable compositeDisposable;
    private String userId;

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
    private void loadContacts() {
            compositeDisposable
                    .add(contactsRepository.getContactsForUser(userId)
                            .subscribe(
                                    stringUserEntry ->
                                            presenterListener.contactReturned(stringUserEntry.getKey(), stringUserEntry.getValue()),
                                    throwable -> {
                                        if (throwable instanceof NoSuchElementException) {
                                            presenterListener.noContactsReturned();
                                        } else {
                                            presenterListener.presentError(throwable.getMessage());
                                        }
                                    }));
        }


    @Override
    public void subscribe() {
        if (FirebaseAuth.getInstance().getUid() != null) {
            userId = FirebaseAuth.getInstance().getUid();
            loadContacts();
        } else {
            presenterListener.showSignedOutEmptyState();
        }

    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }


    /**
     * PresenterListener Definition
     */
    public interface ContactsPresenterListener extends BasePresenterListener {

        void noContactsReturned();

        void contactReturned(String id, User user);

        void presentError(String error);

    }
}
