package com.bookyrself.bookyrself.presenters;

import android.util.Log;

import com.bookyrself.bookyrself.data.EventsInteractor;
import com.bookyrself.bookyrself.data.UsersInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;

import java.util.List;

/**
 * Created by benmedcalf on 3/11/18.
 */

public class EventsFragmentPresenter implements UsersInteractor.UsersEventsInteractorListener {

    private final EventsPresenterListener listener;
//    private final EventsInteractor eventsInteractor;
    private final UsersInteractor usersInteractor;

    /**
     * Constructor
     */
    public EventsFragmentPresenter(EventsPresenterListener listener) {
        this.listener = listener;
//        this.eventsInteractor = new EventsInteractor(this);
        this.usersInteractor = new UsersInteractor(this);
    }

    /**
     * Methods
     */
//    @Override
//    public void eventDetailReturned(EventDetail event, String eventId) {
//        listener.eventDetailReturned(event, eventId);
//    }

    @Override
    public void eventsReturned(List<String> eventIds) {
//        getEventDetails(eventIds);
    }

    @Override
    public void presentError(String error) {
        Log.e(this.toString(), error);
        listener.presentError(error);
    }

    @Override
    public void noEventsReturned() {
        listener.noEventDetailsReturned();
    }

    public void loadUsersEventInfo(final String userId) {
        usersInteractor.getUserEvents(userId);
    }

//    private void getEventDetails(List<String> eventIds) {
//        for (String id : eventIds) {
//            eventsInteractor.getEventDetail(id);
//        }
//    }

    /**
     * Contract / Listener
     */
    public interface EventsPresenterListener {

        void eventDetailReturned(EventDetail event, String eventId);

        void noEventDetailsReturned();

        void presentError(String error);
    }

}
