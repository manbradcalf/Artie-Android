package com.bookyrself.bookyrself.views.fragments

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.serverModels.SearchResponseEvents.Hit
import com.bookyrself.bookyrself.presenters.SearchPresenter
import com.bookyrself.bookyrself.utils.CircleTransform
import com.bookyrself.bookyrself.views.activities.EventDetailActivity
import com.bookyrself.bookyrself.views.activities.UserDetailActivity
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.empty_state_template.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.item_event.view.*
import java.io.IOException
import java.util.*

class SearchFragment : Fragment(), SearchPresenter.SearchPresenterListener {
    private var presenter: SearchPresenter? = null
    private var eventsResults = mutableListOf<Hit>()
    private var usersResults = mutableListOf<com.bookyrself.bookyrself.data.serverModels.SearchResponseUsers.Hit>()
    private var adapter: ResultsAdapter? = null
    private var boolSearchEditable: Boolean? = false
    private var storageReference: StorageReference? = null
    private var cityStateSearch: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setLayout()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        storageReference = FirebaseStorage.getInstance().reference
    }

    override fun onPause() {
        super.onPause()
        search_what.clearFocus()
        cityStateSearch = ""
    }

    override fun onResume() {
        super.onResume()
        search_what.clearFocus()
    }

    private fun setLayout() {
        // Initialize the AutocompleteSupportFragment.
        Places.initialize(activity!!, resources.getString(R.string.google_api_key))
        val autoCompleteFragment = this.childFragmentManager.fragments[0] as AutocompleteSupportFragment

        // Set the search icon for the autoCompleteFragment
        ((autoCompleteFragment.view as LinearLayout).getChildAt(0) as ImageView).setImageDrawable(activity!!.getDrawable(R.drawable.ic_location_search))

        // Specify the types of place data to return.
        autoCompleteFragment.setPlaceFields(listOf(Place.Field.LAT_LNG))
        autoCompleteFragment.setHint("City and State")
        autoCompleteFragment.setTypeFilter(TypeFilter.CITIES)
        val geocoder = Geocoder(activity, Locale.getDefault())


        // Set up a PlaceSelectionListener to handle the response.
        autoCompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                try {
                    val addresses = geocoder.getFromLocation(place.latLng!!.latitude, place.latLng!!.longitude, 1)
                    if (addresses != null && addresses.size > 0) {
                        val selectedCityState = addresses[0].locality + ", " + addresses[0].adminArea
                        autoCompleteFragment.setHint(selectedCityState)
                        cityStateSearch = selectedCityState
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i("ERROR SELECTING PLACE", "An error occurred: $status")
            }
        })

        empty_state_button.visibility = View.GONE
        if (presenter == null) {
            presenter = SearchPresenter(this)
        }
        if (adapter == null) {
            adapter = ResultsAdapter()
        }
        search_recycler_view.adapter = adapter
        val layoutManager = LinearLayoutManager(activity)
        search_recycler_view.layoutManager = layoutManager
        search_what.queryHint = getString(R.string.search_what_query_hint)
        from_button.visibility = View.GONE
        to_button.visibility = View.GONE
        radio_group_search.check(R.id.users_toggle)
        search_btn.visibility = View.GONE
        progress_bar.visibility = View.GONE
        if (adapter!!.itemCount == 0) {
            empty_state_text_header.text = getString(R.string.search_empty_state_header)
            empty_state_text_subheader.text = getString(R.string.search_empty_state_subheader)
            empty_state_image.setImageDrawable(activity!!.getDrawable(R.drawable.ic_minivan))
        } else {
            // Hit this else clause if the fragment is restarted with data already.
            // We need to show the edit search button and unselect the search view
            search_btn.visibility = View.VISIBLE
            search_btn.setText(R.string.search_fragment_edit_search_btn_text)
            search_what.clearFocus()
            search_what.isSelected = false
            search_what.isIconified = false
        }

        search_what.setOnSearchClickListener {
            showFullSearchBar(true)
        }

        from_button.setOnClickListener {
            val dialog = DatePickerDialogFragment()
            dialog.setFlag(FLAG_START_DATE)
            dialog.setSearchPresenter(presenter)
            dialog.show(activity!!.fragmentManager, "datePicker")
        }

        to_button.setOnClickListener {
            val dialog = DatePickerDialogFragment()
            dialog.setFlag(FLAG_END_DATE)
            dialog.setSearchPresenter(presenter)
            dialog.show(activity!!.fragmentManager, "datePicker")
        }

        // Get radio group selected item using on checked change listener
        radio_group_search.setOnCheckedChangeListener { _, checkedId ->
            setDateSelectionOptions()
        }

        search_btn.setOnClickListener {
            if (!boolSearchEditable!!) {
                if (events_toggle.isChecked) {
                    eventsResults.clear()
                    usersResults.clear()
                    presenter!!.executeSearch(
                            EVENT_SEARCH_FLAG,
                            search_what.query.toString(),
                            cityStateSearch,
                            from_button.text.toString(),
                            to_button.text.toString())
                    showFullSearchBar(false)
                } else if (users_toggle.isChecked) {
                    eventsResults.clear()
                    usersResults.clear()
                    presenter!!.executeSearch(
                            USER_SEARCH_FLAG,
                            search_what.query.toString(),
                            cityStateSearch,
                            null,
                            null)
                    showFullSearchBar(false)
                }
            } else {
                boolSearchEditable = false
                search_btn.setText(R.string.search_fragment_search_button_text)
                showFullSearchBar(true)
            }
        }
    }

    override fun searchEventsResponseReady(hits: MutableList<Hit>) {
        if (search_recycler_view.visibility == View.GONE) {
            search_recycler_view.visibility = View.VISIBLE
        }

        // If the last empty state was an error, make sure that it is now
        // a generic failed search. No service errors will hit this method,
        // as they are caught by retrofit, so this is safe.
        if (hits.isEmpty()) {
            empty_state_text_header!!.setText(R.string.search_activity_no_results_header)
            empty_state_text_subheader!!.setText(R.string.search_activity_no_results_subheader)
            empty_state_image!!.setImageDrawable(activity!!.getDrawable(R.drawable.ic_binoculars))
            empty_state_view!!.visibility = View.VISIBLE
            showFullSearchBar(false)
        } else {
            empty_state_view!!.visibility = View.GONE
        }

        eventsResults = hits.filter {
            it._source != null && it._source.citystate != null
        }.toMutableList()

        adapter!!.setViewType(EVENT_VIEW_TYPE)
        boolSearchEditable = true
        search_btn.setText(R.string.search_fragment_edit_search_btn_text)
        adapter!!.notifyDataSetChanged()
        showProgressbar(false)
    }

    override fun searchUsersResponseReady(hits: MutableList<com.bookyrself.bookyrself.data.serverModels.SearchResponseUsers.Hit>) {
        if (search_recycler_view.visibility == View.GONE) {
            search_recycler_view.visibility = View.VISIBLE
        }

        // If the last empty state was an error, make sure that it is now
        // a generic failed search. No service errors will hit this method,
        // as they are caught by retrofit, so this is safe.
        if (hits.isEmpty()) {
            empty_state_text_header!!.setText(R.string.search_activity_no_results_header)
            empty_state_text_subheader!!.setText(R.string.search_activity_no_results_subheader)
            empty_state_image!!.setImageDrawable(activity!!.getDrawable(R.drawable.ic_binoculars))
            empty_state_view!!.visibility = View.VISIBLE
            showFullSearchBar(false)
        } else {
            empty_state_view!!.visibility = View.GONE
        }

        usersResults = hits.filter {
            it._source.username != null
            //TODO: Uncomment when we have more users maybe
//                    && it._source.citystate != null
//                    && it._source.tags != null
        }.toMutableList()
        adapter!!.setViewType(USER_VIEW_TYPE)
        boolSearchEditable = true
        search_btn.text = getString(R.string.edit_search_btn_text)
        adapter!!.notifyDataSetChanged()
        showProgressbar(false)
    }

    //TODO: Find where this can be used and use it (prolly most places)
    private fun showFullSearchBar(bool: Boolean) {
        if (bool) {
            boolSearchEditable = false
            setDateSelectionOptions()
            search_btn.setText(R.string.title_search)
            search_btn.visibility = View.VISIBLE
            radio_group_search.visibility = View.VISIBLE
        } else {
            boolSearchEditable = true
            search_btn.text = getString(R.string.edit_search_btn_text)
            to_button.visibility = View.GONE
            from_button.visibility = View.GONE
            radio_group_search.visibility = View.GONE
        }
    }

    private fun setDateSelectionOptions() {
        if (users_toggle.isChecked) {
            to_button.visibility = View.GONE
            from_button.visibility = View.GONE
            search_btn.visibility = View.VISIBLE
        } else {
            search_btn.visibility = View.VISIBLE
            to_button.visibility = View.VISIBLE
            from_button.visibility = View.VISIBLE
        }
    }

    override fun startDateChanged(date: String) {
        from_button.text = date
    }

    override fun endDateChanged(date: String) {
        to_button.text = date
    }

    override fun showProgressbar(bool: Boolean?) {
        if (bool!!) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.GONE
        }
    }

    override fun itemSelected(id: String, flag: Int) {
        when (flag) {
            EVENT_VIEW_TYPE -> {
                val intent = Intent(activity, EventDetailActivity::class.java)
                intent.putExtra("eventId", id)
                startActivity(intent)
            }
            USER_VIEW_TYPE -> {
                val intent = Intent(activity, UserDetailActivity::class.java)
                intent.putExtra("userId", id)
                startActivity(intent)
            }
            else -> Log.e(SearchFragment::class.java.name, "Unknown Item type selected")
        }
    }

    override fun showError() {
        search_recycler_view.visibility = View.GONE
        progress_bar.visibility = View.GONE
        empty_state_text_header!!.setText(R.string.error_header)
        empty_state_text_subheader!!.setText(R.string.search_error_subheader)
        empty_state_image!!.setImageDrawable(activity!!.getDrawable(R.drawable.ic_error_empty_state))
        empty_state_view!!.visibility = View.VISIBLE
        showFullSearchBar(false)
    }

    /**
     * Adapter
     */
    internal inner class ResultsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var mViewType: Int = 0

        fun setViewType(viewType: Int) {
            mViewType = viewType
        }

        override fun getItemViewType(position: Int): Int {
            return mViewType
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            val view: View

            if (viewType == USER_VIEW_TYPE) {
                view = layoutInflater.inflate(R.layout.item_user_search_result, parent, false)
                return ViewHolderUsers(view)
            } else {
                view = layoutInflater.inflate(R.layout.item_event, parent, false)
                return ViewHolderEvents(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            if (usersResults.isNotEmpty() && holder.itemViewType == USER_VIEW_TYPE) {
                buildUserViewHolder(holder, position)
            } else if (eventsResults.isNotEmpty() && holder.itemViewType == EVENT_VIEW_TYPE) {
                buildEventViewHolder(holder, position)
            } else {
                Log.e(this.javaClass.toString(), "Provided neither Event or User viewholder type")
            }
        }

        override fun getItemCount(): Int {
            return when {
                eventsResults.isNotEmpty() -> eventsResults.size
                usersResults.isNotEmpty() -> usersResults.size
                else -> 0
            }
        }

        private fun buildEventViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (eventsResults.size > position) {

                val event = eventsResults[position]._source
                val viewHolderEvents = holder as ViewHolderEvents

                // Set Event Name
                if (event.eventname != null) {
                    viewHolderEvents.eventNameTextView.text = event.eventname
                }


                // Set Hostname
                if (event.host != null) {
                    if (event.host.username != null) {
                        viewHolderEvents.eventHostTextView.text = getString(R.string.event_item_hosted_by,
                                event.host.username)
                    }
                }

                // Set Event Location
                if (event.citystate != null) {
                    viewHolderEvents.eventCityStateTextView.text = getString(R.string.event_item_citystate,
                            event.citystate)
                }


                // Set Event Image thumbnail

                val eventImageReference = storageReference!!.child("images/events/" + eventsResults[position]._id)
                eventImageReference.downloadUrl.addOnSuccessListener { uri ->

                    // Add downloaded image to event item's ImageView
                    Picasso.with(context)
                            .load(uri)
                            .resize(148, 148)
                            .centerCrop()
                            .transform(CircleTransform())
                            .into(viewHolderEvents.eventImageThumb)
                }.addOnFailureListener { exception ->

                    // Set placeholder image, log error
                    viewHolderEvents.eventImageThumb.setBackgroundResource(R.drawable.ic_event_black_48dp)
                    Log.e("Event image  not loaded", exception.localizedMessage)
                }

                // Set onClickListener to fire off intent in itemSelected()
                viewHolderEvents.eventCardView.setOnClickListener {
                    itemSelected(eventsResults!![position]
                            ._id, EVENT_VIEW_TYPE)
                }
            }
        }

        private fun buildUserViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (usersResults.size > position) {
                val viewHolderUsers = holder as ViewHolderUsers
                viewHolderUsers.userCityStateTextView.text = usersResults[position]._source.citystate

                // Set username
                viewHolderUsers.userNameTextView.text = usersResults[position]._source.username

                // Set tags
                if (usersResults[position]._source.tags != null) {
                    val listString = StringBuilder()
                    for (s in usersResults[position]._source.tags) {
                        listString.append("$s, ")
                    }
                    // Regex to trim the trailing comma
                    viewHolderUsers.userTagsTextView.text = listString.toString().replace(", $".toRegex(), "")
                }


                // Set user image thumbnail
                val profileImageReference = storageReference!!.child("images/users/" + usersResults[position]._id)
                profileImageReference.downloadUrl.addOnSuccessListener { uri ->

                    // Add downloaded image to the user item's ImageView
                    Picasso.with(context)
                            .load(uri)
                            .resize(148, 148)
                            .centerCrop()
                            .transform(CircleTransform())
                            .into(viewHolderUsers.userProfileImageThumb)
                }
                        .addOnFailureListener { exception ->
                            // Handle any errors
                            viewHolderUsers.userProfileImageThumb.setImageDrawable(context!!.getDrawable(R.drawable.ic_person_white_16dp))
                            Log.e("ProfileImage not loaded", exception.localizedMessage)
                        }

                // Set onClickListener to fire off intent in itemSelected()
                viewHolderUsers.userCardView.setOnClickListener {
                    itemSelected(usersResults[position]._id, USER_VIEW_TYPE)
                }
            }
        }

        /**
         * ViewHolder for events
         */
        internal inner class ViewHolderEvents(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var eventCardView: CardView = itemView.event_item_card
            var eventCityStateTextView: TextView = itemView.event_item_line2
            var eventHostTextView: TextView = itemView.event_item_line3
            var eventNameTextView: TextView = itemView.event_item_line1
            var eventImageThumb: ImageView = itemView.event_item_image
        }

        /**
         * ViewHolder for users
         */
        internal inner class ViewHolderUsers(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var userCardView: CardView = itemView.findViewById(R.id.search_result_card_users)
            var userCityStateTextView: TextView = itemView.findViewById(R.id.user_citystate_search_result)
            var userNameTextView: TextView = itemView.findViewById(R.id.username_search_result)
            var userTagsTextView: TextView = itemView.findViewById(R.id.user_tag_search_result)
            var userProfileImageThumb: ImageView = itemView.findViewById(R.id.user_image_search_result)
        }
    }


    companion object {
        const val USER_SEARCH_FLAG = 0
        const val EVENT_SEARCH_FLAG = 1
        const val FLAG_START_DATE = 2
        const val FLAG_END_DATE = 3
        const val USER_VIEW_TYPE = 4
        const val EVENT_VIEW_TYPE = 5
    }
}
