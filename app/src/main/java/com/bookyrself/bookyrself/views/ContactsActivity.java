package com.bookyrself.bookyrself.views;

import com.bookyrself.bookyrself.R;
import com.firebase.ui.auth.AuthUI;

public class ContactsActivity extends MainActivity {

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

}
