package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.models.SearchResponseUsers._source;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsPresenter {
    private final ContactsPresenterListener listener;
    private final FirebaseService service;

    /**
     * Constructor
     */
    public ContactsPresenter(ContactsPresenterListener listener) {
        this.listener = listener;
        this.service = new FirebaseService();
    }

    /**
     * Methods
     */
    public void getContactIds(String userId) {
        service.getAPI().getUserContacts(userId).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                listener.contactsReturned(response.body());
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                listener.presentError();
            }
        });
    }

    public void getUsers(final List<String> ids) {
        if (ids != null) {
            for (int user = 0; user < ids.size(); user++) {
                final int position = user;
                service.getAPI().getUserDetails(ids.get(user)).enqueue(new Callback<_source>() {
                    @Override
                    public void onResponse(Call<_source> call, Response<_source> response) {
                        listener.userReturned(ids.get(position), response.body());
                    }

                    @Override
                    public void onFailure(Call<_source> call, Throwable t) {

                    }
                });
            }
        }
    }

    /**
     * Contract / Listener
     */
    public interface ContactsPresenterListener {

        void presentError();

        void loadingState();

        void contactsReturned(List<String> ids);

        void userReturned(String id, _source user);

    }
}
