package com.bookyrself.bookyrself.views;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.FrameLayout;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SearchResponseUsers.Event;
import com.bookyrself.bookyrself.presenters.CalendarPresenter;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.bookyrself.bookyrself.utils.EventDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.ramotion.paperonboarding.PaperOnboardingFragment;
import com.ramotion.paperonboarding.PaperOnboardingPage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class CalendarActivity extends MainActivity implements OnDateSelectedListener, CalendarPresenter.CalendarPresenterListener {
    private MaterialCalendarView calendarView;
    private CalendarPresenter presenter;
    private android.support.v4.app.FragmentManager fm;
    private FrameLayout fragmentContainer;

    @Override
    int getContentViewId() {
        return R.layout.activity_calendar;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_calendar;
    }

    @Override
    void setLayout() {

        presenter = new CalendarPresenter(this);
        presenter.loadUserCalender("20");
        calendarView = findViewById(R.id.events_calendar);
    }

    @Override
    void checkAuth() {

    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {

    }

    @Override
    public void selectEventOnCalendar(String eventId) {

    }

    @Override
    public void goToEventDetail(String eventId) {

    }

    @Override
    public void calendarReady(List<Event> events) {
        List<CalendarDay> calendarDays = new ArrayList<>();

        for (int i = 0; i < events.size(); i++) {
            String[] s = events.get(i).getDate().split("-");
            int year = Integer.parseInt(s[0]);
            // I have to do weird logic on the month because months are 0 indexed
            // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
            int month = Integer.parseInt(s[1]) - 1;
            int day = Integer.parseInt(s[2]);
//            calendar.set(year, month, day);
            CalendarDay calendarDay = CalendarDay.from(year, month, day);
            calendarDays.add(calendarDay);
        }
        if (calendarDays.size() == events.size()) {
            calendarView.addDecorator(new EventDecorator(Color.BLUE, calendarDays, this));
        }
    }
}
