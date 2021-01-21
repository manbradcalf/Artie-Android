package com.bookyrself.bookyrself.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.utils.CircleTransform
import com.bookyrself.bookyrself.viewmodels.EventInvitesFragmentViewModel
import com.bookyrself.bookyrself.views.activities.EventDetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_event_invites.*
import kotlinx.android.synthetic.main.item_event_invite.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class EventInvitesFragment : BaseFragment() {
    private var storageReference = FirebaseStorage.getInstance().reference
    private var eventInvitesHashMap = hashMapOf<EventDetail, String>()
    private var events = mutableListOf<EventDetail>()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var adapter: EventInvitesAdapter
    lateinit var model: EventInvitesFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProviders.of(this,
                EventInvitesFragmentViewModel.EventInvitesFragmentViewModelFactory(activity!!.application))
                .get(EventInvitesFragmentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_invites, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (FirebaseAuth.getInstance().uid != null) {
            setLayout()
            setListeners()
            model.load()
        } else {
            showSignedOutEmptyState(getString(R.string.empty_state_event_invites_signed_out_subheader),
                    activity!!.getDrawable(R.drawable.ic_invitation))
        }
    }

    private fun setListeners() {
        model.eventsWithPendingInvites.observe(this) {
            if (!it.isNullOrEmpty()) {
                showEventInvites(it)
            } else {
                showEmptyStateForNoInvites()
            }
        }
    }

        private fun setLayout() {
            adapter = EventInvitesAdapter()
            layoutManager = LinearLayoutManager(activity)
            toolbar_event_invites_fragment?.setTitle(R.string.title_event_invites)
            event_invites_recycler_view?.adapter = adapter
            event_invites_recycler_view?.layoutManager = layoutManager
            hideEmptyState()
            showLoadingState(true)
        }

        private fun showEventInvites(invitesReturned: HashMap<EventDetail, String>) {
            showLoadingState(false)
            hideEmptyState()
            showContent(true)

            events = invitesReturned.keys.asSequence().toMutableList()
            eventInvitesHashMap = invitesReturned
            adapter.notifyDataSetChanged()
        }


        private fun showEmptyStateForNoInvites() {
            showEmptyState(getString(R.string.empty_state_event_invites_no_invites_header),
                    getString(R.string.empty_state_event_invites_no_invites_subheader),
                    activity!!.getDrawable(R.drawable.ic_no_events_black_24dp))
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

        /**
         * Adapter
         */
        inner class EventInvitesAdapter : RecyclerView.Adapter<EventInvitesAdapter.ViewHolderEvents>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderEvents =
                    ViewHolderEvents(LayoutInflater.from(parent.context).inflate(R.layout.item_event_invite, parent, false))

            override fun onBindViewHolder(holder: ViewHolderEvents, position: Int) = holder.bind(events[position])

            override fun getItemCount() = events.size

            inner class ViewHolderEvents(itemView: View) : RecyclerView.ViewHolder(itemView) {
                fun bind(eventDetail: EventDetail) = with(itemView) {
                    val eventId = eventInvitesHashMap[eventDetail]

                    val eventNameTextView = this.event_item_invite_line1
                    val eventLocationTextView = this.event_item_invite_line2
                    val eventDateTextView = this.event_item_invite_line3
                    val eventImageThumbnail = this.event_item_invite_image
                    val acceptButton = this.event_item_invite_accept_button
                    val denyButton = this.event_item_invite_deny_button

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

                    storageReference.child("/images/events/$eventId").downloadUrl
                            .addOnSuccessListener { uri ->
                                Picasso.with(activity)
                                        .load(uri)
                                        .placeholder(R.drawable.round)
                                        .error(R.drawable.round)
                                        .transform(CircleTransform())
                                        .resize(100, 100)
                                        .into(eventImageThumbnail)
                            }.addOnFailureListener {
                                Log.e("EventInvitesFragment", "Event image not downloaded")
                                eventImageThumbnail.setImageDrawable(context!!.getDrawable(R.drawable.ic_event_black_48dp))
                            }

                    acceptButton.setOnClickListener {
                        model.respondToInvite(true, eventId!!, eventDetail)
                    }

                    denyButton.setOnClickListener {
                        model.respondToInvite(false, eventId!!, eventDetail)
                    }

                    itemView.setOnClickListener {
                        val intent = Intent(activity, EventDetailActivity::class.java)
                        intent.putExtra("eventId", eventId!!)
                        startActivity(intent)
                    }
                }
            }
        }
    }