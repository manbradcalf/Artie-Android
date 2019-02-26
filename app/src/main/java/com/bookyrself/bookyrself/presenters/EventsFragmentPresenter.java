package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.data.Events.EventsRepo;
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.views.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.NoSuchElementException;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by benmedcalf on 3/11/18.
 */

public class EventsFragmentPresenter implements BasePresenter {

    private final EventsPresenterListener listener;
    private final EventsRepo eventsRepo;
    private final CompositeDisposable compositeDisposable;


    /**
     * Constructor
     */
    public EventsFragmentPresenter(EventsPresenterListener listener) {
        this.listener = listener;
        this.compositeDisposable = new CompositeDisposable();
        this.eventsRepo = MainActivity.getEventsRepo();
    }

    /**
     * Methods
     */
    public void loadUsersEventInfo(final String userId) {
        compositeDisposable
                .add(eventsRepo.getAllEvents(userId)
                        .subscribe(
                                //onNext
                                stringEventDetailPair -> listener.eventDetailReturned(
                                        stringEventDetailPair.second,
                                        stringEventDetailPair.first),

                                //onError
                                throwable -> {
                                    if (throwable instanceof NoSuchElementException) {
                                        listener.noEventDetailsReturned();
                                    } else {
                                        listener.presentError(throwable.getMessage());
                                    }
                                }));
    }

    @Override
    public void subscribe() {
        loadUsersEventInfo(FirebaseAuth.getInstance().getUid());
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.dispose();
    }

    /**
     * Contract / Listener
     */
    public interface EventsPresenterListener {

        void eventDetailReturned(EventDetail event, String eventId);

        void noEventDetailsReturned();

        void presentError(String error);
    }

}
