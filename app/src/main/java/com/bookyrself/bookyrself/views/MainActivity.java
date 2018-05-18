package com.bookyrself.bookyrself.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.utils.FragmentViewPager;
import com.bookyrself.bookyrself.utils.FragmentViewPagerAdapter;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final int RC_SIGN_IN = 123;
    private static final int SEARCH_FRAGMENT_INDEX = 0;
    private static final int CALENDAR_FRAGMENT_INDEX = 1;
    private static final int CONTACTS_FRAGMENT_INDEX = 2;
    private static final int PROFILE_FRAGMENT_INDEX = 3;


    private BottomNavigationView navigationView;
    public FirebaseDatabase db;
    public FirebaseAuth auth;
    public FirebaseApp firebaseApp;
    FragmentViewPagerAdapter adapter;
    FragmentViewPager viewPager;


    //TODO: I'm creating a firebase app, db and auth every time I start an activity? This feels wrong
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setEnterTransition(null);
        firebaseApp = FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        db = FirebaseDatabase.getInstance("https://bookyrself-staging.firebaseio.com/");
        auth = FirebaseAuth.getInstance();
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
        } else if (itemId == R.id.navigation_messages) {
            viewPager.setCurrentItem(CONTACTS_FRAGMENT_INDEX);
        } else if (itemId == R.id.navigation_profile) {
            viewPager.setCurrentItem(PROFILE_FRAGMENT_INDEX);
        }
        return true;
    }

    private void buildFragmentsList() {
        final ProfileFragment profileFragment = new ProfileFragment();
        SearchFragment searchFragment = new SearchFragment();
        CalendarFragment calendarFragment = new CalendarFragment();
        ContactsFragment contactsFragment = new ContactsFragment();

        viewPager = findViewById(R.id.view_pager);
        adapter = new FragmentViewPagerAdapter(this.getSupportFragmentManager());
        adapter.addFragment(searchFragment, "Search");
        adapter.addFragment(calendarFragment, "Calendar");
        adapter.addFragment(contactsFragment, "Contacts");
        adapter.addFragment(profileFragment, "Profile");
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            //TODO: Refactor this? Activity and fragment shouldn't know eachother's state
            @Override
            public void onPageSelected(int position) {
                if (position == PROFILE_FRAGMENT_INDEX) {
                    profileFragment.checkAuth();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

}
