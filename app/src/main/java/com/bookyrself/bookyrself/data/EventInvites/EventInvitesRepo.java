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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class EventInvitesRepo implements EventInvitesDataSource {

    private HashMap<String, EventDetail> pendingEventInvitesMap;
    private boolean cacheIsDirty;
    private DatabaseReference db;


    public EventInvitesRepo() {
        this.pendingEventInvitesMap = new HashMap<>();
        this.cacheIsDirty = true;
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

    @Override
    public Flowable<Pair<String, EventDetail>> getPendingEventInvites(String userId) {

        if (cacheIsDirty) {
            return FirebaseService.getAPI()
                    .getUsersEventInvites(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMapIterable(stringEventInviteInfoHashMap -> getEventIdsOfPendingInvites(stringEventInviteInfoHashMap))
                    .flatMap(eventId -> FirebaseService.getAPI()
                            .getEventData(eventId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(eventDetail -> {
                                pendingEventInvitesMap.put(eventId, eventDetail);
                                cacheIsDirty = false;
                                return new Pair<>(eventId, eventDetail);
                            }));
        } else {
            return Flowable.fromIterable(pendingEventInvitesMap.entrySet())
                    .map(stringEventDetailEntry -> new Pair<>(stringEventDetailEntry.getKey(),stringEventDetailEntry.getValue()));
        }


    }

    private List<String> getEventIdsOfPendingInvites(HashMap<String, EventInviteInfo> eventsMap) {

        List<String> eventIds = new ArrayList<>();

        for (Map.Entry<String, EventInviteInfo> entry : eventsMap.entrySet()) {
            // If the required event invite information exists
            if (entry.getValue().getIsInviteRejected() != null && entry.getValue().getIsInviteAccepted() != null
                    && entry.getValue().getIsHost() != null) {
                // If the user hasn't responded to the invite (hasn't accepted or rejected invites)
                if (!entry.getValue().getIsInviteAccepted() && !entry.getValue().getIsInviteRejected()
                        && !entry.getValue().getIsHost()) {
                    eventIds.add(entry.getKey());
                }
            }
        }
        return eventIds;
    }
}
