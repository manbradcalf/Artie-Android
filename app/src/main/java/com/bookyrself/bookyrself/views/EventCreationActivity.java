package com.bookyrself.bookyrself.views;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.Host;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.presenters.EventCreationPresenter;
import com.bookyrself.bookyrself.utils.CircleTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    @BindView(R.id.event_creation_date_button)
    Button dateButton;

    @BindView(R.id.event_creation_submit_button)
    Button submitButton;

    private EventCreationPresenter presenter;
    private List<User> contacts;
    private List<User> originalContacts;
    private List<String> selectedContacts;
    private Map<User, String> contactsMap;
    private String date;
    private int FLAG_EVENT_CREATION = 1;
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
        originalContacts = new ArrayList<>();
        contactsMap = new HashMap<>();
        selectedContacts = new ArrayList<>();
        presenter.getContacts(FirebaseAuth.getInstance().getUid());

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialogFragment datePickerDialogFragment = new DatePickerDialogFragment();
                datePickerDialogFragment.setFlag(FLAG_EVENT_CREATION);
                datePickerDialogFragment.setmEventCreationPresenter(presenter);
                datePickerDialogFragment.show(getFragmentManager(), "datePicker");
            }
        });

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
                if (date != null) {
                    event.setDate(date);
                }
                if (selectedContacts != null) {
                    HashMap<String, Boolean> selectedContactsMap = new HashMap<>();
                    for (String user : selectedContacts) {
                        selectedContactsMap.put(user, false);
                    }
                    event.setUsers(selectedContactsMap);
                }


                Host host = new Host();
                host.setUsername(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                host.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                event.setHost(host);
                presenter.createEvent(event);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }


    @Override
    public void eventCreated() {
        finish();
    }

    @Override
    public void addToPotentialUsers(String userId) {

    }

    @Override
    public void contactsReturned(Map<User, String> usersMap, List<User> contactsList) {
        originalContacts.addAll(contactsList);
        contacts = contactsList;
        //TODO: These variable names are horrible. Match em
        contactsMap = usersMap;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void removeFromPotentialUsers(String userId) {

    }

    @Override
    public void dateAdded(String dateSelected) {
        date = dateSelected;
    }


    //TODO: This code is duplicated from ContactsFragment. How to reuse?
    //TODO: Also, having a hashmap with <string ids, _source users> AND a list of <_source> seems redundant

    /**
     * Adapter
     */
    class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

        private ContactsFilter filter;

        ContactsAdapter() {

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_user_search_result, parent, false);
            return new EventCreationActivity.ContactsAdapter.ViewHolderContacts(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            final EventCreationActivity.ContactsAdapter.ViewHolderContacts viewHolderContacts = (EventCreationActivity.ContactsAdapter.ViewHolderContacts) holder;
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

                    String userId = contactsMap.get(contacts.get(position));

                    if (!selectedContacts.contains(userId)) {
                        viewHolderContacts.userCardView.setCardBackgroundColor(Color.parseColor("green"));
                        selectedContacts.add(userId);

                    } else {
                        viewHolderContacts.userCardView.setCardBackgroundColor(Color.parseColor("gray"));
                        selectedContacts.remove(userId);
                    }

                }
            });
        }


        @Override
        public int getItemCount() {
            return contacts.size();
        }

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new ContactsFilter(this, originalContacts);
            }
            return filter;
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

    class ContactsFilter extends Filter {

        private final ContactsAdapter adapter;
        private final List<User> originalList;
        private final List<User> filteredList;

        private ContactsFilter(ContactsAdapter adapter, List<User> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = originalList;
            this.filteredList = new ArrayList<User>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (charSequence.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                final String filterPattern = charSequence.toString().toLowerCase().trim();
                for (User user : originalList) {
                    if (user.getUsername().toLowerCase().contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            contacts.clear();
            contacts.addAll((Collection<? extends User>) filterResults.values);
            adapter.notifyDataSetChanged();
        }
    }
}