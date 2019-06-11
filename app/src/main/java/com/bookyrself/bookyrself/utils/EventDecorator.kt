package com.bookyrself.bookyrself.utils

import android.content.Context
import com.bookyrself.bookyrself.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

/**
 * Created by benmedcalf on 3/10/18.
 */

class EventDecorator(private val eventType: Int, dates: Collection<CalendarDay>,
                     private val context: Context) : DayViewDecorator {

    private val dates: HashSet<CalendarDay> = HashSet(dates)


    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        when (eventType) {
            INVITE_PENDING -> view.setBackgroundDrawable(context.getDrawable(R.drawable.calendar_day_event_invite_pending_background)!!)
            INVITE_ACCEPTED, USER_IS_HOST -> view.setBackgroundDrawable(context.getDrawable(R.drawable.calendar_day_event_invite_accepted_background)!!)
            DATE_UNAVAILABLE -> view.setBackgroundDrawable(context.getDrawable(R.drawable.calendar_day_date_unavailable_background)!!)
        }
    }

    companion object {
        const val INVITE_PENDING = 0
        const val INVITE_ACCEPTED = 1
        const val USER_IS_HOST = 2
        const val DATE_UNAVAILABLE = 3
    }
}
