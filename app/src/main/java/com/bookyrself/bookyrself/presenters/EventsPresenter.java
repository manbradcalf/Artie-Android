package com.bookyrself.bookyrself.presenters;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.User.EventInfo;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 3/11/18.
 */

public class EventsPresenter implements EventsInteractor.EventsInteractorListener {

    private final EventsPresenterListener listener;
    private final EventsInteractor eventsInteractor;
    private final FirebaseService service;

    /**
     * Constructor
     */
    public EventsPresenter(EventsPresenterListener listener) {
        this.listener = listener;
        this.service = new FirebaseService();
        this.eventsInteractor = new EventsInteractor(this);
    }

    /**
     * Methods
     */
    public void loadUsersEventInfo(final String userId) {
        service.getAPI().getUsersEventInfo(userId).enqueue(new Callback<HashMap<String, EventInfo>>() {
            @Override
            public void onResponse(@NonNull Call<HashMap<String, EventInfo>> call, @NonNull Response<HashMap<String, EventInfo>> response) {
                if (response.body() != null) {
                    getEventDetails(new ArrayList<>(response.body().keySet()));
                }

            }

            @Override
            public void onFailure(Call<HashMap<String, EventInfo>> call, Throwable t) {
                Log.e("EventsPresenter: ", t.getMessage());
                listener.presentError(t.getMessage());
            }
        });
    }

    private void getEventDetails(List<String> eventIds) {
        for (String id : eventIds) {
            eventsInteractor.getEventDetail(id);
        }
    }

    @Override
    public void eventDetailReturned(EventDetail event, String eventId) {
        listener.eventDetailReturned(event, eventId);
    }

    @Override
    public void presentError(String error) {
        Log.e(this.toString(), error);
        listener.presentError(error);
    }


    /**
     * Contract / Listener
     */
    public interface EventsPresenterListener {

        void eventDetailReturned(EventDetail event, String eventId);

        void presentError(String error);
    }

}
