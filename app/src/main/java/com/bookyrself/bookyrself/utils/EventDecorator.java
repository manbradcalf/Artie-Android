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
    private final boolean isEventInviteAccepted;
    private final HashSet<CalendarDay> dates;

    public EventDecorator(boolean isEventInviteAccepted, Collection<CalendarDay> dates, @NonNull Context context) {
        this.isEventInviteAccepted = isEventInviteAccepted;
        this.dates = new HashSet<>(dates);
        this.context = context;
    }


    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        if (isEventInviteAccepted) {
            view.setBackgroundDrawable(context.getDrawable(R.drawable.calendar_day_event_invite_accepted_background));
        } else {
            view.setBackgroundDrawable(context.getDrawable(R.drawable.calendar_day_event_invite_not_accepted_background));
        }
    }
}
