package com.bookyrself.bookyrself.views;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentContainer;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.utils.FragmentViewPager;
import com.bookyrself.bookyrself.utils.FragmentViewPagerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.ramotion.paperonboarding.PaperOnboardingFragment;
import com.ramotion.paperonboarding.PaperOnboardingPage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final int RC_SIGN_IN = 123;
    private BottomNavigationView navigationView;
    public FirebaseDatabase db;
    public FirebaseAuth auth;
    public FirebaseApp firebaseApp;
    private FragmentManager fm;
    private List<android.support.v4.app.Fragment> fragments = new ArrayList<>();
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
                    viewPager.setCurrentItem(0);
                } else if (itemId == R.id.navigation_calendar) {
                    viewPager.setCurrentItem(1);
                } else if (itemId == R.id.navigation_messages) {
                    viewPager.setCurrentItem(2);
                } else if (itemId == R.id.navigation_profile) {
                    viewPager.setCurrentItem(3);
                }
                return true;
            }

    private void buildFragmentsList() {

        viewPager = findViewById(R.id.view_pager);
        adapter = new FragmentViewPagerAdapter(this.getSupportFragmentManager());
        adapter.addFragment(new SearchFragment(), "Search");
        adapter.addFragment(new CalendarFragment(), "Calendar");
        adapter.addFragment(new ContactsFragment(), "Contacts");
        adapter.addFragment(new ProfileFragment(), "Profile");
        viewPager.setAdapter(adapter);
    }


}
