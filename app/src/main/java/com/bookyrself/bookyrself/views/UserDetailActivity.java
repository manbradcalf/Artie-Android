package com.bookyrself.bookyrself.views;

import android.content.Intent;
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
import com.bookyrself.bookyrself.data.Contacts.ContactsRepository;
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
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

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
    private ContactsRepository contactsRepository = MainActivity.getContactsRepo();

    // Should I have a presenter?
    private CompositeDisposable compositeDisposable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        ButterKnife.bind(this);

        compositeDisposable = new CompositeDisposable();
        userID = getIntent().getStringExtra("userId");
        userDetailPresenter = new UserDetailPresenter(userID, this);
        userDetailPresenter.subscribe();
        Toolbar.setTitle("User Details");
        calendarView.setOnDateChangedListener(this);
        calendarDaysWithEventIds = new HashMap<>();
        storageReference = FirebaseStorage.getInstance().getReference();
        emptyState.setVisibility(View.GONE);
        displayLoadingState();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void displayUserInfo(User user, String userId) {


        setSupportActionBar(Toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set username as toolbar title
        if (user.getUsername() != null) {
            String toolbarText = getString(R.string.user_detail_toolbar, user.getUsername());
            getSupportActionBar().setTitle(toolbarText);
        }

        // Set default contact card text
        addUserToContactsTextView.setText(getString(R.string.add_user_to_contacts, user.getUsername()));

        // Set tags
        StringBuilder listString = new StringBuilder();
        if (user.getTags() != null) {
            for (String s : user.getTags()) {
                listString.append(s).append(", ");
            }
            String tagsText = listString.toString().replaceAll(", $", "");
            tagsTextView.setText(tagsText);
        }

        // Set username
        usernameTextView.setText(user.getUsername());

        // Set citystate
        cityStateTextView.setText(user.getCitystate());

        // Set bio
        bioTextView.setText(user.getBio());

        // Set email
        emailUserTextView.setText(getString(R.string.email_user, user.getUsername()));
        userEmailAddress = user.getEmail();
        emailUserCardview.setOnClickListener(v -> emailUser());

        // Determine if this user is a contact
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            // Since I'm signed in and thus able to add user as contact,
            // set the default click listener for the contact add button
            addUserToContactsCardview.setOnClickListener(v -> userDetailPresenter.addContactToUser(userID, FirebaseAuth.getInstance().getCurrentUser().getUid()));

            // Check if this user is already contact and if so update the textview to portray that
            compositeDisposable.add(
                    contactsRepository
                            .getContactsForUser(FirebaseAuth.getInstance().getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(stringUserPair -> stringUserPair.first)
                            .filter(s -> s.equals(userId))
                            .subscribe(s -> {
                                        addUserToContactsTextView.setText(R.string.user_detail_contact_already_added);
                                        addUserToContactsCardview.setClickable(false);
                                    },
                                    throwable -> {
                                        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
        }

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

        contentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayUserEvent(EventDetail event, String eventId) {
        String[] s = event.getDate().split("-");
        int year = Integer.parseInt(s[0]);
        // I have to do weird logic on the month because months are 0 indexed
        // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
        int month = Integer.parseInt(s[1]) - 1;
        int day = Integer.parseInt(s[2]);
        CalendarDay calendarDay = CalendarDay.from(year, month, day);

        // If there are users for this event
        if (!event.getUsers().entrySet().isEmpty() && event.getUsers() != null) {
            // Loop through the users
            for (Map.Entry<String, Boolean> userIsAttending : event.getUsers().entrySet()) {
                // If this event's user is the user we're viewing and they're attending
                if (userIsAttending.getKey().equals(userID) && userIsAttending.getValue()) {
                    // add this event to the user's calendar
                    acceptedEventsCalendarDays.add(calendarDay);
                    calendarDaysWithEventIds.put(calendarDay, eventId);
                    calendarView.addDecorator(new EventDecorator(userIsAttending.getValue(), acceptedEventsCalendarDays, getApplicationContext()));
                }
            }
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
