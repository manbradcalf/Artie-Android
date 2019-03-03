package com.bookyrself.bookyrself.presenters;

import android.content.Context;

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

    private final EventsPresenterListener presenterListener;
    private final EventsRepo eventsRepo;
    private final CompositeDisposable compositeDisposable;
    private String userId;


    /**
     * Constructor
     */
    public EventsFragmentPresenter(EventsPresenterListener presenterListener, Context context) {
        this.presenterListener = presenterListener;
        this.compositeDisposable = new CompositeDisposable();
        this.eventsRepo = MainActivity.getEventsRepo(context);
    }

    /**
     * Methods
     */
    private void loadUsersEventInfo() {

            compositeDisposable
                    .add(eventsRepo.getAllEvents(userId)
                            .subscribe(
                                    //onNext
                                    stringEventDetailEntry -> presenterListener.eventDetailReturned(
                                            stringEventDetailEntry.getValue(),
                                            stringEventDetailEntry.getKey()),

                                    //onError
                                    throwable -> {
                                        if (throwable instanceof NoSuchElementException) {
                                            presenterListener.noEventDetailsReturned();
                                        } else {
                                            presenterListener.presentError(throwable.getMessage());
                                        }
                                    }));
    }

    @Override
    public void subscribe() {
        if (FirebaseAuth.getInstance().getUid() != null){
            userId = FirebaseAuth.getInstance().getUid();
            loadUsersEventInfo();
        } else {
            presenterListener.showSignedOutEmptyState();
        }
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    /**
     * PresenterListener Definition
     */
    public interface EventsPresenterListener extends BasePresenterListener {

        void eventDetailReturned(EventDetail event, String eventId);

        void noEventDetailsReturned();

        void presentError(String error);
    }

}
