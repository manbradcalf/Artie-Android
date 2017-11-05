package com.bookyrself.bookyrself;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MessagesActivity extends MainActivity {

    private static final int RC_SIGN_IN = 123;

    @Override
    int getContentViewId() {
        return R.layout.activity_messages;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_messages;
    }

    @Override
    void setLayout() {
        if (auth.getCurrentUser() != null) {
            //Signed in
        } else {
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder().build(),
                    RC_SIGN_IN
            );

        }
    }

}
