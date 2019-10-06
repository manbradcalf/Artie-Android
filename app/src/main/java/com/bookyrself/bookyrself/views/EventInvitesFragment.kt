package com.bookyrself.bookyrself.views

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.utils.CircleTransform
import com.bookyrself.bookyrself.viewmodels.EventInvitesFragmentViewModel
import com.firebase.ui.auth.AuthUI
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.empty_state_template.*
import kotlinx.android.synthetic.main.fragment_event_invites.*
import kotlinx.android.synthetic.main.item_event_invite.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class EventInvitesFragment : Fragment(), BaseFragment {
    private var eventInvitesHashMap = hashMapOf<EventDetail, String>()
    private var events = mutableListOf<EventDetail>()
    private var storageReference = FirebaseStorage.getInstance().reference


    lateinit var model: EventInvitesFragmentViewModel
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var adapter: EventInvitesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_invites, container, false)
    }

    override fun onResume() {
        init()
        super.onResume()
    }

    private fun init() {
        toolbar_event_invites_fragment?.setTitle(R.string.title_event_invites)
        hideEmptyState()
        showLoadingState(true)

        // create and observe the view model
        model = ViewModelProviders.of(this,
                EventInvitesFragmentViewModel.EventInvitesFragmentViewModelFactory())
                .get(EventInvitesFragmentViewModel::class.java)

        model.eventsWithPendingInvites.observe(this) {
            adapter = EventInvitesAdapter()
            event_invites_recycler_view?.adapter = adapter
            layoutManager = LinearLayoutManager(activity)
            event_invites_recycler_view?.layoutManager = layoutManager
            showLoadingState(false)
            hideEmptyState()
            showContent(true)

            //TODO: Double check this data conversion here
            events = it.keys.asSequence().toMutableList()
            eventInvitesHashMap = it
            adapter.notifyDataSetChanged()
        }
    }

    override fun presentError(message: String) {
        showLoadingState(false)
        showEmptyState(getString(R.string.error_header), message, "", activity!!.getDrawable(R.drawable.ic_error_empty_state))
    }


    fun removeEventFromList(eventId: String, eventDetail: EventDetail) {

        val entry = AbstractMap.SimpleEntry(eventId, eventDetail)
        eventInvitesHashMap.remove(eventDetail)

        adapter.notifyDataSetChanged()

        if (events.isEmpty()) {
            showEmptyStateForNoInvites()
        }
    }

    fun showEmptyStateForNoInvites() {
        showEmptyState(getString(R.string.empty_state_event_invites_no_invites_header),
                getString(R.string.empty_state_event_invites_no_invites_subheader),
                "", activity!!.getDrawable(R.drawable.ic_no_events_black_24dp))
    }


    override fun showContent(show: Boolean) {
        if (show) {
            event_invites_recycler_view.visibility = View.VISIBLE
        } else {
            event_invites_recycler_view.visibility = View.GONE
        }

    }

    override fun showLoadingState(show: Boolean) {
        if (show) {
            event_invites_progress_bar.visibility = View.VISIBLE
        } else {
            event_invites_progress_bar.visibility = View.GONE
        }
    }

    override fun showEmptyState(header: String, subHeader: String, buttonText: String, image: Drawable?) {

        showContent(false)
        showLoadingState(false)
        event_invites_empty_state?.visibility = View.VISIBLE
        empty_state_text_header?.visibility = View.VISIBLE
        empty_state_text_subheader?.visibility = View.VISIBLE
        empty_state_image?.visibility = View.VISIBLE
        empty_state_text_header?.text = header
        empty_state_text_subheader?.text = subHeader
        empty_state_image?.setImageDrawable(image)
        if (buttonText != "") {
            empty_state_button?.visibility = View.VISIBLE
            empty_state_button?.text = buttonText
            empty_state_button?.setOnClickListener { view ->
                val providers = Arrays.asList(AuthUI.IdpConfig.GoogleBuilder().build(),
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
        } else {
            empty_state_button?.visibility = View.GONE
        }
    }

    override fun hideEmptyState() {
        empty_state_button?.visibility = View.GONE
        event_invites_empty_state?.visibility = View.GONE
        empty_state_image?.visibility = View.GONE
        empty_state_text_header?.visibility = View.GONE
        empty_state_text_subheader?.visibility = View.GONE
    }

    override fun showSignedOutEmptyState() {
        showEmptyState(getString(R.string.event_invites_signed_out_header),
                getString(R.string.empty_state_event_invites_signed_out_subheader),
                getString(R.string.sign_in),
                activity!!.getDrawable(R.drawable.ic_invitation))
    }

    /**
     * Adapter
     */
    inner class EventInvitesAdapter : RecyclerView.Adapter<EventInvitesAdapter.ViewHolderEvents>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderEvents =
                ViewHolderEvents(LayoutInflater.from(parent.context).inflate(R.layout.item_event_invite, parent, false))

        override fun onBindViewHolder(holder: ViewHolderEvents, position: Int) = holder.bind(events[position], position)

        override fun getItemCount(): Int {
            return events.size
        }

        inner class ViewHolderEvents(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bind(item: EventDetail, position: Int) = with(itemView) {
                val eventCard = this.event_item_invite_card
                val eventNameTextView = this.event_item_invite_line1
                val eventLocationTextView = this.event_item_invite_line2
                val eventDateTextView = this.event_item_invite_line3
                val eventImageThumbnail = this.event_item_invite_image
                val acceptButton = this.event_item_invite_accept_button
                val denyButton = this.event_item_invite_deny_button

                val eventDetail = events[position]
                eventNameTextView?.text = eventDetail.eventname
                eventLocationTextView?.text = eventDetail.citystate
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

                try {
                    val date = inputFormat.parse(eventDetail.date)
                    val outputFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US)
                    val formattedDate = outputFormat.format(date)
                    eventDateTextView?.text = formattedDate
                } catch (e: ParseException) {
                    e.printStackTrace()
                }

                val eventImageReference = storageReference.child(
                        "/images/events/" + eventInvitesHashMap[eventDetail])
                eventImageReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.with(activity)
                            .load(uri)
                            .placeholder(R.drawable.round)
                            .error(R.drawable.round)
                            .transform(CircleTransform())
                            .resize(100, 100)
                            .into(eventImageThumbnail)

                }.addOnFailureListener {
                    Log.e("EventInvitesFragment", "Event image not downloaded")
                    eventImageThumbnail.setImageDrawable(context!!.getDrawable(R.drawable.ic_calendar))
                }

                itemView.setOnClickListener {
                    val intent = Intent(activity, EventDetailActivity::class.java)
                    intent.putExtra("eventId", eventInvitesHashMap[events[position]])
                    startActivity(intent)
                }
            }
        }
    }

    companion object {

        private val RC_SIGN_IN = 123
    }
}// Required empty public constructor
