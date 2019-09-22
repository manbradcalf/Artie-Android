package com.bookyrself.bookyrself.views

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.ServerModels.User.User
import com.bookyrself.bookyrself.presenters.ContactsFragmentPresenter
import com.bookyrself.bookyrself.utils.CircleTransform
import com.firebase.ui.auth.AuthUI
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap

import butterknife.BindView
import butterknife.ButterKnife

import android.app.Activity.RESULT_OK

class ContactsFragment : Fragment(), BaseFragment, ContactsFragmentPresenter.ContactsPresenterListener {
    @BindView(R.id.contacts_recyclerview)
    internal var recyclerView: RecyclerView? = null
    @BindView(R.id.toolbar_contacts_fragment)
    internal var toolbar: Toolbar? = null
    @BindView(R.id.contacts_fragment_progressbar)
    internal var progressbar: ProgressBar? = null
    @BindView(R.id.contacts_empty_state)
    internal var emptyState: LinearLayout? = null
    @BindView(R.id.empty_state_text_header)
    internal var emptyStateTextHeader: TextView? = null
    @BindView(R.id.empty_state_image)
    internal var emptyStateImage: ImageView? = null
    @BindView(R.id.empty_state_text_subheader)
    internal var emptyStateTextSubHeader: TextView? = null
    @BindView(R.id.empty_state_button)
    internal var emptyStateButton: Button? = null

    private var adapter: ContactsAdapter? = null
    private var presenter: ContactsFragmentPresenter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var contacts: MutableList<User>? = null
    private var contactsMap: MutableMap<User, String>? = null
    private var storageReference: StorageReference? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)
        ButterKnife.bind(this, view)
        toolbar!!.setTitle(R.string.contacts_toolbar)
        contactsMap = HashMap()
        contacts = ArrayList()
        presenter = ContactsFragmentPresenter(this)
        adapter = ContactsAdapter()
        recyclerView!!.adapter = adapter
        layoutManager = LinearLayoutManager(activity)
        recyclerView!!.layoutManager = layoutManager
        storageReference = FirebaseStorage.getInstance().reference
        showLoadingState(true)

        return view
    }

    override fun showLoadingState(show: Boolean) {
        if (show && progressbar!!.visibility == View.GONE) {
            progressbar!!.visibility = View.VISIBLE
            hideEmptyState()
        } else {
            progressbar!!.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        presenter!!.subscribe()
    }

    override fun onPause() {
        super.onPause()
        contacts!!.clear()
        presenter!!.unsubscribe()
    }

    override fun showEmptyState(header: String, subHeader: String, buttonText: String, image: Drawable?) {
        showContent(false)
        showLoadingState(false)
        emptyState!!.visibility = View.VISIBLE
        emptyStateImage!!.visibility = View.VISIBLE
        emptyStateTextHeader!!.visibility = View.VISIBLE
        emptyStateTextSubHeader!!.visibility = View.VISIBLE

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

    override fun showContent(show: Boolean) {
        if (show) {
            recyclerView!!.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                RC_SIGN_IN -> {
                    hideEmptyState()
                    showLoadingState(true)
                    presenter!!.subscribe()
                }
            }
        }
    }

    override fun noContactsReturned() {
        showEmptyState(getString(R.string.contacts_empty_state_no_content_header),
                getString(R.string.contacts_empty_state_no_content_subheader),
                "",
                activity!!.getDrawable(R.drawable.ic_person_add_black_24dp))
    }

    override fun contactReturned(id: String, user: User) {

        showLoadingState(false)
        hideEmptyState()
        showContent(true)
        contacts!!.add(user)
        contactsMap!![user] = id
        adapter!!.notifyDataSetChanged()
    }

    override fun presentError(error: String) {
        showEmptyState(getString(R.string.error_header),
                error,
                "",
                activity!!.getDrawable(R.drawable.ic_error_empty_state))
    }

    override fun showSignedOutEmptyState() {
        showEmptyState(getString(R.string.contacts_empty_state_signed_out_header),
                getString(R.string.contacts_empty_state_no_content_subheader),
                getString(R.string.sign_in),
                activity!!.getDrawable(R.drawable.ic_person_add_black_24dp))
    }


    /**
     * RecyclerView Adapter
     */
    internal inner class ContactsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_user_search_result, parent, false)
            return ViewHolderContacts(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolderContacts = holder as ViewHolderContacts
            if (contacts!![position].tags != null) {
                val listString = StringBuilder()
                for (s in contacts!![position].tags!!) {
                    listString.append("$s, ")
                }
                viewHolderContacts.userTagsTextView!!.text = listString.toString().replace(", $".toRegex(), "")
            }

            val profileImageReference = storageReference!!.child("/images/users/" + contactsMap!![contacts!![position]]!!)
            profileImageReference
                    .downloadUrl
                    .addOnSuccessListener { uri ->
                        Picasso.with(activity)
                                .load(uri)
                                .placeholder(R.drawable.round)
                                .error(R.drawable.round)
                                .transform(CircleTransform())
                                .resize(100, 100)
                                .into(viewHolderContacts.userProfileImageThumb)
                    }.addOnFailureListener { e ->
                        // Handle any errors
                        Log.e("ContactsFragment: ", "image not dowloaded")
                        viewHolderContacts.userProfileImageThumb!!.setImageDrawable(context!!.getDrawable(R.drawable.ic_profile_black_24dp))
                    }

            viewHolderContacts.userNameTextView!!.text = contacts!![position].username
            viewHolderContacts.userCityStateTextView!!.text = contacts!![position].citystate
            viewHolderContacts.userCardView!!.setOnClickListener { view ->
                val intent = Intent(activity, UserDetailActivity::class.java)
                intent.putExtra("userId", contactsMap!![contacts!![position]])
                startActivity(intent)
            }
        }


        override fun getItemCount(): Int {
            return contacts!!.size
        }

        internal inner class ViewHolderContacts(itemView: View) : RecyclerView.ViewHolder(itemView) {

            @BindView(R.id.search_result_card_users)
            var userCardView: CardView? = null
            @BindView(R.id.user_location_search_result)
            var userCityStateTextView: TextView? = null
            @BindView(R.id.username_search_result)
            var userNameTextView: TextView? = null
            @BindView(R.id.user_tag_search_result)
            var userTagsTextView: TextView? = null
            @BindView(R.id.user_image_search_result)
            var userProfileImageThumb: ImageView? = null

            init {
                ButterKnife.bind(this, itemView)
            }
        }
    }

    companion object {

        private val RC_SIGN_IN = 123
    }
}
