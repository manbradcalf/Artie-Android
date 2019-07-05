package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.Host;
import com.bookyrself.bookyrself.data.ServerModels.User.User;
import com.bookyrself.bookyrself.presenters.EventCreationPresenter;
import com.bookyrself.bookyrself.utils.CircleTransform;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pchmn.materialchips.ChipsInput;
import com.squareup.picasso.Picasso;

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
    @BindView(R.id.event_image)
    ImageView eventImage;

    private static final int RC_PHOTO_SELECT = 789;
    private StorageReference storageReference;
    private EventCreationPresenter presenter;
    private List<User> contacts;
    private Map<User, String> contactsAndUserIdsMap;
    private HashMap<String, Boolean> selectedContacts;
    private String date;
    private Uri selectedImage;
    private int FLAG_EVENT_CREATION = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event_creation);
        ButterKnife.bind(this);

        selectedContacts = new HashMap<>();
        contacts = new ArrayList<>();
        contactsAndUserIdsMap = new HashMap<>();
        storageReference = FirebaseStorage.getInstance().getReference();

        eventImage.setImageDrawable(getDrawable((R.drawable.ic_add_a_photo_black_24dp)));
        eventImage.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Choose Picture"), RC_PHOTO_SELECT);
        });

        dateButton.setOnClickListener(view -> {
            DatePickerDialogFragment datePickerDialogFragment = new DatePickerDialogFragment();
            datePickerDialogFragment.setFlag(FLAG_EVENT_CREATION);
            datePickerDialogFragment.setmEventCreationPresenter(presenter);
            datePickerDialogFragment.show(getFragmentManager(), "datePicker");
        });


        submitButton.setOnClickListener(view -> {
            //TODO: Find a better control flow for validating required fields
            EventDetail event = new EventDetail();
            // Contacts are the only required propert for an event
            if (!contactChipsInput.getSelectedChipList().isEmpty()) {
                List<User> selectedUsers = (List<User>) contactChipsInput.getSelectedChipList();

                for (User user : selectedUsers) {
                    // Get the user Id
                    String userId = contactsAndUserIdsMap.get(user);

                    // Set userId's attending boolean to false
                    selectedContacts.put(userId, false);
                }
                event.setUsers(selectedContacts);
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

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                Host host = new Host();
                host.setUsername(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                host.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                event.setHost(host);
                presenter.createEvent(event);
            } else {
                Toast.makeText(EventCreationActivity.this, "You must be logged in to host an event!", Toast.LENGTH_SHORT).show();
            }
        });

        if (FirebaseAuth.getInstance().getUid() != null) {
            presenter = new EventCreationPresenter(this);
            presenter.subscribe();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IdpResponse response = IdpResponse.fromResultIntent(data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_PHOTO_SELECT) {

                selectedImage = data.getData();

                Picasso.with(getApplicationContext())
                        .load(selectedImage)
                        .resize(148, 148)
                        .centerCrop()
                        .transform(new CircleTransform())
                        .into(eventImage);

            }
        } else if (response == null) {
            // User pressed back button
            Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
        } else if (response.getError() != null) {
            Toast.makeText(this, response.getError().getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void eventCreated(String eventId) {

        // Upload to firebase

        StorageReference profilePhotoRef = storageReference.child("images/events/" + eventId);
        UploadTask uploadTask = profilePhotoRef.putFile(selectedImage);
        uploadTask.addOnSuccessListener(taskSnapshot -> Toast.makeText(this, "image upload completed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "image upload failed", Toast.LENGTH_SHORT).show());

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
    public void dateSelectedFromDatePickerDialog(String dateSelected) {

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

    @Override
    public void presentError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}