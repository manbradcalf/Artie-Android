package com.bookyrself.bookyrself.data.Profile;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ProfileRepo implements ProfileDataSource {

    private DatabaseReference db;


    public ProfileRepo() {
        if (FirebaseAuth.getInstance().getUid() != null) {

            String userId = FirebaseAuth.getInstance().getUid();

            this.db = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId);

            this.db.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    // Update repos observable to fetch new data
                    getProfileInfo(userId);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    getProfileInfo(userId);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    getProfileInfo(userId);
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    getProfileInfo(userId);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    getProfileInfo(userId);
                }
            });
        }
     }


    @Override
    public Flowable<User> getProfileInfo(String userId) {

        return FirebaseService.getAPI()
                .getUserDetails(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Flowable<User> updateProfileInfo(String userId, User user) {
        return FirebaseService.getAPI()
                .patchUser(user,userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }
}
