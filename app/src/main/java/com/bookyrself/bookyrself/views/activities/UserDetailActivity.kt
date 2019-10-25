package com.bookyrself.bookyrself.views.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.serverModels.User.User
import com.bookyrself.bookyrself.utils.CircleTransform
import com.bookyrself.bookyrself.utils.EventDecorator
import com.bookyrself.bookyrself.viewmodels.UserDetailViewModel
import com.bookyrself.bookyrself.viewmodels.UserDetailViewModel.UserDetailViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_detail.*
import kotlinx.android.synthetic.main.empty_state_template.*
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by benmedcalf on 1/13/18.
 */
class UserDetailActivity : BaseActivity(), OnDateSelectedListener {
    private val contactsRepository = MainActivity.contactsRepo
    private val compositeDisposable = CompositeDisposable()
    private var userId: String? = null
    private var calendarDaysWithEventIds: HashMap<CalendarDay, String> = HashMap()
    private val acceptedEventsCalendarDays = ArrayList<CalendarDay>()
    private val unavailableCalendarDays = ArrayList<CalendarDay>()

    lateinit var model: UserDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        userId = intent.getStringExtra("userId")
        user_detail_calendar.setOnDateChangedListener(this)
        toolbar_user_detail.title = "User Details"
        user_detail_empty_state.visibility = View.GONE

