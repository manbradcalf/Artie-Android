package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.presenters.EventsPresenter;
import com.bookyrself.bookyrself.utils.EventDecorator;
import com.google.firebase.auth.FirebaseAuth;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventsFragment extends Fragment implements OnDateSelectedListener, EventsPresenter.EventsPresenterListener {

    private static final int RC_EVENT_CREATION = 1;
    @BindView(R.id.events_calendar)
    MaterialCalendarView calendarView;

    @BindView(R.id.event_creation_fab)
    FloatingActionButton fab;

    @BindView(R.id.events_toolbar)
    Toolbar toolbar;

    private EventsPresenter presenter;
    List<CalendarDay> calendarDays = new ArrayList<>();
    HashMap<CalendarDay, String> calendarDaysWithEventIds;

    @Override
    public void onCreate(Bundle savedInsanceState) {
        super.onCreate(savedInsanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        ButterKnife.bind(this, view);
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
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    calendarView.removeDecorators();
                }
            }
        });
    }

    // Refresh the CalendarView after creating a new event
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.loadUsersEventInfo(FirebaseAuth.getInstance().getUid());
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
        Log.i("calendarDay = ", calendarDay.toString());
        if (calendarDays.contains(calendarDay)) {
            Intent intent = new Intent(getActivity(), EventDetailActivity.class);
            intent.putExtra("eventId", calendarDaysWithEventIds.get(calendarDay));
            startActivity(intent);
        }
    }

    @Override
    public void eventReady(EventDetail event, String eventId) {
        String[] s = event.getDate().split("-");
        int year = Integer.parseInt(s[0]);
        // I have to do weird logic on the month because months are 0 indexed
        // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
        int month = Integer.parseInt(s[1]) - 1;
        int day = Integer.parseInt(s[2]);
        CalendarDay calendarDay = CalendarDay.from(year, month, day);
        calendarDays.add(calendarDay);
        calendarDaysWithEventIds.put(calendarDay, eventId);
        calendarView.addDecorator(new EventDecorator(Color.BLUE, calendarDays, this.getContext()));
    }

    @Override
    public void presentError(String error) {
        Toast.makeText(this.getContext(), error, Toast.LENGTH_SHORT).show();
    }
}


