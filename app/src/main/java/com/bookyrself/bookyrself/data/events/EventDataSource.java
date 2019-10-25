package com.bookyrself.bookyrself.data.Events;

import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail;

import java.util.AbstractMap;
import java.util.Map;

import io.reactivex.Flowable;
import retrofit2.Response;

public interface EventDataSource {

    Flowable<Map.Entry<String, EventDetail>> getAllEvents(String userId);

    Flowable<AbstractMap.SimpleEntry<String, EventDetail>> getEventsWithPendingInvites(String userId);

    Flowable<Boolean> acceptEventInvite(String userId, String eventId);

    Flowable<Response<Void>> rejectEventInvite(String userId, String eventId);
}
