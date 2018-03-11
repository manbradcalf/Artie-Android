package com.bookyrself.bookyrself.views;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.bookyrself.bookyrself.utils.EventDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class CalendarActivity extends MainActivity implements OnDateSelectedListener {
    private MaterialCalendarView calendarView;
    private List<CalendarDay> events;

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
        calendarView = findViewById(R.id.events_calendar);
//        new ApiSimulator().executeOnExecutor(Executors.newSingleThreadExecutor());
        Calendar calendar = Calendar.getInstance();
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        Calendar calendar3 = Calendar.getInstance();
        calendar.set(2018, 2, 22);
        calendar1.set(2018, 2, 23);
        calendar2.set(2018, 2, 24);
        calendar3.set(2018, 3, 2);
        CalendarDay day = CalendarDay.from(calendar);
        CalendarDay day1 = CalendarDay.from(calendar1);
        CalendarDay day2 = CalendarDay.from(calendar2);
        CalendarDay day3 = CalendarDay.from(calendar3);
        ArrayList<CalendarDay> days = new ArrayList<>();
        days.add(day);
        days.add(day1);
        days.add(day2);
        days.add(day3);
        calendarView.addDecorator(new EventDecorator(Color.BLUE, days, this));

    }

    @Override
    void checkAuth() {

    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {

    }

    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -2);
            ArrayList<CalendarDay> dates = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                CalendarDay day = CalendarDay.from(calendar);
                dates.add(day);
                calendar.add(Calendar.DATE, 5);
            }

            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);

            if (isFinishing()) {
                return;
            }

            calendarView.addDecorator(new EventDecorator(Color.RED, calendarDays, CalendarActivity.this));
        }
    }
}
