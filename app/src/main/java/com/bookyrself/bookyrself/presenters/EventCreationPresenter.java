package com.bookyrself.bookyrself.presenters;

import android.util.Log;

import com.bookyrself.bookyrself.data.Contacts.ContactsRepository;
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.data.ResponseModels.User.EventInviteInfo;
import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.bookyrself.bookyrself.views.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benmedcalf on 3/9/18.
 */

public class EventCreationPresenter implements BasePresenter {

    private EventCreationPresenterListener presenterListener;
    private ContactsRepository contactsRepository;
    private CompositeDisposable compositeDisposable;

    /**
     * Constructor
     */
    public EventCreationPresenter(EventCreationPresenterListener listener) {
        this.contactsRepository = MainActivity.getContactsRepo();
        this.presenterListener = listener;
        this.compositeDisposable = new CompositeDisposable();
    }

    /**
     * Methods
     */

    @Override
    public void subscribe() {
        compositeDisposable.add(
                contactsRepository
                        .getContactsForUser(FirebaseAuth.getInstance().getUid())
                        .subscribe(
                                stringUserPair -> presenterListener.contactReturned(stringUserPair.second, stringUserPair.first),
                                throwable -> presenterListener.presentError(throwable.getMessage())));
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.dispose();
    }


    public void createEvent(EventDetail event) {
        compositeDisposable.add(
                FirebaseService.getAPI().createEvent(event)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())

                        // Set the event to the Host's user
                        .doOnNext(eventCreationResponse -> {

                            EventInviteInfo hostEventInviteInfo = new EventInviteInfo();
                            hostEventInviteInfo.setIsInviteAccepted(true);
                            hostEventInviteInfo.setIsHost(true);
                            hostEventInviteInfo.setIsInviteRejected(false);

                            FirebaseService.getAPI()
                                    .addEventToUser(hostEventInviteInfo,
                                            FirebaseAuth.getInstance().getUid(),
                                            eventCreationResponse.getName())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe();
                        })

                        // Set the event to the invitees
                        .doOnNext(eventCreationResponse -> {

                            EventInviteInfo inviteeEventInfo = new EventInviteInfo();

                            inviteeEventInfo.setIsHost(false);
                            inviteeEventInfo.setIsInviteAccepted(false);
                            inviteeEventInfo.setIsInviteRejected(false);

                            for (String userId : event.getUsers().keySet()) {
                                Flowable.just(FirebaseService.getAPI()
                                        .addEventToUser(inviteeEventInfo, userId, eventCreationResponse.getName())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe());
                            }
                        })
                        .subscribe(
                                eventCreationResponse -> presenterListener.eventCreated(),
                                throwable -> {
                                    presenterListener.presentError(throwable.getMessage());
                                    Log.e("EventCreationPresenter:", throwable.getMessage());
                                }));
    }


    // Called by DatePickerDialogFragment, not the actual EventCreationActivity
    public void setDate(String date) {
        presenterListener.dateSelectedFromDatePickerDialog(date);
    }


    /**
     * Contract / Listener
     */
    public interface EventCreationPresenterListener {

        void contactReturned(User contact, String userId);

        void eventCreated();

        void dateSelectedFromDatePickerDialog(String date);

        void presentError(String message);
    }
}
