package com.bookyrself.bookyrself.interactors;

import android.support.v4.util.Pair;
import android.util.Log;

import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.presenters.BasePresenter;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.HashMap;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ContactsRepository implements ContactsDataSource {

    private ContactsInteractorListener contactsInteractorListener;
    private Boolean cacheIsDirty;
    private HashMap<String, User> contactsMap;

    public ContactsRepository(ContactsInteractorListener listener) {
        this.contactsMap = new HashMap<>();
        this.contactsInteractorListener = listener;
        this.cacheIsDirty = false;
    }

    /**
     * Methods
     */
    @Override
    public Flowable<Pair<String, User>> getContactsForUser(String userId) {

        return FirebaseService.getAPI()
                .getUserContacts(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapIterable(HashMap::keySet)
                .flatMap(s -> FirebaseService.getAPI()
                        .getUserDetails(s)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(user -> {
                            contactsMap.put(s,user);
                            cacheIsDirty = false;
                            return new Pair<>(s, user);
                        }));
    }


    public interface ContactsInteractorListener extends BasePresenter {

        void contactReturned(String userId, User contact);

        void noContactsReturned();

    }
}
