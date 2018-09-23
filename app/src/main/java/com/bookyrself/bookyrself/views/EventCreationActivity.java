package com.bookyrself.bookyrself.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.Host;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers._source;
import com.bookyrself.bookyrself.presenters.ContactsActivityPresenter;
import com.bookyrself.bookyrself.presenters.EventCreationPresenter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
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

    @BindView(R.id.event_creation_scrollview)
    ScrollView scrollView;

    @BindView(R.id.event_creation_city_state)
    EditText cityStateEditText;

    @BindView(R.id.event_creation_event_name)
    EditText eventNameEditText;

    @BindView(R.id.event_creation_tags)
    EditText tagsEditText;

    @BindView(R.id.event_creation_submit_button)
    Button submitButton;

    private EventCreationPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);
        presenter = new EventCreationPresenter(this);
        ButterKnife.bind(this);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventDetail event = new EventDetail();
                if (cityStateEditText.getText() != null) {
                    event.setCitystate(cityStateEditText.getText().toString());
                }
                if (eventNameEditText.getText() != null) {
                    event.setEventname(eventNameEditText.getText().toString());
                }
                if (tagsEditText.getText() != null) {
                    List<String> tagsList = Arrays.asList(tagsEditText.getText().toString().split(", "));
                    event.setTags(tagsList);
                }

                //TODO: Clean up host fetching logic
                Host host = new Host();
                List<Host> hosts = new ArrayList<>();
                host.setUsername(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                host.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                hosts.add(host);
                event.setHost(hosts);
                presenter.createEvent(event);
            }
        });
    }


    @Override
    public void eventCreated() {

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