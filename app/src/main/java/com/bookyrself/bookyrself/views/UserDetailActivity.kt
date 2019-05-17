package com.bookyrself.bookyrself.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.*
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.User.User
import com.bookyrself.bookyrself.presenters.UserDetailPresenter
import com.bookyrself.bookyrself.utils.CircleTransform
import com.bookyrself.bookyrself.utils.EventDecorator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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

/**
 * Created by benmedcalf on 1/13/18.
 */

class UserDetailActivity : AppCompatActivity(), UserDetailPresenter.UserDetailPresenterListener, OnDateSelectedListener {

    private var storageReference: StorageReference? = null
    private var userEmailAddress: String? = null
    private var userID: String? = null
    private var calendarDaysWithEventIds: HashMap<CalendarDay, String>? = null
    private var presenter: UserDetailPresenter? = null
    private val acceptedEventsCalendarDays = ArrayList<CalendarDay>()
    private val contactsRepository = MainActivity.getContactsRepo()

    // Should I have a presenter?
    private var compositeDisposable: CompositeDisposable? = null
    private val unavailableDates = ArrayList<CalendarDay>()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)


        storageReference = FirebaseStorage.getInstance().reference
        calendarDaysWithEventIds = HashMap()
        compositeDisposable = CompositeDisposable()
        userID = intent.getStringExtra("userId")
        presenter = UserDetailPresenter(userID!!, this)
        
        
        user_detail_calendar.setOnDateChangedListener(this)
        toolbar_user_detail.title = "User Details"
        user_detail_empty_state.visibility = View.GONE
        
        displayLoadingState()
    }

    public override fun onResume() {
        super.onResume()
        presenter!!.subscribe()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun displayUserInfo(user: User, userId: String) {


        setSupportActionBar(toolbar_user_detail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Set username as toolbar title
        if (user.username != null) {
            val toolbarText = getString(R.string.user_detail_toolbar, user.username)
            supportActionBar!!.title = toolbarText
        }

        // Set default contact card text
        add_user_to_contacts_textview.text = getString(R.string.add_user_to_contacts, user.username)

        // Set tags
        val listString = StringBuilder()
        if (user.tags != null) {
            for (s in user.tags) {
                listString.append(s).append(", ")
            }
            val tagsText = listString.toString().replace(", $".toRegex(), "")
            tags_user_detail_activity.setText(tagsText)
        }

        // Set unavailable dates
        if (user.unavailableDates != null) {
            for (date in user.unavailableDates.keys) {

                val s = date.split("-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                val year = Integer.parseInt(s[0])
                // I have to do weird logic on the month because months are 0 indexed
                // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
                val month = Integer.parseInt(s[1]) - 1
                val day = Integer.parseInt(s[2])

                val calendarDay = CalendarDay.from(year, month, day)
                unavailableDates.add(calendarDay)

                user_detail_calendar.addDecorator(EventDecorator(EventDecorator.DATE_UNAVAILABLE, unavailableDates, applicationContext))
            }
        }

        // Set username
        username_user_detail_activity.text = user.username

        // Set citystate
        city_state_user_detail_activity.text = user.citystate

        // Set bio
        bio_body_user_detail_activity.text = user.bio

        // Set email
        message_user_detail_activity_text.text = getString(R.string.email_user, user.username)
        userEmailAddress = user.email
        message_user_detail_activity_card!!.setOnClickListener { emailUser() }

        // Set URL
        user_url_user_detail_activity.isClickable = true
        user_url_user_detail_activity.movementMethod = LinkMovementMethod.getInstance()

        val linkedText = String.format("<a href=\"%s\">%s</a> ", "http://" + user.url, user.url)
        user_url_user_detail_activity.text = Html.fromHtml(linkedText)

        // Determine if this user is a contact
        if (FirebaseAuth.getInstance().currentUser != null) {

            // Since I'm signed in and thus able to add user as contact,
            // set the default click listener for the contact add button
            add_user_to_contacts_card.setOnClickListener { presenter!!.addContactToUser(userID!!, FirebaseAuth.getInstance().currentUser!!.uid) }

            // Check if this user is already contact and if so update the textview to portray that
            compositeDisposable!!.add(
                    contactsRepository
                            .getContactsForUser(FirebaseAuth.getInstance().uid)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
//                            .map<String>(Function<Entry<String, User>, String> { it.key })
                            .map { it.key }
                            .filter { s -> s == userId }
                            .subscribe({ s ->
                                add_user_to_contacts_textview.setText(R.string.user_detail_contact_already_added)
                                add_user_to_contacts_card.isClickable = false
                            },
                                    { throwable ->
                                        if (throwable.message != null) {
                                            Toast.makeText(this, throwable.message, Toast.LENGTH_SHORT).show()
                                        }
                                        throwable.printStackTrace()
                                    }))
        } else {
            add_user_to_contacts_textview.setText(R.string.contact_button_signed_out)
        }

        val profileImageReference = storageReference!!.child("images/users/" + userID!!)
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

    override fun displayUserEvent(event: EventDetail, eventId: String) {
        val s = event.date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val year = Integer.parseInt(s[0])
        // I have to do weird logic on the month because months are 0 indexed
        // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
        val month = Integer.parseInt(s[1]) - 1
        val day = Integer.parseInt(s[2])
        val calendarDay = CalendarDay.from(year, month, day)

        if (event.host.userId == userID) {
            // If the user is hosting this event
            // add this event to the user's calendar
            acceptedEventsCalendarDays.add(calendarDay)
            calendarDaysWithEventIds!![calendarDay] = eventId
            user_detail_calendar.addDecorator(EventDecorator(EventDecorator.INVITE_ACCEPTED, acceptedEventsCalendarDays, applicationContext))

        } else if (event.users != null) {
            // If there are users for this event
            if (!event.users.entries.isEmpty()) {
                // Loop through the users
                for ((key, value) in event.users) {
                    // If this event's user is the user we're viewing and they're attending
                    if (key == userID && value) {
                        // add this event to the user's calendar
                        acceptedEventsCalendarDays.add(calendarDay)
                        calendarDaysWithEventIds!![calendarDay] = eventId
                        if (value) {
                            user_detail_calendar.addDecorator(EventDecorator(EventDecorator.INVITE_ACCEPTED, acceptedEventsCalendarDays, applicationContext))
                        } else {
                            user_detail_calendar.addDecorator(EventDecorator(EventDecorator.INVITE_PENDING, acceptedEventsCalendarDays, applicationContext))
                        }

                    }
                }
            }
        }
    }

    override fun presentError(id: String) {
        user_detail_content.visibility = View.GONE
        empty_state_button.visibility = View.GONE
        empty_state_image.setImageDrawable(getDrawable(R.drawable.ic_error_empty_state))
        empty_state_text_header.setText(R.string.user_detail_error_header)
        empty_state_text_subheader.text = String.format("Error fetching userID %s", id)
        user_detail_empty_state.visibility = View.VISIBLE
    }

    override fun displayLoadingState() {
        user_detail_content.visibility = View.GONE
    }


    override fun presentSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    override fun onDateSelected(materialCalendarView: MaterialCalendarView, calendarDay: CalendarDay, b: Boolean) {
        if (acceptedEventsCalendarDays.contains(calendarDay)) {
            val intent = Intent(this, EventDetailActivity::class.java)
            intent.putExtra("eventId", calendarDaysWithEventIds!![calendarDay])
            startActivity(intent)
        }
    }

    private fun emailUser() {

        if (userEmailAddress != null) {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(userEmailAddress!!))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                presentError("Unable to email user")
            }
        }
    }
}
