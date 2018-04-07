package com.bookyrself.bookyrself.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;

import com.bookyrself.bookyrself.R;

public class ProfileFragment extends Fragment {

    private static final int RC_SIGN_IN = 123;
    private Button btnSignOut;
    private ScrollView scrollView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    void setLayout() {
        btnSignOut = getActivity().findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        scrollView = getActivity().findViewById(R.id.user_detail_scrollview);
    }

    //TODO: find out how to give fragments access to auth and db objects
//    void checkAuth() {
//        if (auth.getCurrentUser() != null) {
//            //Signed in
//        } else {
//            //TODO: SmartLock disabled only for testing. Remove this before committing.
//            startActivityForResult(
//                    AuthUI.getInstance()
//                            .createSignInIntentBuilder()
//                            .setIsSmartLockEnabled(false, true)
//                            .build(),
//                    RC_SIGN_IN
//            );
//
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
//        if (requestCode == RC_SIGN_IN) {
//            IdpResponse response = IdpResponse.fromResultIntent(data);
//
//            // Successfully signed in
//            if (resultCode == RESULT_OK) {
//                showSnackbar("You did it!");
//                return;
//            } else {
//                // Sign in failed
//                if (response == null) {
//                    // User pressed back button
//                    showSnackbar("Canceled");
//                    return;
//                }
//
//                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
//                    showSnackbar("No Connection");
//                    return;
//                }
//
//                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
//                    showSnackbar("Unknown Error");
//                    return;
//                }
//            }
//
//            showSnackbar("Idk");
//        }
//    }
//
//    private void showSnackbar(String message) {
//        Snackbar snackbar = Snackbar
//                .make(scrollView, message, Snackbar.LENGTH_SHORT);
//
//        snackbar.show();
//    }

}
