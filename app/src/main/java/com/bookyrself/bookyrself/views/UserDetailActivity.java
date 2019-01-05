package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.graphics.Color;
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
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.presenters.EventsPresenter;
import com.bookyrself.bookyrself.presenters.UserDetailPresenter;
import com.bookyrself.bookyrself.utils.CircleTransform;
import com.bookyrself.bookyrself.utils.EventDecorator;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by benmedcalf on 1/13/18.
 */

public class UserDetailActivity extends AppCompatActivity implements UserDetailPresenter.UserDetailPresenterListener,
        EventsPresenter.CalendarPresenterListener, OnDateSelectedListener {

    private CardView emailUserCardview;
    private CardView addUserToContactsCardview;
    private HashSet<CalendarDay> calendarDays;
    private HashMap<CalendarDay, String> calendarDaysWithEventIds;
    private TextView usernameTextView;
    private TextView cityStateTextView;
    private TextView tagsTextView;
    private TextView urlTextView;
    private TextView bioTextView;
    private ImageView profileImage;

    private UserDetailPresenter userDetailPresenter;
    private ProgressBar profileImageProgressbar;
    private ProgressBar contentProgressBar;
    private TextView emailUserTextView;
    private TextView addUserToContactsTextView;
    private String userEmailAddress;
    private String userID;
    private Toolbar Toolbar;
    private EventsPresenter eventsPresenter;
    private MaterialCalendarView calendarView;
    private RelativeLayout contentView;
    private StorageReference storageReference;
    private View emptyState;
    private TextView emptyStateTextHeader;
    private TextView emptyStateTextSubHeader;
    private ImageView emptyStateImageView;
    private Button emptyStateButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        userDetailPresenter = new UserDetailPresenter(this);
        userID = getIntent().getStringExtra("userId");
        userDetailPresenter.getUserInfo(userID);
        eventsPresenter = new EventsPresenter(this);
        eventsPresenter.loadUsersEventInfo(userID);
        Toolbar = findViewById(R.id.toolbar_user_detail);
        Toolbar.setTitle("User Details");
        calendarView = findViewById(R.id.user_detail_calendar);
        contentView = findViewById(R.id.user_detail_content);
        calendarView.setOnDateChangedListener(this);
        calendarDays = new HashSet<>();
        calendarDaysWithEventIds = new HashMap<>();
        storageReference = FirebaseStorage.getInstance().getReference();
        emptyState = findViewById(R.id.user_detail_empty_state);
        emptyStateButton = findViewById(R.id.empty_state_button);
        emptyStateImageView = findViewById(R.id.empty_state_image);
        emptyStateTextHeader = findViewById(R.id.empty_state_text_header);
        emptyStateTextSubHeader = findViewById(R.id.empty_state_text_subheader);
        emptyState.setVisibility(View.GONE);
        loadingState();
    }

    @Override
    public void userInfoReady(User response) {

        // Show the user details now that they're loaded
        usernameTextView = findViewById(R.id.username_user_detail_activity);
        cityStateTextView = findViewById(R.id.city_state_user_detail_activity);
        tagsTextView = findViewById(R.id.tags_user_detail_activity);
        urlTextView = findViewById(R.id.user_url_user_detail_activity);
        profileImageProgressbar = findViewById(R.id.profile_image_progressbar);
        profileImage = findViewById(R.id.profile_image_user_detail_activity);
        bioTextView = findViewById(R.id.bio_body_user_detail_activity);
        emailUserTextView = findViewById(R.id.message_user_detail_activity_text);
        emailUserCardview = findViewById(R.id.message_user_detail_activity_card);
        emailUserCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailUser();
            }
        });

        addUserToContactsCardview = findViewById(R.id.add_user_to_contacts_card);
        addUserToContactsTextView = findViewById(R.id.add_user_to_contacts_textview);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            addUserToContactsCardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userDetailPresenter.addContactToUser(FirebaseAuth.getInstance().getCurrentUser().getUid(), userID);
                }
            });
        } else {
            addUserToContactsTextView.setText("Log in to add this user to your contacts!");
        }


        setSupportActionBar(Toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (response.getUsername() != null) {
            String toolbarText = getString(R.string.user_detail_toolbar, response.getUsername());
            getSupportActionBar().setTitle(toolbarText);
        }


        StringBuilder listString = new StringBuilder();
        usernameTextView.setText(response.getUsername());
        cityStateTextView.setText(response.getCitystate());
        bioTextView.setText(response.getBio());
        emailUserTextView.setText(getString(R.string.email_user, response.getUsername()));
        addUserToContactsTextView.setText(getString(R.string.add_user_to_contacts, response.getUsername()));
        userEmailAddress = response.getEmail();

        // Android Studio or gradle is being weird and not recognizing Task
        // However everything compiles and runs fine
        final StorageReference profileImageReference = storageReference.child("images/" + userID);
        profileImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getApplicationContext())
                        .load(uri)
                        .resize(148, 148)
                        .centerCrop()
                        .transform(new CircleTransform())
                        .into(profileImage);
                profileImageProgressbar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(getApplicationContext(), "image not dowloaded", Toast.LENGTH_SHORT).show();
                profileImage.setImageDrawable(getDrawable(R.drawable.ic_profile_black_24dp));
                profileImageProgressbar.setVisibility(View.GONE);
            }
        });

        if (response.getTags() != null) {
            for (String s : response.getTags()) {
                listString.append(s + ", ");
            }
            tagsTextView.setText(listString.toString());
        }
        contentView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
    public void loadingState() {
        contentView.setVisibility(View.GONE);
    }

    //TODO: I am using this method in both UserDetailActivity and EventDetailActivity presenters. I should consolidate
    @Override
    public void emailUser() {

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

    @Override
    public void presentSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void selectEventOnCalendar(String eventId) {

    }

    @Override
    public void goToEventDetail(String eventId) {

    }

    @Override
    public void userEventsReady(List<Event> events) {

    }

    @Override
    public void userseventinfoready(HashMap<String, EventDetail> events) {
        if (events != null) {
            for (Map.Entry<String, EventDetail> pair : events.entrySet()) {
                String[] s = events.get(pair.getKey()).getDate().split("-");
                int year = Integer.parseInt(s[0]);
                // I have to do weird logic on the month because months are 0 indexed
                // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
                int month = Integer.parseInt(s[1]) - 1;
                int day = Integer.parseInt(s[2]);
                CalendarDay calendarDay = CalendarDay.from(year, month, day);
                calendarDays.add(calendarDay);
                calendarDaysWithEventIds.put(calendarDay, pair.getKey());
            }
            if (calendarDays.size() == events.size()) {
                EventDecorator decorator = new EventDecorator(Color.BLUE, calendarDays, this);
                calendarView.addDecorator(decorator);
            }
        }
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        if (calendarDays.contains(date)) {
            Intent intent = new Intent(this, EventDetailActivity.class);
            intent.putExtra("eventId", calendarDaysWithEventIds.get(date));
            startActivity(intent);
        }
    }
}