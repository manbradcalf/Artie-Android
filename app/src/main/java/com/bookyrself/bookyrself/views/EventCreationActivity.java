package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventCreationActivity extends AppCompatActivity implements EventCreationPresenter.EventCreationPresenterListener {


    @BindView(R.id.event_creation_toolbar)
    Toolbar toolbar;

    @BindView(R.id.event_creation_scrollview)
    ScrollView scrollView;

    @BindView(R.id.event_creation_city_state)
    EditText cityStateEditText;

    @BindView(R.id.event_creation_event_name)
    EditText eventNameEditText;

    @BindView(R.id.event_creation_tags)
    EditText tagsEditText;

    @BindView(R.id.event_creation_date_button)
    EditText dateButton;

    @BindView(R.id.event_creation_submit_button)
    FloatingActionButton submitButton;

    @BindView(R.id.search_contacts_event_creation)
    ChipsInput contactChipsInput;

    private EventCreationPresenter presenter;
    private List<User> contacts;
    private Map<User, String> contactsAndUserIdsMap;
    private HashMap<String, Boolean> selectedContactsAndAttendingBooleanMap;
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
                //TODO: Find a better control flow for validating required fields
                EventDetail event = new EventDetail();
                // Contacts are the only required propert for an event
                if (!contactChipsInput.getSelectedChipList().isEmpty()) {
                    List<User> selectedUsers = (List<User>) contactChipsInput.getSelectedChipList();

                    for (User user : selectedUsers) {
                        // Get the user Id
                        String userId = contactsAndUserIdsMap.get(user);

                        // Set userId's attending boolean to false
                        selectedContactsAndAttendingBooleanMap.put(userId, false);
                    }
                    event.setUsers(selectedContactsAndAttendingBooleanMap);
                } else {
                    Toast.makeText(getApplicationContext(), "Please select contacts to invite!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!eventNameEditText.getText().toString().isEmpty()) {
                    event.setEventname(eventNameEditText.getText().toString());
                } else {
                    eventNameEditText.requestFocus();
                    Toast.makeText(getApplicationContext(), "Please name your event!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!cityStateEditText.getText().toString().isEmpty()) {
                    event.setCitystate(cityStateEditText.getText().toString());
                } else {
                    cityStateEditText.requestFocus();
                    Toast.makeText(getApplicationContext(), "Please select a location!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!tagsEditText.getText().toString().isEmpty()) {
                    List<String> tagsList = Arrays.asList(tagsEditText.getText().toString().split(", "));
                    event.setTags(tagsList);
                }
                if (date != null) {
                    event.setDate(date);
                } else {
                    Toast.makeText(getApplicationContext(), "Please select a date!", Toast.LENGTH_LONG).show();
                    return;
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
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void contactReturned(User contact, String userId) {
        contacts.add(contact);
        contactsAndUserIdsMap.put(contact, userId);
        contactChipsInput.setFilterableList(contacts);
    }

    @Override
    public void dateAdded(String dateSelected) {

        // Set the date for the event
        date = dateSelected;

        // Format the date for the view
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date parsedDate = inputFormat.parse(dateSelected);
            DateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy");
            String dateString = outputFormat.format(parsedDate);
            dateButton.setText(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}