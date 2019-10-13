package com.bookyrself.bookyrself.views.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bookyrself.bookyrself.R;
import com.ramotion.paperonboarding.PaperOnboardingFragment;
import com.ramotion.paperonboarding.PaperOnboardingPage;

import java.util.ArrayList;

public class OnboardingActivity extends AppCompatActivity {

    FrameLayout fragmentContainer;
    FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();


        fragmentContainer = findViewById(R.id.onboarding_fragment);
        fm = getSupportFragmentManager();
        PaperOnboardingPage scr1 = new PaperOnboardingPage("Find Artists",
                "Search artists by tags, location and availability",
                Color.parseColor("#FFFFFF"), R.drawable.ic_search_onboarding, R.drawable.ic_search_24dp_tryagain);
        PaperOnboardingPage scr2 = new PaperOnboardingPage("Find Events",
                "Search for dates available for booking your shows.",
                Color.parseColor("#FFFFFF"), R.drawable.ic_calendar, R.drawable.ic_calendar);
        PaperOnboardingPage scr3 = new PaperOnboardingPage("Tours",
                "Use the venues, artists and friends you find to book your tour!",
                Color.parseColor("#FFFFFF"), R.drawable.ic_map_location, R.drawable.ic_map_location);

        ArrayList<PaperOnboardingPage> elements = new ArrayList<>();
        elements.add(scr1);
        elements.add(scr2);
        elements.add(scr3);
        PaperOnboardingFragment onBoardingFragment = PaperOnboardingFragment.newInstance(elements);
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.onboarding_fragment, onBoardingFragment);
        fragmentTransaction.commit();
        onBoardingFragment.setOnRightOutListener(() -> {
            editor.putBoolean(getString(R.string.has_seen_onboarding_key), true);
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });
    }


}
