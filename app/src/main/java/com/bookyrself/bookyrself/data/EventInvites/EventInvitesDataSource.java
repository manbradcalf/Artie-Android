package com.bookyrself.bookyrself.data.EventInvites;

import android.support.v4.util.Pair;

import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;

import io.reactivex.Flowable;

public interface EventInvitesDataSource {

    Flowable<Pair<String, EventDetail>> getPendingEventInvites(String userId);
}