        displayLoadingState()
        initData(userId)
    }

    private fun initData(userId: String?) {
        model = ViewModelProviders.of(this,
                UserDetailViewModelFactory(userId!!)).get(UserDetailViewModel::class.java)

        model.user.observe(this) {
            displayUserInfo(it, userId)
        }

        model.events.observe(this) {
            for (event in it) {
                displayUserEvent(event.key, event.value)
            }
        }

        model.contactWasAdded.observe(this) {
            presentSuccessForContactAdded()
        }

        model.errorMessage.observe(this) {
            presentError(it)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun displayUserInfo(user: User?, userId: String?) {
        setSupportActionBar(toolbar_user_detail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = getString(R.string.user_detail_toolbar, user?.username)

        // Set default contact card text
        add_user_to_contacts_textview.text = getString(R.string.add_user_to_contacts, user?.username)

        // Set tags
        val listString = StringBuilder()
        user?.tags?.forEach { tag ->
            listString.append(tag).append(", ")
        }
        val tagsText = listString.toString().replace(", $".toRegex(), "")

        tags_user_detail_activity.text = tagsText
        username_user_detail_activity.text = user?.username
        city_state_user_detail_activity.text = user?.citystate
        bio_body_user_detail_activity.text = user?.bio
        message_user_detail_activity_text.text = getString(R.string.email_user, user?.username)
        message_user_detail_activity_card?.setOnClickListener { emailUser() }
        user_url_user_detail_activity.isClickable = true
        user_url_user_detail_activity.movementMethod = LinkMovementMethod.getInstance()

        val linkedText = String.format("<a href=\"%s\">%s</a> ", "http://" + user?.url, user?.url)
        user_url_user_detail_activity.text = Html.fromHtml(linkedText)

        // Set unavailable dates
        user?.unavailableDates?.keys?.forEach { date ->

            val s = date.split("-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val year = Integer.parseInt(s[0])
            // I have to do weird logic on the month because months are 0 indexed
            // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
            val month = Integer.parseInt(s[1]) - 1
            val day = Integer.parseInt(s[2])

            val calendarDay = CalendarDay.from(year, month, day)
            unavailableCalendarDays.add(calendarDay)

            user_detail_calendar.addDecorator(EventDecorator(EventDecorator.DATE_UNAVAILABLE, unavailableCalendarDays, applicationContext))

        }


        // If I'm signed in
        if (FirebaseAuth.getInstance().uid != null) {

            // Check if this user is already contact and if so update the textview to portray that
            // TODO Leftover from old rxJava ways. refactor repo layer and this block
            compositeDisposable.add(
                    contactsRepository
                            .getContactsForUser(FirebaseAuth.getInstance().uid!!)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .map { it.key }
                            .filter { s -> s == userId }
                            .subscribe({
                                add_user_to_contacts_textview.setText(R.string.user_detail_contact_already_added)
                                add_user_to_contacts_card.isClickable = false
                            },
                                    { throwable ->
                                        if (throwable.message != null) {
                                            Toast.makeText(this, throwable.message, Toast.LENGTH_SHORT).show()
                                        }
                                        throwable.printStackTrace()
                                    }))

            // Since I'm signed in and thus able to add user as contact,
            // set the default click listener for the contact add button
            add_user_to_contacts_card.setOnClickListener {
                model.addContactToUser(userId!!, FirebaseAuth.getInstance().uid!!)
            }
        } else {
            // If I'm signed out
            //TODO: Add intent to login here
            add_user_to_contacts_textview.setText(R.string.contact_button_signed_out)
        }

        val profileImageReference = imageStorage.child("images/users/" + this.userId!!)
        profileImageReference
                .downloadUrl
                .addOnSuccessListener { uri ->
                    Picasso.with(applicationContext)
                            .load(uri)
                            .resize(148, 148)
                            .centerCrop()
                            .transform(CircleTransform())
                            .into(profile_image_user_detail_activity)
                    profile_image_progressbar.visibility = View.GONE
                }.addOnFailureListener { exception ->
                    // Handle any errors
                    Toast.makeText(applicationContext, "Profile Image Unavailable", Toast.LENGTH_SHORT).show()
                    profile_image_user_detail_activity!!.setImageDrawable(getDrawable(R.drawable.ic_profile_black_24dp))
                    profile_image_progressbar.visibility = View.GONE
                }

        user_detail_content.visibility = View.VISIBLE
    }

    private fun displayUserEvent(event: EventDetail, eventId: String) {
        val s = event.date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val year = Integer.parseInt(s[0])
        // I have to do weird logic on the month because months are 0 indexed
        // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
        val month = Integer.parseInt(s[1]) - 1
        val day = Integer.parseInt(s[2])
        val calendarDay = CalendarDay.from(year, month, day)

        if (event.host.userId == userId) {
            // If the user is hosting this event
            // add this event to the user's calendar
            acceptedEventsCalendarDays.add(calendarDay)
            calendarDaysWithEventIds[calendarDay] = eventId
            user_detail_calendar.addDecorator(EventDecorator(EventDecorator.INVITE_ACCEPTED, acceptedEventsCalendarDays, applicationContext))

        } else if (event.users != null) {
            // If there are users for this event
            if (event.users.entries.isNotEmpty()) {
                // Loop through the users
                for ((eventUserId, isAttending) in event.users) {
                    // If this event's user is the user we're viewing and they're attending
                    if (eventUserId == userId && isAttending) {
                        // add this event to the user's calendar
                        acceptedEventsCalendarDays.add(calendarDay)
                        calendarDaysWithEventIds[calendarDay] = eventId
                        if (isAttending) {
                            user_detail_calendar.addDecorator(EventDecorator(EventDecorator.INVITE_ACCEPTED, acceptedEventsCalendarDays, applicationContext))
                        } else {
                            user_detail_calendar.addDecorator(EventDecorator(EventDecorator.INVITE_PENDING, acceptedEventsCalendarDays, applicationContext))
                        }
                    }
                }
            }
        }
    }

    private fun displayLoadingState() {
        user_detail_content.visibility = View.GONE
    }

    private fun emailUser() {

        if (model.user.value?.email != null) {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(model.user.value?.email!!))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                presentError("Unable to email user")
            }
        }
    }


    private fun presentSuccessForContactAdded() {
        Toast.makeText(this, "Contact successfully added!", Toast.LENGTH_SHORT).show()
    }

    override fun presentError(message: String) {
        user_detail_content.visibility = View.GONE
        empty_state_button.visibility = View.GONE
        empty_state_image.setImageDrawable(getDrawable(R.drawable.ic_error_empty_state))
        empty_state_text_header.setText(R.string.user_detail_error_header)
        empty_state_text_subheader.text = message
        user_detail_empty_state.visibility = View.VISIBLE
    }

    override fun onDateSelected(widget: MaterialCalendarView, calendarDay: CalendarDay, selected: Boolean) {
        if (acceptedEventsCalendarDays.contains(calendarDay)) {
            val intent = Intent(this, EventDetailActivity::class.java)
            intent.putExtra("eventId", calendarDaysWithEventIds[calendarDay])
            startActivity(intent)
        }
    }

}
