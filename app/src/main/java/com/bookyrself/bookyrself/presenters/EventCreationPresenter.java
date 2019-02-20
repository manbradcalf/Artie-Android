package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.interactors.ContactsRepository;
import com.bookyrself.bookyrself.interactors.EventsInteractor;
import com.bookyrself.bookyrself.interactors.UsersInteractor;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.User.EventInviteInfo;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.views.MainActivity;

import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;

/**
 * Created by benmedcalf on 3/9/18.
 */

public class EventCreationPresenter implements ContactsRepository.ContactsInteractorListener, EventsInteractor.EventCreationInteractorListener, UsersInteractor.UsersInteractorListener {

    private EventCreationPresenterListener presenterListener;
    private ContactsRepository contactsRepository;
    private EventsInteractor eventsInteractor;
    private UsersInteractor usersInteractor;

    /**
     * Constructor
     */
    public EventCreationPresenter(EventCreationPresenterListener listener) {
        this.contactsRepository = MainActivity.getContactsRepo();
        this.eventsInteractor = new EventsInteractor(this);
        this.usersInteractor = new UsersInteractor(this);
        this.presenterListener = listener;
    }

    /**
     * Methods
     */

    public void createEvent(EventDetail event) {
        eventsInteractor.createEvent(event);
    }

    public void getContacts(String userId) {

        contactsRepository.getContactsForUser(userId);
    }

    public void setDate(String date) {
        if (date != null) {
            presenterListener.dateAdded(date);
        }
    }


    /**
     * ContactsRepository Listeners
     */
    @Override
    public void contactReturned(String userId, User contact) {

    }

    @Override
    public void noContactsReturned() {

    }


    /**
     * EventsInteractorListener
     */
    @Override
    public void eventDetailReturned(EventDetail event, String eventId) {

    }

    @Override
    public void addNewlyCreatedEventToUsers(String eventId, List<String> userIdsOfAttendees, String hostUserId) {
        EventInviteInfo hostEventInviteInfo = new EventInviteInfo();
        hostEventInviteInfo.setIsInviteAccepted(true);
        hostEventInviteInfo.setIsHost(true);
        hostEventInviteInfo.setIsInviteRejected(false);
        usersInteractor.addEventToUser(hostEventInviteInfo, hostUserId, eventId);

        if (userIdsOfAttendees.size() != 0) {
            for (String userId : userIdsOfAttendees) {
                EventInviteInfo eventInviteInfo = new EventInviteInfo();
                eventInviteInfo.setIsInviteAccepted(false);
                eventInviteInfo.setIsInviteRejected(false);
                eventInviteInfo.setIsHost(false);
                usersInteractor.addEventToUser(eventInviteInfo, userId, eventId);
            }
            presenterListener.eventCreated();
        }
        //TODO: Wut? I'm tired
        else {
            presenterListener.eventCreated();
        }
    }

    @Override
    public void presentError(String error) {

    }

    @Override
    public void userDetailReturned(User user, String userId) {

    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {

    }

    /**
     * Contract / Listener
     */
    public interface EventCreationPresenterListener {

        void contactReturned(User contact, String userId);

        void eventCreated();

        void dateAdded(String date);
    }
}
