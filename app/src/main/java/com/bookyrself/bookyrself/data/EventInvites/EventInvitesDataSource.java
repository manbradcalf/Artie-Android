package com.bookyrself.bookyrself.data.EventInvites;

import android.support.v4.util.Pair;

import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;

import io.reactivex.Flowable;

public interface EventInvitesDataSource {

    Flowable<Pair<String, EventDetail>> getPendingEventInvites(String userId);

    Flowable<Boolean> acceptEventInvite(String userId, String eventId);

    Flowable<Boolean> rejectEventInvite(String userId, String eventId);
}
