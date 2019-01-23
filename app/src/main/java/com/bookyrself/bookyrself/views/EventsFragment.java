package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.presenters.EventsPresenter;
import com.bookyrself.bookyrself.utils.EventDecorator;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class EventsFragment extends Fragment implements OnDateSelectedListener, EventsPresenter.EventsPresenterListener {

    @BindView(R.id.events_calendar)
    MaterialCalendarView calendarView;
    @BindView(R.id.event_creation_fab)
    FloatingActionButton fab;
    @BindView(R.id.events_toolbar)
    Toolbar toolbar;
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

    private static final int RC_SIGN_IN = 123;
    private static final int RC_EVENT_CREATION = 456;
    private EventsPresenter presenter;
    private List<CalendarDay> acceptedEventsCalendarDays = new ArrayList<>();
    private List<CalendarDay> pendingEventsCalendarDays = new ArrayList<>();
    private HashMap<CalendarDay, String> calendarDaysWithEventIds;

    @Override
    public void onCreate(Bundle savedInsanceState) {
        super.onCreate(savedInsanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        ButterKnife.bind(this, view);
        showEmptyState(false);
        toolbar.setTitle("Your Calendar");
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        presenter = new EventsPresenter(this);
        presenter.loadUsersEventInfo(FirebaseAuth.getInstance().getUid());
        calendarView.setOnDateChangedListener(this);
        calendarDaysWithEventIds = new HashMap<>();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EventCreationActivity.class);
                startActivityForResult(intent, RC_EVENT_CREATION);
            }
        });

        //TODO: Move to presenter level
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    calendarView.removeDecorators();
                }
            }
        });
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            showContent(true);
        } else {
            showLoggedOutEmptyState();
        }
    }

    // Reload the CalendarView after creating a new event or signing in
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //TODO: Is this pointless duplication or good for readability & updatability?
                case RC_SIGN_IN:
                    presenter.loadUsersEventInfo(FirebaseAuth.getInstance().getUid());
                case RC_EVENT_CREATION:
                    presenter.loadUsersEventInfo(FirebaseAuth.getInstance().getUid());
            }
        }
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
        if (acceptedEventsCalendarDays.contains(calendarDay)) {
            Intent intent = new Intent(getActivity(), EventDetailActivity.class);
            intent.putExtra("eventId", calendarDaysWithEventIds.get(calendarDay));
            startActivity(intent);
        } else if (pendingEventsCalendarDays.contains(calendarDay)) {
            Intent intent = new Intent(getActivity(), EventDetailActivity.class);
            intent.putExtra("eventId", calendarDaysWithEventIds.get(calendarDay));
            startActivity(intent);
        }
    }

    @Override
    public void eventDetailReturned(EventDetail event, String eventId) {
        showEmptyState(false);
        showContent(true);
        String[] s = event.getDate().split("-");
        int year = Integer.parseInt(s[0]);
        // I have to do weird logic on the month because months are 0 indexed
        // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
        int month = Integer.parseInt(s[1]) - 1;
        int day = Integer.parseInt(s[2]);
        CalendarDay calendarDay = CalendarDay.from(year, month, day);

        // If I'm not already hosting
        if (!event.getHost().getUserId().equals(FirebaseAuth.getInstance().getUid())) {
            // Check all events for my ID. If it's there && true, set accepted. else, set pending.
            for (Map.Entry<String, Boolean> userIsAttending : event.getUsers().entrySet()) {
                if (userIsAttending.getValue()) {
                    acceptedEventsCalendarDays.add(calendarDay);
                    calendarDaysWithEventIds.put(calendarDay, eventId);
                    calendarView.addDecorator(new EventDecorator(true, acceptedEventsCalendarDays, this.getContext()));
                } else {
                    pendingEventsCalendarDays.add(calendarDay);
                    calendarDaysWithEventIds.put(calendarDay, eventId);
                    calendarView.addDecorator(new EventDecorator(false, pendingEventsCalendarDays, this.getContext()));
                }
            }
        }
        // If i'm the host, set me as "accepted"
        else {
            acceptedEventsCalendarDays.add(calendarDay);
            calendarDaysWithEventIds.put(calendarDay, eventId);
            calendarView.addDecorator(new EventDecorator(true, acceptedEventsCalendarDays, this.getContext()));
        }
    }

    @Override
    public void presentError(String error) {
        showContent(false);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            showLoggedOutEmptyState();
        } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            showUserHasNoEventsEmptyState();
        } else {
            Toast.makeText(getActivity(), "Unknown error: " + error, Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmptyState(boolean b) {
        if (b) {
            emptyState.setVisibility(View.VISIBLE);
            emptyStateButton.setVisibility(View.VISIBLE);
        } else {
            emptyState.setVisibility(View.GONE);
            emptyStateButton.setVisibility(View.GONE);
        }

    }

    private void showContent(boolean b) {
        if (b) {
            calendarView.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
        } else {
            calendarView.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
        }
    }

    private void showUserHasNoEventsEmptyState() {
        showContent(false);
        emptyState.setVisibility(View.VISIBLE);
        emptyStateButton.setVisibility(View.VISIBLE);
        emptyStateImageView.setImageDrawable(getActivity().getDrawable(R.drawable.ic_calendar));
        emptyStateTextHeader.setText("Add an event to get started!");
        emptyStateTextSubHeader.setText("Please sign in to create events.");
        emptyStateButton.setText("Sign In");
        emptyStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.EmailBuilder().build());
                // Authenticate
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false, true)
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN);
            }
        });
    }

    private void showLoggedOutEmptyState() {
        showContent(false);
        emptyState.setVisibility(View.VISIBLE);
        emptyStateButton.setVisibility(View.VISIBLE);
        emptyStateImageView.setImageDrawable(getActivity().getDrawable(R.drawable.ic_calendar));
        emptyStateTextHeader.setText("You are logged out!");
        emptyStateTextSubHeader.setText("Please sign in to view events.");
        emptyStateButton.setText("Sign In");
        emptyStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.EmailBuilder().build());
                // Authenticate
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false, true)
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN);
            }
        });
    }
}


