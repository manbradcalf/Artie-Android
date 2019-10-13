package com.bookyrself.bookyrself.views.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.utils.EventDecorator
import com.bookyrself.bookyrself.viewmodels.EventsFragmentViewModel
import com.bookyrself.bookyrself.viewmodels.EventsFragmentViewModel.EventsFragmentViewModelFactory
import com.bookyrself.bookyrself.views.activities.EventCreationActivity
import com.bookyrself.bookyrself.views.activities.EventDetailActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import kotlinx.android.synthetic.main.empty_state_template.*
import kotlinx.android.synthetic.main.fragment_events.*
import java.util.*
import kotlin.collections.HashMap

class EventsFragment : BaseFragment(), OnDateSelectedListener {

    lateinit var model: EventsFragmentViewModel
    private val acceptedEventsCalendarDays = ArrayList<CalendarDay>()
    private val pendingEventsCalendarDays = ArrayList<CalendarDay>()
    private val calendarDaysWithEventIds = HashMap<CalendarDay, String>()

    private fun init() {
        // Set up view
        events_toolbar?.title = "Your Calendar"
        event_creation_fab?.setOnClickListener {
            val intent = Intent(activity, EventCreationActivity::class.java)
            startActivityForResult(intent, RC_EVENT_CREATION)
        }
        events_calendar?.setOnDateChangedListener(this)

        model = ViewModelProviders.of(this,
                EventsFragmentViewModelFactory()).get(EventsFragmentViewModel::class.java)

        model.eventDetailsHashMap.observe(this) { events ->
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

        model.signedOutMessage.observe(this) {
            if (FirebaseAuth.getInstance().uid == null) {
                showContent(false)
                showSignedOutEmptyState()
            }
        }
    }

    override fun onResume() {
        init()
        super.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        calendarDaysWithEventIds.clear()
        pendingEventsCalendarDays.clear()
        acceptedEventsCalendarDays.clear()
        events_calendar?.removeDecorators()
    }

    // Re-init after signing in or creating event
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            init()
        }
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
                    }// or if I'm not attending yet, set invite to pending
                } else
                    Log.e("Test", "User not attending")
        }// If I'm not hosting and there are users invited
    }

    private fun noEventDetailsReturned() {
        showContent(true)
        hideEmptyState()
    }

    override fun presentError(error: String) {
        showContent(false)
        showEmptyState(getString(R.string.error_header), error, "", activity!!.getDrawable(R.drawable.ic_error_empty_state))
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

    override fun showLoadingState(show: Boolean) {
        if (show) {
            events_progressbar?.visibility = View.VISIBLE
        } else {
            events_progressbar?.visibility = View.INVISIBLE
        }
    }

    private fun showEmptyState(header: String, subHeader: String, buttonText: String, image: Drawable?) {
        showContent(false)
        showLoadingState(false)
        empty_state_view?.visibility = View.VISIBLE
        empty_state_image?.visibility = View.VISIBLE
        empty_state_text_header?.visibility = View.VISIBLE
        empty_state_text_subheader?.visibility = View.VISIBLE

        empty_state_image?.setImageDrawable(image)
        empty_state_text_header?.text = header
        empty_state_text_subheader?.text = subHeader

        if (buttonText == "") {
            empty_state_button?.visibility = View.GONE
        } else {
            empty_state_button?.visibility = View.VISIBLE
            empty_state_button?.text = buttonText
            empty_state_button?.setOnClickListener {
                val providers = listOf(AuthUI.IdpConfig.GoogleBuilder().build(),
                        AuthUI.IdpConfig.EmailBuilder().build())
                // Authenticate
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false, true)
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN)
            }
        }
    }

    override fun showSignedOutEmptyState() {
        showEmptyState(
                getString(R.string.events_fragment_empty_state_signed_out_header),
                getString(R.string.events_fragment_empty_state_signed_out_subheader),
                getString(R.string.sign_in),
                activity!!.getDrawable(R.drawable.ic_calendar))
    }

    companion object {
        private const val RC_SIGN_IN = 123
        private const val RC_EVENT_CREATION = 456
    }
}


