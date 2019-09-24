package com.bookyrself.bookyrself.views

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.ServerModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.presenters.EventInvitesFragmentPresenter
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.AbstractMap
import java.util.ArrayList
import java.util.Arrays
import java.util.Date
import java.util.Locale

import butterknife.BindView
import butterknife.ButterKnife


class EventInvitesFragment : Fragment(), BaseFragment, EventInvitesFragmentPresenter.EventInvitesViewListener {

    @BindView(R.id.event_invites_progress_bar)
    internal var progressBar: ProgressBar? = null
    @BindView(R.id.event_invites_recycler_view)
    internal var recyclerView: RecyclerView? = null
    @BindView(R.id.toolbar_event_invites_fragment)
    internal var toolbar: Toolbar? = null
    @BindView(R.id.event_invites_empty_state)
    internal var emptyState: View? = null
    @BindView(R.id.empty_state_text_header)
    internal var emptyStateTextHeader: TextView? = null
    @BindView(R.id.empty_state_image)
    internal var emptyStateImage: ImageView? = null
    @BindView(R.id.empty_state_text_subheader)
    internal var emptyStateTextSubHeader: TextView? = null
    @BindView(R.id.empty_state_button)
    internal var emptyStateButton: Button? = null


    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: EventsAdapter? = null
    private var events: MutableList<Entry<String, EventDetail>>? = null
    private var presenter: EventInvitesFragmentPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = EventInvitesFragmentPresenter(this, context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_event_invites, container, false)
        ButterKnife.bind(this, view)
        events = ArrayList<Entry<String, EventDetail>>()
        adapter = EventsAdapter()
        layoutManager = LinearLayoutManager(activity)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = adapter
        toolbar!!.setTitle(R.string.title_event_invites)
        hideEmptyState()
        showLoadingState(true)

        return view
    }

    override fun onResume() {
        super.onResume()
        presenter!!.subscribe()
    }

    override fun onPause() {
        super.onPause()
        presenter!!.unsubscribe()
    }

    override fun eventPendingInvitationResponseReturned(eventId: String, event: EventDetail) {
        showLoadingState(false)
        events!!.add(AbstractMap.SimpleEntry(eventId, event))
        adapter!!.notifyDataSetChanged()
        showContent(true)
    }

    override fun presentError(message: String) {
        showLoadingState(false)
        showEmptyState(getString(R.string.error_header), message, "", activity!!.getDrawable(R.drawable.ic_error_empty_state))
    }


    override fun removeEventFromList(eventId: String, eventDetail: EventDetail) {

        val entry = AbstractMap.SimpleEntry(eventId, eventDetail)
        events!!.remove(entry)
        adapter!!.notifyDataSetChanged()

        if (events!!.isEmpty()) {
            showEmptyStateForNoInvites()
        }

    }

    override fun showEmptyStateForNoInvites() {
        showEmptyState(getString(R.string.empty_state_event_invites_no_invites_header),
                getString(R.string.empty_state_event_invites_no_invites_subheader),
                "", activity!!.getDrawable(R.drawable.ic_no_events_black_24dp))
    }


    override fun showContent(show: Boolean) {
        if (show) {
            recyclerView!!.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.GONE
        }

    }

    override fun showLoadingState(show: Boolean) {
        if (show) {
            progressBar!!.visibility = View.VISIBLE
        } else {
            progressBar!!.visibility = View.GONE
        }
    }

    override fun showEmptyState(header: String, subHeader: String, buttonText: String, image: Drawable?) {

        showContent(false)
        showLoadingState(false)

        emptyState!!.visibility = View.VISIBLE
        emptyStateTextHeader!!.visibility = View.VISIBLE
        emptyStateTextSubHeader!!.visibility = View.VISIBLE
        emptyStateImage!!.visibility = View.VISIBLE
        emptyStateTextHeader!!.text = header
        emptyStateTextSubHeader!!.text = subHeader
        emptyStateImage!!.setImageDrawable(image)
        if (buttonText != "") {
            emptyStateButton!!.visibility = View.VISIBLE
            emptyStateButton!!.text = buttonText
            emptyStateButton!!.setOnClickListener { view ->
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
            emptyStateButton!!.visibility = View.GONE
        }
    }

    override fun hideEmptyState() {
        emptyStateButton!!.visibility = View.GONE
        emptyState!!.visibility = View.GONE
        emptyStateImage!!.visibility = View.GONE
        emptyStateTextHeader!!.visibility = View.GONE
        emptyStateTextSubHeader!!.visibility = View.GONE
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
    internal inner class EventsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            val view = layoutInflater.inflate(R.layout.item_event_invite, parent, false)
            return ViewHolderEvents(view)

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            val viewHolderEvents = holder as ViewHolderEvents
            val eventDetail = events!![position].value
            viewHolderEvents.eventNameTextView!!.setText(eventDetail.getEventname())
            viewHolderEvents.eventLocationTextView!!.setText(eventDetail.getCitystate())
            val inputformat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            try {
                val date = inputformat.parse(eventDetail.getDate())
                val outputFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US)
                val formattedDate = outputFormat.format(date)
                viewHolderEvents.eventDateTextView!!.text = formattedDate
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            viewHolderEvents.acceptButton!!.setOnClickListener { view ->
                presenter!!.acceptEventInvite(FirebaseAuth.getInstance().uid!!,
                        events!![position].key,
                        events!![position].value)
            }

            viewHolderEvents.denyButton!!.setOnClickListener { view ->
                presenter!!.rejectEventInvite(FirebaseAuth.getInstance().uid!!,
                        events!![position].key,
                        events!![position].value)
            }
        }

        override fun getItemCount(): Int {
            return events!!.size
        }

        internal inner class ViewHolderEvents(itemView: View) : RecyclerView.ViewHolder(itemView) {
            @BindView(R.id.event_item_invite_card)
            var eventCard: CardView? = null
            @BindView(R.id.event_item_invite_line1)
            var eventNameTextView: TextView? = null
            @BindView(R.id.event_item_invite_line3)
            var eventDateTextView: TextView? = null
            @BindView(R.id.event_item_invite_line2)
            var eventLocationTextView: TextView? = null
            @BindView(R.id.event_item_invite_image)
            var eventImageThumbnail: ImageView? = null
            @BindView(R.id.event_item_invite_accept_button)
            var acceptButton: Button? = null
            @BindView(R.id.event_item_invite_deny_button)
            var denyButton: Button? = null

            init {
                ButterKnife.bind(this, itemView)
            }
        }
    }

    companion object {

        private val RC_SIGN_IN = 123
    }
}// Required empty public constructor
