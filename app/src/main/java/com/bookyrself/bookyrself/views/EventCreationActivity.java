package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.Host;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers._source;
import com.bookyrself.bookyrself.presenters.ContactsActivityPresenter;
import com.bookyrself.bookyrself.presenters.EventCreationPresenter;
import com.bookyrself.bookyrself.utils.CircleTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventCreationActivity extends AppCompatActivity implements EventCreationPresenter.EventCreationPresenterListener {


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
    private List<_source> contacts;
    private Map<_source, String> contactsMap;
    ContactsAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);
        ButterKnife.bind(this);
        presenter = new EventCreationPresenter(this);
        //TODO: Clean up host fetching logic
        adapter = new ContactsAdapter();
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        contacts = new ArrayList<>();
        contactsMap = new HashMap<>();
        presenter.getContacts(FirebaseAuth.getInstance().getUid());

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
    public void contactsReturned(Map<_source, String> usersMap, List<_source> contactsList) {
     contacts = contactsList;
     //TODO: These variable names are horrible. Match em
     contactsMap = usersMap;
     adapter.notifyDataSetChanged();
    }

    @Override
    public void removeFromPotentialUsers(String userId) {

    }


    //TODO: This code is duplicated from ContactsFragment. How to reuse?
    //TODO: Also, having a hashmap with <string ids, _source users> AND a list of <_source> seems redundant
    /**
     * Adapter
     */
    class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ContactsAdapter() {

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_user_search_result, parent, false);
            return new EventCreationActivity.ContactsAdapter.ViewHolderContacts(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            EventCreationActivity.ContactsAdapter.ViewHolderContacts viewHolderContacts = (EventCreationActivity.ContactsAdapter.ViewHolderContacts) holder;
            viewHolderContacts.userNameTextView.setText(contacts.get(position).getUsername());
            //TODO: Should I get the whole application context here?
            Picasso.with(getApplicationContext())
                    .load("https://img.etsystatic.com/il/9a1dbd/1358791570/il_570xN.1358791570_ib1c.jpg")
                    .placeholder(R.drawable.round)
                    .error(R.drawable.round)
                    .transform(new CircleTransform())
                    .resize(100, 100)
                    .into(viewHolderContacts.userProfileImageThumb);
            viewHolderContacts.userCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), UserDetailActivity.class);
                    intent.putExtra("userId", contactsMap.get(contacts.get(position)));
                    startActivity(intent);
                }
            });
        }


        @Override
        public int getItemCount() {
            return contacts.size();
        }

        class ViewHolderContacts extends RecyclerView.ViewHolder {

            @BindView(R.id.search_result_card_users)
            CardView userCardView;
            @BindView(R.id.username_search_result)
            TextView userNameTextView;
            @BindView(R.id.user_image_search_result)
            ImageView userProfileImageThumb;

            ViewHolderContacts(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}