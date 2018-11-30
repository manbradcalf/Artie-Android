package com.bookyrself.bookyrself.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.Host;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.presenters.EventCreationPresenter;
import com.google.firebase.auth.FirebaseAuth;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;
import com.pchmn.materialchips.model.ChipInterface;

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

    @BindView(R.id.search_contacts_event_creation)
    ChipsInput contactChipsInput;

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
    private Map<User, String> contactsAndUserIdsMap;
    private HashMap<String,Boolean> selectedContactsAndAttendingBooleanMap;
    private String date;
    private int FLAG_EVENT_CREATION = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);
        ButterKnife.bind(this);
        presenter = new EventCreationPresenter(this);
        // TODO: Clean up this wacky variable name
        selectedContactsAndAttendingBooleanMap = new HashMap<>();
        contacts = new ArrayList<>();
        contactsAndUserIdsMap = new HashMap<>();

        // Set a default empty list to avoid NPEs
        List<Chip> tempList = new ArrayList<>();
        tempList.add(new Chip("test","testing"));
        contactChipsInput.setFilterableList(tempList);

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
                if (!contactChipsInput.getSelectedChipList().isEmpty()) {

                    List<User> selectedUsers = (List<User>) contactChipsInput.getSelectedChipList();

                    for (User user : selectedUsers) {
                        // Get the user Id
                        String userId = contactsAndUserIdsMap.get(user);

                        // Set userId's attending boolean to false
                        selectedContactsAndAttendingBooleanMap.put(userId,false);
                    }
                    event.setUsers(selectedContactsAndAttendingBooleanMap);
                }


                Host host = new Host();
                host.setUsername(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                host.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                event.setHost(host);
                presenter.createEvent(event);
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
        contacts.addAll(contactsList);
        //TODO: These variable names are horrible. Match em
        contactsAndUserIdsMap = usersMap;
        contactChipsInput.setFilterableList(contacts);
    }

    @Override
    public void removeFromPotentialUsers(String userId) {

    }

    @Override
    public void dateAdded(String dateSelected) {
        date = dateSelected;
    }

}