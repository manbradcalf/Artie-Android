package com.bookyrself.bookyrself.data.EventInvites;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.User.EventInviteInfo;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class EventInvitesRepo implements EventInvitesDataSource {

    private HashMap<String, EventDetail> pendingEventInvitesMap;
    private boolean cacheIsDirty;
    private DatabaseReference db;


    public EventInvitesRepo() {
        this.pendingEventInvitesMap = new HashMap<>();
        this.cacheIsDirty = true;
        if (FirebaseAuth.getInstance().getUid() != null) {
            this.db = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(FirebaseAuth.getInstance().getUid())
                    .child("events");


            this.db.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    cacheIsDirty = true;
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    cacheIsDirty = true;
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    cacheIsDirty = true;
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    cacheIsDirty = true;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    cacheIsDirty = true;
                }
            });

        }
    }

    @Override
    public Flowable<Pair<String, EventDetail>> getPendingEventInvites(String userId) {

        if (cacheIsDirty) {
            cacheIsDirty = false;
            return FirebaseService.getAPI()
                    .getUsersEventInvites(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(HashMap::entrySet)

                    // Filter out the invites so only pending invites remain
                    .map(entries -> {
                        HashMap<String, EventInviteInfo> pendingInvites = new HashMap<>();
                        for (Map.Entry<String, EventInviteInfo> entry : entries) {
                            if (isInvitePendingResponse(entry)) {
                                pendingInvites.put(entry.getKey(), entry.getValue());
                            }
                        }
                        if (pendingInvites.isEmpty()) {
                            throw new NoSuchElementException();
                        } else {
                            return pendingInvites;
                        }
                    })

                    // Now check if the list of pending invites is empty
                    .firstOrError()
                    .toFlowable()
                    .flatMapIterable(HashMap::entrySet)

                    // Fetch event info for each pending invite
                    .flatMap(eventInvite -> FirebaseService.getAPI()
                            .getEventData(eventInvite.getKey())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(eventDetail -> {
                                pendingEventInvitesMap.put(eventInvite.getKey(), eventDetail);
                                return new Pair<>(eventInvite.getKey(), eventDetail);
                            }));
        } else {
            if (pendingEventInvitesMap == null) {
                throw new NoSuchElementException();
            } else {
                return Flowable.fromIterable(pendingEventInvitesMap.entrySet())
                        .map(stringEventDetailEntry -> new Pair<>(stringEventDetailEntry.getKey(), stringEventDetailEntry.getValue()));
            }
        }
    }

    @Override
    public Flowable<Boolean> acceptEventInvite(String userId, String eventId) {
        return FirebaseService.getAPI()
                .acceptInvite(true, userId, eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(aBoolean -> FirebaseService.getAPI()
                        .setEventUserAsAttending(true, userId, eventId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()))
                .doOnNext(aBoolean -> {
                    // Remove from pending invitations cache
                    pendingEventInvitesMap.remove(eventId);
                });
    }

    @Override
    public Flowable<Boolean> rejectEventInvite(String userId, String eventId) {
        return FirebaseService.getAPI().
                rejectInvite(true, userId, eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(aBoolean -> FirebaseService.getAPI()
                        .setEventUserAsAttending(false, userId, eventId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()))
                .doOnNext(aBoolean -> {
                    // Remove from pending invitations cache
                    pendingEventInvitesMap.remove(eventId);
                });
    }

    private Boolean isInvitePendingResponse(Map.Entry<String, EventInviteInfo> eventInvite) {

        if (eventInvite != null) {
            // All fields are false by default, so if all fields remain false,
            // It means the user hasn't interacted at all with the invite,
            // hence the invite is pending
            return !eventInvite.getValue().getIsInviteAccepted()
                    && !eventInvite.getValue().getIsInviteRejected()
                    && !eventInvite.getValue().getIsHost();
        } else {
            return false;
        }
    }
}
