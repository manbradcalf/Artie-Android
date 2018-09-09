package com.bookyrself.bookyrself.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.view.ViewGroup;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers._source;
import com.bookyrself.bookyrself.presenters.ContactsActivityPresenter;
import com.bookyrself.bookyrself.presenters.EventCreationPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventCreationActivity extends AppCompatActivity implements EventCreationPresenter.EventCreationPresenterListener, ContactsActivityPresenter.ContactsPresenterListener {


    @BindView(R.id.event_creation_toolbar)
    Toolbar toolbar;

    @BindView(R.id.event_creation_recyclerview)
    RecyclerView recyclerView;

    @BindView(R.id.search_contacts_event_creation)
    SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);
        ButterKnife.bind(this);
    }


    @Override
    public void createEvent(String[] userIds) {

    }

    @Override
    public void addToPotentialUsers(String userId) {

    }

    @Override
    public void removeFromPotentialUsers(String userId) {

    }

    @Override
    public void presentError() {

    }

    @Override
    public void loadingState() {

    }

    @Override
    public void contactsReturned(List<String> ids) {

    }

    @Override
    public void userReturned(String id, _source user) {

    }

    @Override
    public void noUsersReturned() {

    }


    /**
     * Adapter
     */
    class ResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}