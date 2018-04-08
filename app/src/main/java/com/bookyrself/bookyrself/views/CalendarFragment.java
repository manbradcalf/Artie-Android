package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.presenters.CalendarPresenter;
import com.bookyrself.bookyrself.utils.EventDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CalendarFragment extends Fragment implements OnDateSelectedListener, CalendarPresenter.CalendarPresenterListener {
    private MaterialCalendarView calendarView;
    private CalendarPresenter presenter;
    List<CalendarDay> calendarDays = new ArrayList<>();
    HashMap<CalendarDay, String> calendarDaysWithEventIds;

    @Override
    public void onCreate(Bundle savedInsanceState) {
        super.onCreate(savedInsanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        presenter = new CalendarPresenter(this);
        presenter.loadUserCalender("20");
        calendarView = view.findViewById(R.id.events_calendar);
        calendarView.setOnDateChangedListener(this);
        calendarDaysWithEventIds = new HashMap<>();
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
        Log.i("calendarDay = ", calendarDay.toString());
        if (calendarDays.contains(calendarDay)) {
//            Toast.makeText(getActivity(), "u did it!", Toast.LENGTH_LONG).show();
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
    public void calendarReady(List<Event> events) {

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
