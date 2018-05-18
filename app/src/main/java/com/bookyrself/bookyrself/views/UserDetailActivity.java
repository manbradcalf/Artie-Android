package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.models.SearchResponseUsers._source;
import com.bookyrself.bookyrself.presenters.CalendarPresenter;
import com.bookyrself.bookyrself.presenters.UserDetailPresenter;
import com.bookyrself.bookyrself.utils.EventDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by benmedcalf on 1/13/18.
 */

public class UserDetailActivity extends AppCompatActivity implements UserDetailPresenter.UserDetailPresenterListener,
        CalendarPresenter.CalendarPresenterListener, OnDateSelectedListener {

    private CardView emailUserCardview;
    private CardView addUserToContactsCardview;
    private List<CalendarDay> calendarDays = new ArrayList<>();
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
    private Toolbar Toolbar;
    private CalendarPresenter calendarPresenter;
    private MaterialCalendarView calendarView;
    private RelativeLayout contentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        userDetailPresenter = new UserDetailPresenter(this);
        userDetailPresenter.getUserInfo(getIntent().getStringExtra("userId"));
        calendarPresenter = new CalendarPresenter(this);
        calendarPresenter.loadUserEvents(getIntent().getStringExtra("userId"));
        Toolbar = findViewById(R.id.toolbar_user_detail);
        Toolbar.setTitle("User Details");
        calendarView = findViewById(R.id.user_detail_calendar);
        calendarView.setOnDateChangedListener(this);
        calendarDaysWithEventIds = new HashMap<>();
        loadingState();
    }

    @Override
    public void userInfoReady(_source response) {

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
        addUserToContactsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Add code that adds a user to your contacts list
            }
        });

        setSupportActionBar(Toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String toolbarText = getString(R.string.user_detail_toolbar, response.getUsername());
        getSupportActionBar().setTitle(toolbarText);

        StringBuilder listString = new StringBuilder();
        usernameTextView.setText(response.getUsername());
        cityStateTextView.setText(response.getCitystate());
        bioTextView.setText(response.getBio());
        emailUserTextView.setText(getString(R.string.email_user, response.getUsername()));
        addUserToContactsTextView.setText(getString(R.string.add_user_to_contacts, response.getUsername()));
        userEmailAddress = response.getEmail();
        Picasso.with(this)
                .load(response.getPicture())
                .into(profileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap imageBitmap = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();
                        RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                        imageDrawable.setCircular(true);
                        imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                        profileImageProgressbar.setVisibility(View.GONE);
                        profileImage.setImageDrawable(imageDrawable);
                    }

                    @Override
                    public void onError() {
                        Log.e(this.getClass().toString(), "didn't load image");
                    }
                });
        for (String s : response.getTags()) {
            listString.append(s + ", ");
        }

        tagsTextView.setText(listString.toString());
        contentView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void presentError() {
        //TODO: This should be a legit empty state
//        emptyState.setVisibility(View.VISIBLE);
    }

    @Override
    public void loadingState() {
        contentView = findViewById(R.id.user_detail_content);
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
                presentError();
            }
        }
    }

    @Override
    public void selectEventOnCalendar(String eventId) {

    }

    @Override
    public void goToEventDetail(String eventId) {

    }

    @Override
    public void eventsReady(List<Event> events) {
        for (int i = 0; i < events.size(); i++) {
            String[] s = events.get(i).getDate().split("-");
            int year = Integer.parseInt(s[0]);
            // I have to do weird logic on the month because months are 0 indexed
            // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
            int month = Integer.parseInt(s[1]) - 1;
            int day = Integer.parseInt(s[2]);
            CalendarDay calendarDay = CalendarDay.from(year, month, day);
            calendarDays.add(calendarDay);
            calendarDaysWithEventIds.put(calendarDay, events.get(i).getId());
        }
        if (calendarDays.size() == events.size()) {
            calendarView.addDecorator(new EventDecorator(Color.BLUE, calendarDays, this));
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