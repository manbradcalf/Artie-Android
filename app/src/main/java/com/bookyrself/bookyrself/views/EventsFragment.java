package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.presenters.EventsFragmentPresenter;
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

public class EventsFragment extends Fragment implements BaseFragment, OnDateSelectedListener, EventsFragmentPresenter.EventsPresenterListener {

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
    @BindView(R.id.events_progressbar)
    ProgressBar progressBar;

    private static final int RC_SIGN_IN = 123;
    private static final int RC_EVENT_CREATION = 456;
    private EventsFragmentPresenter presenter;
    private List<CalendarDay> acceptedEventsCalendarDays = new ArrayList<>();
    private List<CalendarDay> pendingEventsCalendarDays = new ArrayList<>();
    private HashMap<CalendarDay, String> calendarDaysWithEventIds = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new EventsFragmentPresenter(this, getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Set up view
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        ButterKnife.bind(this, view);
        toolbar.setTitle("Your Calendar");

        fab.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), EventCreationActivity.class);
            startActivityForResult(intent, RC_EVENT_CREATION);
        });
        calendarView.setOnDateChangedListener(this);

        // Determine the view state by the auth state
        if (FirebaseAuth.getInstance().getUid() == null) {
            // Signed out
            showSignedOutEmptyState();
        } else {
            // Signed In, load events
            showContent(false);
            hideEmptyState();
            showLoadingState(true);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.subscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        calendarDaysWithEventIds.clear();
        pendingEventsCalendarDays.clear();
        acceptedEventsCalendarDays.clear();
        calendarView.removeDecorators();
    }

    /**
     *  Reload the calendar view after signing in or creating event
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            presenter.subscribe();
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

    /**
     * @param event   The event detail of an event listed under a user's node in Firebase
     * @param eventId The event Id of the event
     */
    @Override
    public void eventDetailReturned(EventDetail event, String eventId) {
        showContent(true);
        showLoadingState(false);
        String[] s = event.getDate().split("-");
        int year = Integer.parseInt(s[0]);
        // I have to do weird logic on the month because months are 0 indexed
        // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
        int month = Integer.parseInt(s[1]) - 1;
        int day = Integer.parseInt(s[2]);
        CalendarDay calendarDay = CalendarDay.from(year, month, day);

        // If i'm the host, I'm attending
        if (event.getHost().getUserId().equals(FirebaseAuth.getInstance().getUid())) {
            acceptedEventsCalendarDays.add(calendarDay);
            calendarDaysWithEventIds.put(calendarDay, eventId);
            calendarView.addDecorator(new EventDecorator(EventDecorator.USER_IS_HOST, acceptedEventsCalendarDays, this.getContext()));
        }
        // If I'm not hosting and there are users invited
        else if (!event.getHost().getUserId().equals(FirebaseAuth.getInstance().getUid()) && event.getUsers() != null) {

            // Loop through all users in the event detail
            // If the user is me
            // My id is not in the event because the invite was rejected and userId deleted from event
            for (Map.Entry<String, Boolean> userIsAttending : event.getUsers().entrySet())
                if (userIsAttending.getKey().equals(FirebaseAuth.getInstance().getUid())) {

                    // and I am attending, set the calendarDay to be attending
                    if (userIsAttending.getValue()) {
                        acceptedEventsCalendarDays.add(calendarDay);
                        calendarDaysWithEventIds.put(calendarDay, eventId);
                        calendarView.addDecorator(new EventDecorator(EventDecorator.INVITE_ACCEPTED, acceptedEventsCalendarDays, this.getContext()));
                    }

                    // or if I'm not attending yet, set invite to pending
                    else if (!userIsAttending.getValue()) {
                        pendingEventsCalendarDays.add(calendarDay);
                        calendarDaysWithEventIds.put(calendarDay, eventId);
                        calendarView.addDecorator(new EventDecorator(EventDecorator.INVITE_PENDING, pendingEventsCalendarDays, this.getContext()));
                    }
                } else Log.e("Test", "User not attending");
        }
    }

    @Override
    public void noEventDetailsReturned() {
        showContent(true);
    }

    @Override
    public void presentError(String error) {
        showContent(false);
        showEmptyState(getString(R.string.error_header), error, "", getActivity().getDrawable(R.drawable.ic_error_empty_state));
    }


    @Override
    public void showContent(boolean show) {
        if (show) {
            showLoadingState(false);
            hideEmptyState();
            calendarView.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
        } else {
            calendarView.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
        }
    }

    @Override
    public void showLoadingState(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void showEmptyState(String header, String subHeader, String buttonText, Drawable image) {
        showContent(false);
        showLoadingState(false);
        emptyState.setVisibility(View.VISIBLE);
        emptyStateImageView.setVisibility(View.VISIBLE);
        emptyStateTextHeader.setVisibility(View.VISIBLE);
        emptyStateTextSubHeader.setVisibility(View.VISIBLE);

        emptyStateImageView.setImageDrawable(image);
        emptyStateTextHeader.setText(header);
        emptyStateTextSubHeader.setText(subHeader);

        if (buttonText.equals("")) {
            emptyStateButton.setVisibility(View.GONE);
        } else {
            emptyStateButton.setVisibility(View.VISIBLE);
            emptyStateButton.setText(buttonText);
            emptyStateButton.setOnClickListener(view -> {
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
            });
        }
    }

    @Override
    public void hideEmptyState() {
        emptyStateButton.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        emptyStateTextHeader.setVisibility(View.GONE);
        emptyStateTextSubHeader.setVisibility(View.GONE);
    }

    @Override
    public void showSignedOutEmptyState() {
        showEmptyState(
                getString(R.string.events_fragment_empty_state_signed_out_header),
                getString(R.string.events_fragment_empty_state_signed_out_subheader),
                getString(R.string.sign_in),
                getActivity().getDrawable(R.drawable.ic_calendar));
    }
}


