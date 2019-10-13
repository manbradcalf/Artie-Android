package com.bookyrself.bookyrself;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.bookyrself.bookyrself.views.activities.MainActivity;
import com.bookyrself.bookyrself.views.activities.OnboardingActivity;

public class StartupActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean hasSeenOnboarding = sharedPreferences.getBoolean(getString(R.string.has_seen_onboarding_key), false);
        if (hasSeenOnboarding) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, OnboardingActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
