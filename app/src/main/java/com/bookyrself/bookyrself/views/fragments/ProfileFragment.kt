package com.bookyrself.bookyrself.views.fragments

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.serverModels.User.User
import com.bookyrself.bookyrself.viewmodels.ProfileFragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.empty_state_template.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*

class ProfileFragment : BaseFragment() {

    lateinit var model: ProfileFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProviders.of(
            this,
            ProfileFragmentViewModel.ProfileFragmentViewModelFactory(activity!!.application)
        ).get(ProfileFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (FirebaseAuth.getInstance().uid != null) {
            showContent(false)
            showLoadingState(true)
            setHasOptionsMenu(true)
            setListeners()
            model.load()
        } else {
            showSignedOutEmptyState()
        }
    }

    private fun setListeners() {
        model.events.observe(this) { events ->
            eventsReturned(events)
        }

        model.user.observe(this) { user ->
            userReturned(user)
        }
    }

    private fun eventsReturned(events: HashMap<EventDetail, String>?) {
        events?.let {
            it.forEach { event ->
                // TODO set drawable on Calendar for event.value.date
                // TODO implement OnDateSelectedListener for the date
            }
        }
    }

    private fun userReturned(user: User) {
        resetView()
        showLoadingState(false)
        profile_empty_state.visibility = View.GONE

        user.username?.let { username_profile_fragment.text = it }
        user.citystate?.let { city_state_profile_activity.text = it }
        user.bio?.let { bio_body_profile_activity.text = it }

        user.tags?.let {
            val tagsString = it.toString().replace("\\[|]|, $".toRegex(), "")
            tags_profile_activity.text = tagsString
        }

        user.url?.let {
            user_url_profile_activity.isClickable = true
            user_url_profile_activity.movementMethod = LinkMovementMethod.getInstance()
            user_url_profile_activity.text = Html.fromHtml(
                String.format(
                    "<a href=\"%s\">%s</a> ",
                    "http://$it", it
                )
            )
        }

        user.bio?.let { bio_body_profile_activity.text = it }

        showContent(true)
    }

    override fun showContent(show: Boolean) {
        if (show) {
            profile_content.visibility = View.VISIBLE
        } else {
            profile_content.visibility = View.GONE
        }
    }

    fun showSignedOutEmptyState() {
        showEmptyState(
            getString(R.string.auth_val_prop_header),
            getString(R.string.auth_val_prop_subheader),
            activity!!.getDrawable(R.drawable.ic_no_auth_profile),
            getString(R.string.sign_in)
        )
        toolbar_profile.title = getString(R.string.profile_toolbar_placeholder)
        setMenuVisibility(false)
    }

    override fun showLoadingState(show: Boolean) {
        if (show) {
            profile_fragment_progressbar.visibility = View.VISIBLE
        } else {
            profile_fragment_progressbar.visibility = View.GONE
        }
    }

    private fun resetView() {
        tags_profile_activity.text = getString(R.string.profile_tags_placeholder)
        bio_body_profile_activity.text = getString(R.string.profile_bio_placeholder)
        city_state_profile_activity.text = getString(R.string.profile_citystate_placeholder)
        user_url_profile_activity.text = getString(R.string.profile_url_placeholder)
    }
}