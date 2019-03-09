package com.bookyrself.bookyrself.data.Contacts;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ContactsRepository implements ContactsDataSource {

    private Boolean cacheIsDirty;
    private HashMap<String, User> contactsMap;
    private DatabaseReference db;

    public ContactsRepository() {
        this.contactsMap = new HashMap<>();
        this.cacheIsDirty = true;

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            contactsMap.clear();
            cacheIsDirty = true;
        });

        if (FirebaseAuth.getInstance().getUid() != null) {
            this.db = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(FirebaseAuth.getInstance().getUid())
                    .child("contacts");

            this.db.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    cacheIsDirty = true;
                    Log.e("Contacts Repo: ", "Child added");
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    cacheIsDirty = true;
                    Log.e("Contacts Repo: ", "Child changed");
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    cacheIsDirty = true;
                    Log.e("Contacts Repo: ", "Child removed");
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    cacheIsDirty = true;
                    Log.e("Contacts Repo: ", "Child moved");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    cacheIsDirty = true;
                    Log.e("Contacts Repo: ", databaseError.getMessage());
                }
            });
        }
    }

    /**
     * Methods
     */
    @Override
    public Flowable<Map.Entry<String, User>> getContactsForUser(String userId) {

        if (cacheIsDirty) {
            // Cache is dirty, get from network
            return FirebaseService.getAPI()
                    .getUserContacts(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(HashMap::entrySet)
                    .firstOrError()
                    .toFlowable()
                    .flatMapIterable(entries -> entries)
                    .flatMap(entry -> FirebaseService.getAPI()
                            .getUserDetails(entry.getKey())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(user -> {
                                contactsMap.put(entry.getKey(), user);
                                cacheIsDirty = false;
                                return new AbstractMap.SimpleEntry<>(entry.getKey(), user);
                            }));
        } else {
            // Cache is clean, get local copy
            return Flowable.fromIterable(contactsMap.entrySet())
                    .map(stringUserEntry -> new AbstractMap.SimpleEntry<>(stringUserEntry.getKey(), stringUserEntry.getValue()));
        }
    }
}
