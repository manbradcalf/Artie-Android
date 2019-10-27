package com.bookyrself.bookyrself.views.fragments

import android.app.Activity.RESULT_OK
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
import com.bookyrself.bookyrself.data.serverModels.User.User
import com.bookyrself.bookyrself.utils.CircleTransform
import com.bookyrself.bookyrself.viewmodels.ContactsFragmentViewModel
import com.bookyrself.bookyrself.views.activities.UserDetailActivity
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.item_user_search_result.view.*

class ContactsFragment : BaseFragment() {
    var storageReference = FirebaseStorage.getInstance().reference
    var contactsMap = hashMapOf<User, String>()
    var contacts = listOf<User>()

    private lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var adapter: ContactsAdapter
    lateinit var model: ContactsFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }


    fun init() {
        // Set up initial view
        toolbar_contacts_fragment.setTitle(R.string.contacts_toolbar)
        showLoadingState(true)

        // create and observe the view model
        model = ViewModelProviders.of(this,
                ContactsFragmentViewModel.ContactsFragmentViewModelFactory(activity!!.application))
                .get(ContactsFragmentViewModel::class.java)

        model.contactsHashMap.observe(this) {
            // Now we have data so lets fire up the adapter
            if (!it.isNullOrEmpty()) {
                showUserContacts(it)
            } else {
                noUserContactsReturned()
            }
        }

        model.isSignedIn.observe(this) { userIsSignedIn ->
            if (!userIsSignedIn) {
                showLoadingState(false)
                showSignedOutEmptyState(
                        getString(R.string.contacts_empty_state_no_content_subheader),
                        activity!!.getDrawable(R.drawable.ic_person_add_black_24dp)!!)
            } else if (!contactsMap.isNullOrEmpty()) {
                //TODO: Will this be victim of a race condition if contactsMap hasn't been set yet
                showUserContacts(contactsMap)
            } else {
                noUserContactsReturned()
            }
        }
    }

    override fun onResume() {
        init()
        super.onResume()
    }

    //TODO: Rename this method and / or combine with showContent
    private fun showUserContacts(contactsReturned: HashMap<User, String>?) {
        adapter = ContactsAdapter()
        contacts_recyclerview.adapter = adapter
        layoutManager = LinearLayoutManager(activity)
        contacts_recyclerview.layoutManager = layoutManager
        showLoadingState(false)
        hideEmptyState()
        showContent(true)

        //TODO: Double check this data conversion here
        contacts = contactsReturned?.keys?.asSequence()!!.toList()
        contactsMap = contactsReturned
        adapter.notifyDataSetChanged()
    }

    private fun noUserContactsReturned() {
        showEmptyState(getString(R.string.contacts_empty_state_no_content_header),
                getString(R.string.contacts_empty_state_no_content_subheader),
                activity!!.getDrawable(R.drawable.ic_person_add_black_24dp))
    }

    override fun showLoadingState(show: Boolean) {
        if (show && contacts_fragment_progressbar.visibility == View.GONE) {
            contacts_fragment_progressbar.visibility = View.VISIBLE
            hideEmptyState()
        } else {
            contacts_fragment_progressbar.visibility = View.GONE
        }
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
                    model.load()
                }
            }
        }
    }

    /**
     * RecyclerView Adapter
     */
    inner class ContactsAdapter : RecyclerView.Adapter<ContactsAdapter.ViewHolderContacts>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderContacts =
                ViewHolderContacts(LayoutInflater.from(parent.context).inflate(R.layout.item_user_search_result, parent, false))

        override fun onBindViewHolder(holder: ViewHolderContacts, position: Int) = holder.bind(contacts[position])

        override fun getItemCount() = contacts.size

        inner class ViewHolderContacts(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(contact: User) = with(itemView) {
                val contactId = contactsMap[contact]

                val contactUserNameTextView = this.username_search_result
                val contactImageThumbnail = this.user_image_search_result
                val contactTagsTextView = this.user_tag_search_result
                val contactCityStateTextView = this.user_citystate_search_result

                contactUserNameTextView.text = contact.username
                contactCityStateTextView.text = contact.citystate

                if (contact.tags != null) {
                    val listString = StringBuilder()
                    for (s in contact.tags!!) {
                        listString.append("$s, ")
                    }
                    contactTagsTextView.text = listString.toString().replace(", $".toRegex(), "")
                }

                storageReference.child("/images/users/$contactId").downloadUrl
                        .addOnSuccessListener { uri ->
                            Picasso.with(activity)
                                    .load(uri)
                                    .placeholder(R.drawable.round)
                                    .error(R.drawable.round)
                                    .transform(CircleTransform())
                                    .resize(100, 100)
                                    .into(contactImageThumbnail)
                        }.addOnFailureListener {
                            // Handle any errors
                            Log.e("ContactsFragment", "contact image not downloaded")
                            contactImageThumbnail.setImageDrawable(context!!.getDrawable(R.drawable.ic_profile_black_24dp))
                        }

                itemView.setOnClickListener {
                    val intent = Intent(activity, UserDetailActivity::class.java)
                    intent.putExtra("userId", contactId)
                    startActivity(intent)
                }
            }
        }
    }
}


