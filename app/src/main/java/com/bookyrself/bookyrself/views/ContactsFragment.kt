package com.bookyrself.bookyrself.views

import android.app.Activity.RESULT_OK
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
import com.bookyrself.bookyrself.data.ServerModels.User.User
import com.bookyrself.bookyrself.utils.CircleTransform
import com.bookyrself.bookyrself.viewmodels.ContactsFragmentViewModel
import com.firebase.ui.auth.AuthUI
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.empty_state_template.*
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.item_user_search_result.view.*
import java.util.*

class ContactsFragment : Fragment(), BaseFragment {
    var storageReference = FirebaseStorage.getInstance().reference
    var contactsMap = hashMapOf<User, String>()
    var contacts = listOf<User>()

    lateinit var adapter: ContactsAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var model: ContactsFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onResume() {
        initView()
        initData()
        super.onResume()
    }

    private fun initData() {
        model = ViewModelProviders.of(this,
                ContactsFragmentViewModel.ContactsFragmentViewModelFactory())
                .get(ContactsFragmentViewModel::class.java)

        model.contactsHashMap.observe(this) {

            // Now we have data so lets fire up the adapter
            adapter = ContactsAdapter()
            contacts_recyclerview.adapter = adapter
            layoutManager = LinearLayoutManager(activity)
            contacts_recyclerview.layoutManager = layoutManager
            showLoadingState(false)
            hideEmptyState()
            showContent(true)

            //TODO: Double check this data conversion here
            contacts = it.keys.asSequence().toList()
            contactsMap = it
            adapter?.notifyDataSetChanged()
        }
    }

    private fun initView() {
        toolbar_contacts_fragment.setTitle(R.string.contacts_toolbar)
        showLoadingState(true)
    }

    override fun showLoadingState(show: Boolean) {
        if (show && contacts_fragment_progressbar.visibility == View.GONE) {
            contacts_fragment_progressbar.visibility = View.VISIBLE
            hideEmptyState()
        } else {
            contacts_fragment_progressbar.visibility = View.GONE
        }
    }

    override fun showEmptyState(header: String, subHeader: String, buttonText: String, image: Drawable?) {
        showContent(false)
        showLoadingState(false)
        empty_state_view.visibility = View.VISIBLE
        empty_state_image.visibility = View.VISIBLE
        empty_state_text_header.visibility = View.VISIBLE
        empty_state_text_subheader.visibility = View.VISIBLE

        empty_state_text_header.text = header
        empty_state_text_subheader.text = subHeader
        empty_state_image.setImageDrawable(image)
        if (buttonText != "") {
            empty_state_button.visibility = View.VISIBLE
            empty_state_button.text = buttonText
            empty_state_button.setOnClickListener { view ->
                val providers = listOf(AuthUI.IdpConfig.GoogleBuilder().build(),
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
            empty_state_button.visibility = View.GONE
        }
    }

    override fun hideEmptyState() {
        empty_state_button.visibility = View.GONE
        empty_state_image.visibility = View.GONE
        empty_state_text_header.visibility = View.GONE
        empty_state_text_subheader.visibility = View.GONE
    }

    override fun showContent(show: Boolean) {
        if (show) {
            contacts_recyclerview.visibility = View.VISIBLE
        } else {
            contacts_recyclerview.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                RC_SIGN_IN -> {
                    hideEmptyState()
                    showLoadingState(true)
                }
            }
        }
    }

    fun noContactsReturned() {
        showEmptyState(getString(R.string.contacts_empty_state_no_content_header),
                getString(R.string.contacts_empty_state_no_content_subheader),
                "",
                activity!!.getDrawable(R.drawable.ic_person_add_black_24dp))
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
    inner class ContactsAdapter : RecyclerView.Adapter<ContactsAdapter.ViewHolderContacts>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderContacts =
                ViewHolderContacts(LayoutInflater.from(parent.context).inflate(R.layout.item_user_search_result, parent, false))

        override fun onBindViewHolder(holder: ViewHolderContacts, position: Int) = holder.bind(contacts[position], position)

        override fun getItemCount() = contacts.size

        inner class ViewHolderContacts(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(item: User, position: Int) = with(itemView) {
                val contactUserName = this.username_search_result
                val contactPhoto = this.user_image_search_result
                val contactTags = this.user_tag_search_result
                val contactCityState = this.user_citystate_search_result

                contactUserName.text = item.username
                contactCityState.text = item.citystate

                if (item.tags != null) {
                    val listString = StringBuilder()
                    for (s in item.tags!!) {
                        listString.append("$s, ")
                    }
                    contactTags.text = listString.toString().replace(", $".toRegex(), "")
                }

                val profileImageReference = storageReference!!.child("/images/users/" + contactsMap!![contacts!![position]]!!)
                profileImageReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.with(activity)
                            .load(uri)
                            .placeholder(R.drawable.round)
                            .error(R.drawable.round)
                            .transform(CircleTransform())
                            .resize(100, 100)
                            .into(contactPhoto)
                }.addOnFailureListener {
                    // Handle any errors
                    Log.e("ContactsFragment", "user image not downloaded")
                    contactPhoto.setImageDrawable(context!!.getDrawable(R.drawable.ic_profile_black_24dp))
                }

                itemView.setOnClickListener {
                    val intent = Intent(activity, UserDetailActivity::class.java)
                    intent.putExtra("userId", contactsMap[contacts[position]])
                    startActivity(intent)
                }
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 123
    }
}


