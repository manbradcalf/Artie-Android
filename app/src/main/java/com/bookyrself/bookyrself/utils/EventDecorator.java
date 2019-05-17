package com.bookyrself.bookyrself.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bookyrself.bookyrself.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by benmedcalf on 3/10/18.
 */

public class EventDecorator implements DayViewDecorator {
    private final Context context;
    private final int eventType;
    private final HashSet<CalendarDay> dates;
    public static final int INVITE_PENDING = 0;
    public static final int INVITE_ACCEPTED = 1;
    public static final int USER_IS_HOST = 2;
    public static final int DATE_UNAVAILABLE = 3;

    public EventDecorator(int eventType, Collection<CalendarDay> dates, @NonNull Context context) {
        this.eventType = eventType;
        this.dates = new HashSet<>(dates);
        this.context = context;
    }


    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {

        switch (eventType) {
            case INVITE_PENDING:
                view.setBackgroundDrawable(context.getDrawable(R.drawable.calendar_day_event_invite_pending_background));
                break;
            case INVITE_ACCEPTED:
            case USER_IS_HOST:
                view.setBackgroundDrawable(context.getDrawable(R.drawable.calendar_day_event_invite_accepted_background));
                break;
            case DATE_UNAVAILABLE:
                view.setBackgroundDrawable(context.getDrawable(R.drawable.calendar_day_date_unavailable_background));
                break;
        }
    }
}
