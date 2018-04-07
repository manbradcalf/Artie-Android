package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.bookyrself.bookyrself.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

public class ProfileActivity extends MainActivity {

    private static final int RC_SIGN_IN = 123;
    private Button btnSignOut;
    private ScrollView scrollView;

    @Override
    int getContentViewId() {
        return R.layout.activity_profile;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_profile;
    }

    @Override
    void setLayout() {
        btnSignOut = findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
            }
        });
        scrollView = findViewById(R.id.user_detail_scrollview);
        checkAuth();
    }

    @Override
    void checkAuth() {
        if (auth.getCurrentUser() != null) {
            //Signed in
        } else {
            //TODO: SmartLock disabled only for testing. Remove this before committing.
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false, true)
                            .build(),
                    RC_SIGN_IN
            );

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                showSnackbar("You did it!");
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar("Canceled");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar("No Connection");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar("Unknown Error");
                    return;
                }
            }

            showSnackbar("Idk");
        }
    }

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar
                .make(scrollView, message, Snackbar.LENGTH_SHORT);

        snackbar.show();
    }

}
