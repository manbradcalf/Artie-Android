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
import com.bookyrself.bookyrself.data.serverModels.user.User
import com.bookyrself.bookyrself.utils.EventDecorator
import com.bookyrself.bookyrself.viewmodels.UserDetailViewModel
import com.bookyrself.bookyrself.views.fragments.BaseFragment.Companion.RC_SIGN_IN
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
    private var calendarDaysWithEventIds: HashMap<CalendarDay, String> = HashMap()
    private val acceptedEventsCalendarDays = ArrayList<CalendarDay>()
    private val unavailableCalendarDays = ArrayList<CalendarDay>()
    lateinit var userDetailId: String
    lateinit var model: UserDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        setSupportActionBar(user_detail_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        user_detail_calendar.setOnDateChangedListener(this)
        user_detail_empty_state.visibility = View.GONE
        displayLoadingState()
        userDetailId = intent.getStringExtra("userId")
        setListeners(userDetailId)
    }

    private fun setListeners(userId: String) {
        model = ViewModelProviders.of(this,
                UserDetailViewModel.UserDetailViewModelFactory(application, userId))
                .get(UserDetailViewModel::class.java)

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
        model.load()
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun displayUserInfo(user: User?, userId: String?) {
        user_detail_collapsing_toolbar!!.title = user?.username
        user_detail_collapsing_toolbar!!.setExpandedTitleColor(resources.getColor(R.color.cardview_light_background))
        user_detail_collapsing_toolbar!!.setCollapsedTitleTextColor(resources.getColor(R.color.cardview_light_background))
        user_detail_empty_state_card_view.visibility = View.GONE

        // Set tags
        val listString = StringBuilder()
        user?.tags?.forEach { tag ->
            listString.append(tag).append(", ")
        }
        val tagsText = listString.toString().replace(", $".toRegex(), "")

        tags_user_detail_activity.text = tagsText
        city_state_user_detail_activity.text = user?.citystate
        bio_body_user_detail_activity.text = user?.bio
        email_user_detail_btn?.setOnClickListener { emailUser() }
        email_user_detail_btn.setCompoundDrawablesRelative(getDrawable(R.drawable.ic_mail_accent_24dp), null, null, null)
        user_url_user_detail_activity.isClickable = true
        user_url_user_detail_activity.movementMethod = LinkMovementMethod.getInstance()

        val linkedText = String.format("<a href=\"%s\">%s</a> ", "http://" + user?.url, user?.url)
        user_url_user_detail_activity.text = Html.fromHtml(linkedText)

        // Set unavailable dates
        user?.unavailableDates?.keys?.forEach { date ->
            val s = date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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
                                // TODO: Create a tint list so I can fill this button with the accent at this point
                                // https://developer.android.com/guide/topics/resources/color-list-resource.html
                                setSaveButton(true)
                            },
                                    { throwable ->
                                        if (throwable.message != null) {
                                            Toast.makeText(this, throwable.message, Toast.LENGTH_SHORT).show()
                                        }
                                        throwable.printStackTrace()
                                    }))

            // Since I'm signed in and thus able to add user as contact,
            // set the default click listener for the contact add button
            user_detail_save_btn.setOnClickListener {
                model.addContactToUser(userId!!, FirebaseAuth.getInstance().uid!!)
            }
        } else {
            // I'm signed out, so clicking the btn fires auth intent
            val intent = Intent(this, AuthenticationActivity::class.java)
            startActivityForResult(intent, RC_SIGN_IN)
        }

        val profileImageReference = imageStorage.child("images/users/$userDetailId")
        profileImageReference
                .downloadUrl
                .addOnSuccessListener { uri ->
                    Picasso.with(applicationContext)
                            .load(uri)
                            .resize(500, 500)
                            .centerCrop()
                            .into(user_image_detail)
                    profile_image_progressbar.visibility = View.GONE
                }.addOnFailureListener {
                    // Handle any errors
                    Toast.makeText(applicationContext, "Profile Image Unavailable", Toast.LENGTH_SHORT).show()
                    user_image_detail!!.setImageDrawable(getDrawable(R.drawable.ic_profile_black_24dp))
                    profile_image_progressbar.visibility = View.GONE
                }

        user_image_detail.setOnClickListener {
            val intent = Intent(this, ViewImageActivity::class.java)
            intent.putExtra("id", userId)
            intent.putExtra("imageType", "users")
            startActivity(intent)
        }
        user_detail_content.visibility = View.VISIBLE
    }

    private fun setSaveButton(userIsSaved: Boolean) {
        if (userIsSaved) {
            user_detail_save_btn.text = "Saved"
            user_detail_save_btn.isClickable = false
        } else {
            user_detail_save_btn.text = "Save"
            user_detail_save_btn.isClickable = true
        }
    }

    private fun displayUserEvent(event: EventDetail, eventId: String) {
        val s = event.date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val year = Integer.parseInt(s[0])
        // I have to do weird logic on the month because months are 0 indexed
        // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
        val month = Integer.parseInt(s[1]) - 1
        val day = Integer.parseInt(s[2])
        val calendarDay = CalendarDay.from(year, month, day)

        if (event.host.userId == userDetailId) {
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
                    if (eventUserId == userDetailId && isAttending) {
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
        setSaveButton(true)
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
