package com.bookyrself.bookyrself.views.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.contacts.ContactsRepoRxJava
import com.bookyrself.bookyrself.data.profile.ProfileRepo
import com.bookyrself.bookyrself.utils.FragmentViewPager
import com.bookyrself.bookyrself.utils.FragmentViewPagerAdapter
import com.bookyrself.bookyrself.views.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val profileFragment = ProfileFragment()
    private val searchFragment = SearchFragment()
    private val eventsFragment = EventsFragment()
    private val contactsFragment = ContactsFragment()
    private val eventInvitesFragment = EventInvitesFragment()

    lateinit var adapter: FragmentViewPagerAdapter
    lateinit var viewPager: FragmentViewPager
    lateinit var navigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enterTransition = null
        setContentView(R.layout.activity_main)
        navigationView = findViewById(R.id.navigation)
        navigationView.setOnNavigationItemSelectedListener(this)
        buildFragmentsList()
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    public override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
        window.exitTransition = null
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_search -> viewPager.currentItem = SEARCH_FRAGMENT_INDEX
            R.id.navigation_calendar -> viewPager.currentItem = CALENDAR_FRAGMENT_INDEX
            R.id.navigation_contacts -> viewPager.currentItem = CONTACTS_FRAGMENT_INDEX
            R.id.navigation_profile -> viewPager.currentItem = PROFILE_FRAGMENT_INDEX
            R.id.navigation_event_invites_list -> viewPager.currentItem = EVENTS_INVITE_LIST
        }
        return true
    }

    private fun buildFragmentsList() {
        viewPager = findViewById(R.id.view_pager)
        adapter = FragmentViewPagerAdapter(this.supportFragmentManager)
        adapter.addFragment(searchFragment, "Search")
        adapter.addFragment(eventsFragment, "Calendar")
        adapter.addFragment(eventInvitesFragment, "Event Invites")
        adapter.addFragment(contactsFragment, "Contacts")
        adapter.addFragment(profileFragment, "Profile")
        viewPager.adapter = adapter
    }

    companion object {

        private const val SEARCH_FRAGMENT_INDEX = 0
        private const val CALENDAR_FRAGMENT_INDEX = 1
        private const val EVENTS_INVITE_LIST = 2
        private const val CONTACTS_FRAGMENT_INDEX = 3
        private const val PROFILE_FRAGMENT_INDEX = 4

        private var CONTACTS_REPO: ContactsRepoRxJava? = null
        private var PROFILE_REPO: ProfileRepo? = null

        //TODO: Fix all these !! and find a better way to serve up these repos
        val contactsRepo: ContactsRepoRxJava
            get() {
                if (CONTACTS_REPO == null) {
                    CONTACTS_REPO = ContactsRepoRxJava()
                }
                return CONTACTS_REPO!!
            }

        val profileRepo: ProfileRepo
            get() {
                if (PROFILE_REPO == null) {
                    PROFILE_REPO = ProfileRepo()
                }
                return PROFILE_REPO!!
            }
    }
}
