package com.bookyrself.bookyrself.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bookyrself.bookyrself.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by benmedcalf on 3/10/18.
 */

public class EventDecorator implements DayViewDecorator {
    private final Context context;
    private final int color;
    private final HashSet<CalendarDay> dates;

    public EventDecorator(int color, Collection<CalendarDay> dates, @NonNull Context context) {
        this.color = color;
        this.dates = new HashSet<>(dates);
        this.context = context;
    }


    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(context.getDrawable(R.drawable.ic_circumference));
    }
}
