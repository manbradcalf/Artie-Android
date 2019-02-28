package com.bookyrself.bookyrself.data.Events;

import android.support.v4.util.Pair;

import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;

import io.reactivex.Flowable;
import retrofit2.Response;

public interface EventDataSource {

    Flowable<Pair<String,EventDetail>> getAllEvents(String userId);

    Flowable<Pair<String, EventDetail>> getEventsWithPendingInvites(String userId);

    Flowable<Boolean> acceptEventInvite(String userId, String eventId);

    Flowable<Response<Void>> rejectEventInvite(String userId, String eventId);
}
