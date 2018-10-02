package com.bookyrself.bookyrself.interactors;

import android.util.Log;

import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers._source;
import com.bookyrself.bookyrself.presenters.BasePresenter;
import com.bookyrself.bookyrself.presenters.ContactsActivityPresenter;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsInteractor {

    private final ContactsInteractorListener listener;
    private final FirebaseService service;

    public ContactsInteractor(ContactsInteractorListener listener) {
        this.service = new FirebaseService();
        this.listener = listener;
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
                listener.presentError(t.getMessage());
                Log.e("getContactIds: ", t.getMessage());
            }
        });
    }

    public void getUsers(final List<String> userIds) {
        if (userIds != null) {
            for (int user = 0; user < userIds.size(); user++) {
                final int position = user;
                service.getAPI().getUserDetails(userIds.get(user)).enqueue(new Callback<_source>() {
                    @Override
                    public void onResponse(Call<_source> call, Response<_source> response) {
                        listener.userReturned(userIds.get(position), response.body());
                    }

                    @Override
                    public void onFailure(Call<_source> call, Throwable t) {
                        Log.e("ContactsInteractor", t.getMessage());
                    }
                });
            }
        } else {
            listener.noUsersReturned();
        }
    }

    public interface ContactsInteractorListener {

        void contactsReturned(List<String> ids);

        void userReturned(String id, _source user);

        void noUsersReturned();

        void presentError(String error);
    }
}
