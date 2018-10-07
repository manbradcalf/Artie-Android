package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.presenters.EventsPresenter;
import com.bookyrself.bookyrself.utils.EventDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventsFragment extends Fragment implements OnDateSelectedListener, EventsPresenter.CalendarPresenterListener {

    @BindView(R.id.events_calendar)
    MaterialCalendarView calendarView;

    @BindView(R.id.event_creation_fab)
    FloatingActionButton fab;

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
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        presenter = new EventsPresenter(this);
        presenter.loadUserEvents("20");
        calendarView.setOnDateChangedListener(this);
        calendarDaysWithEventIds = new HashMap<>();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EventCreationActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
        Log.i("calendarDay = ", calendarDay.toString());
        if (calendarDays.contains(calendarDay)) {
            Intent intent = new Intent(getActivity(), EventDetailActivity.class);
            intent.putExtra("eventId", calendarDaysWithEventIds.get(calendarDay));
            intent.putExtra("imgUrl", "https://image.flaticon.com/icons/svg/223/223222.svg");
            startActivity(intent);
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
                calendarView.addDecorator(new EventDecorator(Color.BLUE, calendarDays, getActivity()));
            }
        }
    }


