package com.bookyrself.bookyrself.views;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.data.Contacts.ContactsRepository;
import com.bookyrself.bookyrself.data.Events.EventsRepository;
import com.bookyrself.bookyrself.data.Profile.ProfileRepo;
import com.bookyrself.bookyrself.utils.FragmentViewPager;
import com.bookyrself.bookyrself.utils.FragmentViewPagerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final int SEARCH_FRAGMENT_INDEX = 0;
    private static final int CALENDAR_FRAGMENT_INDEX = 1;
    private static final int CONTACTS_FRAGMENT_INDEX = 2;
    private static final int PROFILE_FRAGMENT_INDEX = 3;
    private static final int EVENTS_INVITE_LIST = 4;
    private static EventsRepository EVENT_INVITES_REPO = null;
    private static ContactsRepository CONTACTS_REPO = null;
    private static ProfileRepo PROFILE_REPO = null;
    final ProfileFragment profileFragment = new ProfileFragment();
    final SearchFragment searchFragment = new SearchFragment();
    final EventsFragment eventsFragment = new EventsFragment();
    final ContactsFragment contactsFragment = new ContactsFragment();
    final EventInvitesFragment eventInvitesFragment = new EventInvitesFragment();

    public FirebaseDatabase db;
    public FirebaseApp firebaseApp;
    FragmentViewPagerAdapter adapter;
    FragmentViewPager viewPager;
    private BottomNavigationView navigationView;

    public static ContactsRepository getContactsRepo() {
        if (CONTACTS_REPO == null) {
            CONTACTS_REPO = new ContactsRepository();
        }
            return CONTACTS_REPO;
    }

    public static EventsRepository getEventsRepo(Context context) {
        if (EVENT_INVITES_REPO == null) {
            EVENT_INVITES_REPO = new EventsRepository(context);
        }
        return EVENT_INVITES_REPO;
    }

    public static ProfileRepo getProfileRepo() {
        if (PROFILE_REPO == null) {
            PROFILE_REPO = new ProfileRepo();
        }

        return PROFILE_REPO;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setEnterTransition(null);
        firebaseApp = FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        db = FirebaseDatabase.getInstance("https://bookyrself-staging.firebaseio.com/");
        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(this);
        buildFragmentsList();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
        getWindow().setExitTransition(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.navigation_search) {
            viewPager.setCurrentItem(SEARCH_FRAGMENT_INDEX);
        } else if (itemId == R.id.navigation_calendar) {
            viewPager.setCurrentItem(CALENDAR_FRAGMENT_INDEX);
        } else if (itemId == R.id.navigation_contacts) {
            viewPager.setCurrentItem(CONTACTS_FRAGMENT_INDEX);
        } else if (itemId == R.id.navigation_profile) {
            viewPager.setCurrentItem(PROFILE_FRAGMENT_INDEX);
        } else if (itemId == R.id.navigation_event_invites_list) {
            viewPager.setCurrentItem(EVENTS_INVITE_LIST);
        }
        return true;
    }

    private void buildFragmentsList() {

        viewPager = findViewById(R.id.view_pager);
        adapter = new FragmentViewPagerAdapter(this.getSupportFragmentManager());
        adapter.addFragment(searchFragment, "Search");
        adapter.addFragment(eventsFragment, "Calendar");
        adapter.addFragment(contactsFragment, "Contacts");
        adapter.addFragment(profileFragment, "Profile");
        adapter.addFragment(eventInvitesFragment, "Event Invites");
        viewPager.setAdapter(adapter);
    }
}
