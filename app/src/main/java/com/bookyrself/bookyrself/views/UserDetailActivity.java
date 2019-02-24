package com.bookyrself.bookyrself.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.presenters.UserDetailPresenter;
import com.bookyrself.bookyrself.utils.CircleTransform;
import com.bookyrself.bookyrself.utils.EventDecorator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by benmedcalf on 1/13/18.
 */

public class UserDetailActivity extends AppCompatActivity implements UserDetailPresenter.UserDetailPresenterListener, OnDateSelectedListener {

    @BindView(R.id.message_user_detail_activity_card)
    CardView emailUserCardview;
    @BindView(R.id.add_user_to_contacts_card)
    CardView addUserToContactsCardview;
    @BindView(R.id.username_user_detail_activity)
    TextView usernameTextView;
    @BindView(R.id.city_state_user_detail_activity)
    TextView cityStateTextView;
    @BindView(R.id.tags_user_detail_activity)
    TextView tagsTextView;
    @BindView(R.id.user_url_user_detail_activity)
    TextView urlTextView;
    @BindView(R.id.bio_body_user_detail_activity)
    TextView bioTextView;
    @BindView(R.id.profile_image_user_detail_activity)
    ImageView profileImage;
    @BindView(R.id.profile_image_progressbar)
    ProgressBar profileImageProgressbar;
    @BindView(R.id.message_user_detail_activity_text)
    TextView emailUserTextView;
    @BindView(R.id.add_user_to_contacts_textview)
    TextView addUserToContactsTextView;
    @BindView(R.id.toolbar_user_detail)
    Toolbar Toolbar;
    @BindView(R.id.user_detail_calendar)
    MaterialCalendarView calendarView;
    @BindView(R.id.user_detail_content)
    RelativeLayout contentView;
    @BindView(R.id.user_detail_empty_state)
    View emptyState;
    @BindView(R.id.empty_state_text_header)
    TextView emptyStateTextHeader;
    @BindView(R.id.empty_state_text_subheader)
    TextView emptyStateTextSubHeader;
    @BindView(R.id.empty_state_image)
    ImageView emptyStateImageView;
    @BindView(R.id.empty_state_button)
    Button emptyStateButton;

    private StorageReference storageReference;
    private String userEmailAddress;
    private String userID;
    private HashMap<CalendarDay, String> calendarDaysWithEventIds;
    private UserDetailPresenter userDetailPresenter;
    private List<CalendarDay> acceptedEventsCalendarDays = new ArrayList<>();
    private Set<String> contactIdsToCheck = new HashSet<>();
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        ButterKnife.bind(this);
        userID = getIntent().getStringExtra("userId");
        userDetailPresenter = new UserDetailPresenter(userID, this);
        userDetailPresenter.subscribe();
        Toolbar.setTitle("User Details");
        calendarView.setOnDateChangedListener(this);
        calendarDaysWithEventIds = new HashMap<>();
        storageReference = FirebaseStorage.getInstance().getReference();
        emptyState.setVisibility(View.GONE);
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        contactIdsToCheck = sharedPreferences.getStringSet("contacts", contactIdsToCheck);
        displayLoadingState();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void displayUserInfo(User user, String userId) {

        emailUserCardview.setOnClickListener(v -> emailUser());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            //TODO: Replace this with a call to the contactsRepo
            if (contactIdsToCheck.contains(userId)) {
                addUserToContactsTextView.setText(R.string.user_detail_contact_already_added);
            } else {
                addUserToContactsTextView.setText(getString(R.string.add_user_to_contacts, user.getUsername()));
                addUserToContactsCardview.setOnClickListener(v -> userDetailPresenter.addContactToUser(userID, FirebaseAuth.getInstance().getCurrentUser().getUid()));
            }
        } else {
            addUserToContactsTextView.setText(String.format(getString(R.string.add_user_as_contact_val_prop), user.getUsername()));
        }


        setSupportActionBar(Toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (user.getUsername() != null) {
            String toolbarText = getString(R.string.user_detail_toolbar, user.getUsername());
            getSupportActionBar().setTitle(toolbarText);
        }


        StringBuilder listString = new StringBuilder();
        usernameTextView.setText(user.getUsername());
        cityStateTextView.setText(user.getCitystate());
        bioTextView.setText(user.getBio());
        emailUserTextView.setText(getString(R.string.email_user, user.getUsername()));
        userEmailAddress = user.getEmail();

        final StorageReference profileImageReference = storageReference.child("images/" + userID);
        profileImageReference
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Picasso.with(getApplicationContext())
                            .load(uri)
                            .resize(148, 148)
                            .centerCrop()
                            .transform(new CircleTransform())
                            .into(profileImage);
                    profileImageProgressbar.setVisibility(View.GONE);
                }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(getApplicationContext(), "Profile Image Unavailable", Toast.LENGTH_SHORT).show();
            profileImage.setImageDrawable(getDrawable(R.drawable.ic_profile_black_24dp));
            profileImageProgressbar.setVisibility(View.GONE);
        });

        if (user.getTags() != null) {
            for (String s : user.getTags()) {
                listString.append(s).append(", ");
            }
            String tagsText = listString.toString().replaceAll(", $", "");
            tagsTextView.setText(tagsText);
        }
        contentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayUserEvents(EventDetail event, String eventId) {
        String[] s = event.getDate().split("-");
        int year = Integer.parseInt(s[0]);
        // I have to do weird logic on the month because months are 0 indexed
        // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
        int month = Integer.parseInt(s[1]) - 1;
        int day = Integer.parseInt(s[2]);
        CalendarDay calendarDay = CalendarDay.from(year, month, day);

        for (Map.Entry<String, Boolean> userIsAttending : event.getUsers().entrySet()) {
            acceptedEventsCalendarDays.add(calendarDay);
            calendarDaysWithEventIds.put(calendarDay, eventId);
            calendarView.addDecorator(new EventDecorator(userIsAttending.getValue(), acceptedEventsCalendarDays, getApplicationContext()));
        }
    }

    @Override
    public void presentError(String id) {
        contentView.setVisibility(View.GONE);
        emptyStateButton.setVisibility(View.GONE);
        emptyStateImageView.setImageDrawable(getDrawable(R.drawable.ic_error_empty_state));
        emptyStateTextHeader.setText("There was a problem loading the user");
        emptyStateTextSubHeader.setText(String.format("Error fetching userID %s", id));
        emptyState.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayLoadingState() {
        contentView.setVisibility(View.GONE);
    }


    @Override
    public void presentSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
        if (acceptedEventsCalendarDays.contains(calendarDay)) {
            Intent intent = new Intent(this, EventDetailActivity.class);
            intent.putExtra("eventId", calendarDaysWithEventIds.get(calendarDay));
            startActivity(intent);
        }
    }

    private void emailUser() {

        if (userEmailAddress != null) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmailAddress});
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                presentError("Unable to email user");
            }
        }
    }
}
