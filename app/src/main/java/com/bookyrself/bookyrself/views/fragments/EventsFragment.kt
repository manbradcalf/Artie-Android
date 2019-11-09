package com.bookyrself.bookyrself.views.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.utils.EventDecorator
import com.bookyrself.bookyrself.viewmodels.EventsFragmentViewModel
import com.bookyrself.bookyrself.views.activities.EventCreationActivity
import com.bookyrself.bookyrself.views.activities.EventDetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import kotlinx.android.synthetic.main.fragment_events.*
import java.util.*
import kotlin.collections.HashMap

class EventsFragment : BaseFragment(), OnDateSelectedListener {

    lateinit var model: EventsFragmentViewModel
    private val acceptedEventsCalendarDays = ArrayList<CalendarDay>()
    private val pendingEventsCalendarDays = ArrayList<CalendarDay>()
    private val calendarDaysWithEventIds = HashMap<CalendarDay, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProviders.of(this,
                EventsFragmentViewModel.EventsFragmentViewModelFactory(activity!!.application))
                .get(EventsFragmentViewModel::class.java)

        FirebaseAuth.getInstance().addAuthStateListener {
            if (it.uid == null) {
                showSignedOutEmptyState(
                        getString(R.string.events_fragment_empty_state_signed_out_subheader),
                        activity!!.getDrawable(R.drawable.ic_no_events_black_24dp)
                )
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (FirebaseAuth.getInstance().uid != null) {
            setLayout()
            setListeners()
            model.load()
        }
    }

    private fun setListeners() {
        model.eventDetails.observe(this) { events ->
            if (events.isNotEmpty()) {
                showContent(true)
                for (event in events) {
                    eventDetailReturned(event.key, event.value)
                }
            } else {
                noEventDetailsReturned()
            }
        }
        model.errorMessage.observe(this) {
            presentError(it)
        }
    }

    private fun setLayout() {
        events_toolbar?.title = "Your Calendar"
        event_creation_fab?.setOnClickListener {
            val intent = Intent(activity, EventCreationActivity::class.java)
            startActivityForResult(intent, RC_EVENT_CREATION)
        }
        events_calendar?.setOnDateChangedListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        calendarDaysWithEventIds.clear()
        pendingEventsCalendarDays.clear()
        acceptedEventsCalendarDays.clear()
        events_calendar?.removeDecorators()
    }

    override fun onDateSelected(materialCalendarView: MaterialCalendarView, calendarDay: CalendarDay, b: Boolean) {
        if (acceptedEventsCalendarDays.contains(calendarDay)) {
            val intent = Intent(activity, EventDetailActivity::class.java)
            intent.putExtra("eventId", calendarDaysWithEventIds[calendarDay])
            startActivity(intent)
        } else if (pendingEventsCalendarDays.contains(calendarDay)) {
            val intent = Intent(activity, EventDetailActivity::class.java)
            intent.putExtra("eventId", calendarDaysWithEventIds[calendarDay])
            startActivity(intent)
        }
    }

    /**
     * @param event   The event detail of an event listed under a user's node in Firebase
     * @param eventId The event Id of the event
     */
    private fun eventDetailReturned(event: EventDetail, eventId: String) {
        showContent(true)
        showLoadingState(false)
        val s = event.date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val year = Integer.parseInt(s[0])
        // I have to do weird logic on the month because months are 0 indexed
        // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
        val month = Integer.parseInt(s[1]) - 1
        val day = Integer.parseInt(s[2])
        val calendarDay = CalendarDay.from(year, month, day)

        // If i'm the host, I'm attending
        if (event.host.userId == FirebaseAuth.getInstance().uid) {
            acceptedEventsCalendarDays.add(calendarDay)
            calendarDaysWithEventIds[calendarDay] = eventId
            events_calendar?.addDecorator(EventDecorator(EventDecorator.USER_IS_HOST, acceptedEventsCalendarDays, this.context!!))
        } else if (event.host.userId != FirebaseAuth.getInstance().uid && event.users != null) {

            // Loop through all users in the event detail
            // If the user is me
            // My id is not in the event because the invite was rejected and userId deleted from event
            for ((key, value) in event.users)
                if (key == FirebaseAuth.getInstance().uid) {

                    // and I am attending, set the calendarDay to be attending
                    if (value) {
                        acceptedEventsCalendarDays.add(calendarDay)
                        calendarDaysWithEventIds[calendarDay] = eventId
                        events_calendar?.addDecorator(EventDecorator(EventDecorator.INVITE_ACCEPTED, acceptedEventsCalendarDays, this.context!!))
                    } else if (!value) {
                        pendingEventsCalendarDays.add(calendarDay)
                        calendarDaysWithEventIds[calendarDay] = eventId
                        events_calendar?.addDecorator(EventDecorator(EventDecorator.INVITE_PENDING, pendingEventsCalendarDays, this.context!!))
                    }
                }
        }
    }

    private fun noEventDetailsReturned() {
        showContent(true)
        hideEmptyState()
    }

    //TODO: Should i update min api or somn
    @SuppressLint("RestrictedApi")
    override fun showContent(show: Boolean) {
        if (show) {
            showLoadingState(false)
            hideEmptyState()
            events_calendar?.visibility = View.VISIBLE
            event_creation_fab?.visibility = View.VISIBLE
        } else {
            events_calendar?.visibility = View.GONE
            event_creation_fab?.visibility = View.GONE
        }
    }

    @SuppressLint("RestrictedApi")
    override fun showLoadingState(show: Boolean) {
        if (show) {
            events_progressbar?.visibility = View.VISIBLE
        } else {
            events_progressbar?.visibility = View.GONE
        }
    }
}


