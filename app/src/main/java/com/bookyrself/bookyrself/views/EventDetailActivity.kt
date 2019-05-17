package com.bookyrself.bookyrself.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.MiniUser
import com.bookyrself.bookyrself.presenters.EventDetailPresenter
import com.bookyrself.bookyrself.utils.CircleTransform
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_event_detail.*
import kotlinx.android.synthetic.main.empty_state_template.*
import kotlinx.android.synthetic.main.item_event_detail_user.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by benmedcalf on 11/22/17.
 */

class EventDetailActivity : AppCompatActivity(), EventDetailPresenter.EventDetailPresenterListener {

    private var presenter: EventDetailPresenter? = null
    private var storageReference: StorageReference? = null
    private var invitedUsers: MutableList<Map.Entry<String, MiniUser>>? = null
    private var adapter: UsersListAdapter? = null
    private var eventId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        // Get the storage reference for the thumbnails
        storageReference = FirebaseStorage.getInstance().reference

        // Set up the adapter
        invitedUsers = ArrayList()
        adapter = UsersListAdapter(this, invitedUsers!!, storageReference!!)
        event_detail_users_list.adapter = adapter

        // Set up initial loading state
        event_detail_linearlayout!!.visibility = View.GONE
        event_detail_empty_state.visibility = View.GONE
        eventId = intent.getStringExtra("eventId")
        setSupportActionBar(event_detail_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        showProgressbar(true)

        // Set up the presenter
        presenter = EventDetailPresenter(this, eventId!!)
        presenter!!.subscribe()
    }

    override fun showEventData(eventDetail: EventDetail) {

        showProgressbar(false)
        event_detail_collapsing_toolbar!!.title = eventDetail.eventname
        event_detail_collapsing_toolbar!!.setExpandedTitleColor(resources.getColor(R.color.cardview_light_background))
        event_detail_collapsing_toolbar!!.setCollapsedTitleTextColor(resources.getColor(R.color.cardview_light_background))

        // Set up the host card
        val host = eventDetail.host
        val hostUsername = host.username

        // Show the City State of the event, not the host
        val hostCityState = eventDetail.citystate

        event_detail_host_item!!.setOnClickListener {
            val intent = Intent(event_detail_host_item!!.context, UserDetailActivity::class.java)
            intent.putExtra("userId", eventDetail.host.userId)
            startActivity(intent)
        }

        item_event_detail_username!!.text = hostUsername
        val profileImageReference = storageReference!!.child("images/users/" + host.userId)
        profileImageReference.downloadUrl.addOnSuccessListener { uri ->
            Picasso.with(applicationContext)
                    .load(uri)
                    .resize(200, 200)
                    .centerCrop()
                    .transform(CircleTransform())
                    .into(item_event_detail_userthumb)
        }.addOnFailureListener {
            // Handle any errors
            item_event_detail_userthumb!!.setImageDrawable(applicationContext.getDrawable(R.drawable.ic_profile_black_24dp))
        }


        // Set the date
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            val date = inputFormat.parse(eventDetail.date)
            val outputFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US)
            val formattedDate = outputFormat.format(date)
            event_detail_date!!.text = formattedDate
        } catch (e: ParseException) {
            e.printStackTrace()
        }


        // Set the city state
        item_event_detail_citystate!!.text = hostCityState

        // Set the image
        val eventImageReference = storageReference!!.child("images/events/" + eventId!!)
        eventImageReference.downloadUrl.addOnSuccessListener { uri ->
            Picasso.with(applicationContext)
                    .load(uri)
                    .resize(500, 500)
                    .centerCrop()
                    .into(event_image_detail)
        }.addOnFailureListener { Log.e(this.javaClass.name, "Unable to load Event Image") }

        // Make it visible
        event_detail_linearlayout!!.visibility = View.VISIBLE
    }

    override fun showInvitedUser(user: AbstractMap.SimpleEntry<String, MiniUser>) {
        invitedUsers!!.add(user)
        adapter!!.notifyDataSetChanged()
    }

    private fun showProgressbar(show: Boolean) {
        if (show) {
            event_detail_progressBar!!.visibility = View.VISIBLE
        } else {
            event_detail_progressBar!!.visibility = View.GONE
        }
    }

    override fun presentError(message: String) {
        showProgressbar(false)
        event_detail_toolbar!!.title = "Event Detail Error"
        event_detail_linearlayout!!.visibility = View.GONE
        empty_state_button!!.visibility = View.GONE
        empty_state_image!!.setImageDrawable(getDrawable(R.drawable.ic_error_empty_state))
        empty_state_text_header!!.text = getString(R.string.error_header)
        empty_state_text_subheader!!.text = message
        empty_state_text_header!!.visibility = View.VISIBLE
        empty_state_text_subheader!!.visibility = View.VISIBLE
        event_detail_empty_state!!.visibility = View.VISIBLE
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Adapter
     */
    private inner class UsersListAdapter constructor(private val mContext: Context, miniUsers: MutableList<Map.Entry<String, MiniUser>>, private val mStorageReference: StorageReference) : BaseAdapter() {

        private val mInflater: LayoutInflater


        init {
            invitedUsers = miniUsers
            mInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        override fun getCount(): Int {
            return invitedUsers!!.size
        }

        override fun getItem(position: Int): Any {
            return invitedUsers!![position].value
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val rowView = mInflater.inflate(R.layout.item_event_detail_user, parent, false)
            val userThumb = rowView.findViewById<ImageView>(R.id.item_event_detail_userthumb)
            val userName = rowView.findViewById<TextView>(R.id.item_event_detail_username)
            val cityState = rowView.findViewById<TextView>(R.id.item_event_detail_citystate)
            val userUrl = rowView.findViewById<TextView>(R.id.item_event_detail_url)
            val attendingStatusTextView = rowView.findViewById<TextView>(R.id.item_event_detail_attending_textview)

            val miniUser = getItem(position) as MiniUser

            userName.text = miniUser.username
            cityState.text = miniUser.citystate
            attendingStatusTextView.text = miniUser.attendingStatus
            userUrl.text = miniUser.url
            userUrl.isClickable = true
            Linkify.addLinks(userUrl, Linkify.WEB_URLS)


            val profileImageReference = mStorageReference.child("images/users/" + miniUser.userId)
            profileImageReference.downloadUrl.addOnSuccessListener { uri ->
                Picasso.with(mContext)
                        .load(uri)
                        .resize(148, 148)
                        .centerCrop()
                        .transform(CircleTransform())
                        .into(userThumb)
            }.addOnFailureListener {
                // Handle any errors
                userThumb.setImageDrawable(mContext.getDrawable(R.drawable.ic_profile_black_24dp))
            }

            rowView.setOnClickListener {
                val intent = Intent(mContext, UserDetailActivity::class.java)
                intent.putExtra("userId", miniUser.userId)
                mContext.startActivity(intent)
            }

            return rowView
        }
    }
}
