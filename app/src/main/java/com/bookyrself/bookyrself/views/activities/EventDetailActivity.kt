package com.bookyrself.bookyrself.views.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.serverModels.EventDetail.MiniUser
import com.bookyrself.bookyrself.utils.CircleTransform
import com.bookyrself.bookyrself.viewmodels.EventDetailViewModel
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_event_detail.*
import kotlinx.android.synthetic.main.empty_state_template.*
import kotlinx.android.synthetic.main.item_event_detail_user.*
import kotlinx.android.synthetic.main.item_event_detail_user.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by benmedcalf on 11/22/17.
 */

class EventDetailActivity : BaseActivity() {

    lateinit var model: EventDetailViewModel
    lateinit var invitedUsers: MutableList<Pair<String, MiniUser>>
    private lateinit var adapter: UsersListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        // Set up the adapter
        invitedUsers = ArrayList()
        adapter = UsersListAdapter(this, imageStorage)
        event_detail_users_list.adapter = adapter
        val recyclerView = event_detail_users_list
        val layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager

        // Set up initial loading state
        event_detail_linearlayout!!.visibility = View.GONE
        event_detail_empty_state.visibility = View.GONE
        setSupportActionBar(event_detail_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        showProgressbar(true)
        setListeners(intent.getStringExtra("eventId"))
    }

    private fun setListeners(eventId: String) {
        model = ViewModelProviders.of(this,
                EventDetailViewModel.EventDetailViewModelFactory(application, eventId))
                .get(EventDetailViewModel::class.java)

        model.event.observe(this) { eventDetail ->
            showEventData(eventDetail!!, eventId)
        }

        model.invitees.observe(this) { invitees ->
            showInvitedUsers(invitees)
        }
        model.load()
    }

    private fun showEventData(eventDetailData: EventDetail, eventId: String) {
        showProgressbar(false)
        event_detail_collapsing_toolbar!!.title = eventDetailData.eventname
        event_detail_collapsing_toolbar!!.setExpandedTitleColor(resources.getColor(R.color.cardview_light_background))
        event_detail_collapsing_toolbar!!.setCollapsedTitleTextColor(resources.getColor(R.color.cardview_light_background))

        // Set up the host card
        val host = eventDetailData.host
        val hostUsername = host.username

        // Show the City State of the event, not the host
        val hostCityState = eventDetailData.citystate

        event_detail_host_item!!.setOnClickListener {
            val intent = Intent(event_detail_host_item!!.context, UserDetailActivity::class.java)
            intent.putExtra("userId", eventDetailData.host.userId)
            startActivity(intent)
        }

        item_event_detail_username!!.text = hostUsername
        val profileImageReference = imageStorage.child("images/users/" + host.userId)
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
            val date = inputFormat.parse(eventDetailData.date)
            val outputFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US)
            val formattedDate = outputFormat.format(date)
            event_detail_date!!.text = formattedDate
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        // Set the city state
        item_event_detail_citystate!!.text = hostCityState

        // Set the image
        val eventImageReference = imageStorage.child("images/events/$eventId")
        eventImageReference.downloadUrl.addOnSuccessListener { uri ->
            Picasso.with(applicationContext)
                    .load(uri)
                    .resize(500, 500)
                    .centerCrop()
                    .into(event_image_detail)
        }.addOnFailureListener { Log.e(this.javaClass.name, "Unable to load Event Image") }

        event_image_detail.setOnClickListener {
            val intent = Intent(this, ViewImageActivity::class.java)
            intent.putExtra("id", eventId)
            intent.putExtra("imageType", "events")
            startActivity(intent)
        }

        if (FirebaseAuth.getInstance().uid != host?.userId) {
            event_detail_edit_fab.hide()
        } else {
            event_detail_edit_fab.setOnClickListener {
                val intent = Intent(this, EventCreationActivity::class.java)
                intent.putExtra("eventname", eventDetailData.eventname)
                intent.putExtra("citystate", eventDetailData.citystate)
                intent.putExtra("date", eventDetailData.date)
                var inviteeChipsList = ArrayList<User>()
                for (invitedUser in invitedUsers) {
                    inviteeChipsList.add(invitedUser.second)
                }
                intent.putParcelableArrayListExtra("contacts", inviteeChipsList)
                startActivity(intent)
            }
        }

        // Make it visible
        event_detail_linearlayout!!.visibility = View.VISIBLE
    }

    private fun showInvitedUsers(users: MutableList<Pair<String, MiniUser>>) {
        invitedUsers = users
        adapter.notifyDataSetChanged()
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
    inner class UsersListAdapter constructor(private val mContext: Context, private val mStorageReference: StorageReference) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_event_detail_user, parent, false)
            return InviteeViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            val viewHolder = holder as InviteeViewHolder

            val miniUser = invitedUsers[position].second


            viewHolder.userName.text = miniUser.username
            viewHolder.cityState.text = miniUser.citystate
            viewHolder.attendingStatusTextView.text = miniUser.attendingStatus
            viewHolder.userUrl.text = miniUser.url
            viewHolder.userUrl.isClickable = true
            Linkify.addLinks(viewHolder.userUrl, Linkify.WEB_URLS)


            val profileImageReference = mStorageReference.child("images/users/" + miniUser.userId)
            profileImageReference.downloadUrl.addOnSuccessListener { uri ->
                Picasso.with(mContext)
                        .load(uri)
                        .resize(148, 148)
                        .centerCrop()
                        .transform(CircleTransform())
                        .into(viewHolder.userThumb)
            }.addOnFailureListener {
                // Handle any errors
                viewHolder.userThumb.setImageDrawable(mContext.getDrawable(R.drawable.ic_profile_black_24dp))
            }

            viewHolder.rowView.setOnClickListener {
                val intent = Intent(mContext, UserDetailActivity::class.java)
                intent.putExtra("userId", miniUser.userId)
                mContext.startActivity(intent)
            }
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemCount(): Int {
            return invitedUsers.size
        }


        inner class InviteeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val rowView: CardView = itemView.search_result_card_users
            val userThumb: ImageView = rowView.item_event_detail_userthumb
            val userName: TextView = rowView.item_event_detail_username
            val cityState: TextView = rowView.item_event_detail_citystate
            val userUrl: TextView = rowView.item_event_detail_url
            val attendingStatusTextView: TextView = rowView.item_event_detail_attending_textview
        }
    }
}
