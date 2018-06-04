package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SearchResponseUsers._source;
import com.bookyrself.bookyrself.presenters.ProfilePresenter;
import com.bookyrself.bookyrself.utils.CircleTransform;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements ProfilePresenter.ProfilePresenterListener {

    private static final int RC_SIGN_IN = 123;
    private static final int RC_PROFILE_CREATION = 456;
    private static final int RC_PHOTO_SELECT = 789;
    private Button btnSignOut;
    private Button btnEditProfile;
    private TextView userNameTextView;
    private TextView bioTextView;
    private ProfilePresenter presenter;
    private ImageView profileImage;
    private StorageReference storageReference;
    private _source user;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        user = new _source();
        presenter = new ProfilePresenter(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        userNameTextView = getActivity().findViewById(R.id.username_profile_fragment);
        btnSignOut = getActivity().findViewById(R.id.btnSignOut);
        btnEditProfile = getActivity().findViewById(R.id.btnEditProfile);
        bioTextView = getActivity().findViewById(R.id.bio_body_profile_activity);
        profileImage = getActivity().findViewById(R.id.profile_image);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.EmailBuilder().build());
            // Authenticate
            btnSignOut.setVisibility(View.GONE);
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false, true)
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        } else {
            // Get user data
            presenter.getUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
    }

    @Override
    public void checkAuth() {

    }

    @Override
    public void profileInfoReady(_source response) {
        setLayout(response);
    }

    @Override
    public void presentToast(String message) {

    }

    @Override
    public void loadingState() {

    }

    @Override
    public void successfulAuth() {

    }

    public void setLayout(_source user) {

        if (user != null) {
            userNameTextView.setText(user.getUsername());
            bioTextView.setText(user.getBio());
            StorageReference profileImageReference = storageReference.child("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(getActivity().getApplicationContext())
                            .load(uri)
                            .resize(148, 148)
                            .centerCrop()
                            .transform(new CircleTransform())
                            .into(profileImage);
                }
            });
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Choose Picture"), RC_PHOTO_SELECT);
                }
            });
            //TODO: Set image, city state, tags, etc
        } else {
            userNameTextView.setText("user not in fb db");
        }
        btnSignOut.setVisibility(View.VISIBLE);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
            }
        });
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProfileCreationActivity.class);
                startActivityForResult(intent, RC_PROFILE_CREATION);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IdpResponse response = IdpResponse.fromResultIntent(data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_SIGN_IN:
                    if (isNewSignUp()) {
                        // Successfully signed up
                        // Creating user object to push to FB DB
                        user.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        user.setUsername(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                        presenter.createUser(user, FirebaseAuth.getInstance().getCurrentUser().getUid());
                        showToast("Signing Up!");
                    } else {
                        // Successfully signed in
                        presenter.getUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        showToast("Signing In!");
                    }
                    return;
                case RC_PROFILE_CREATION:
                    //TODO: This creation logic probably shouldn't be in the fragment?
                    //TODO: Should I pass the Intent to the presenter?
                    //TODO: *Vomit emoji* do i really need to null check everything?
                    if (data.getStringExtra("bio") != null) {
                        user.setBio(data.getStringExtra("bio"));
                    }
                    if (data.getStringExtra("username") != null) {
                        user.setUsername(data.getStringExtra("username"));
                    }
                    if (data.getStringExtra("location") != null) {
                        user.setCitystate(data.getStringExtra("location"));
                    }
                    if (user != null) {
                        presenter.patchUser(user, FirebaseAuth.getInstance().getCurrentUser().getUid());
                        showToast("Updating Profile!");
                    }
                    return;
                case RC_PHOTO_SELECT:
                    Uri selectedimg = data.getData();

                    // Set the image to the profileImageThumb
                    Picasso.with(getActivity().getApplicationContext())
                            .load(selectedimg)
                            .resize(148, 148)
                            .centerCrop()
                            .transform(new CircleTransform())
                            .into(profileImage);

                    // Upload to firebase
                    StorageReference profilePhotoRef = storageReference.child("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
                    UploadTask uploadTask = profilePhotoRef.putFile(selectedimg);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            presentToast("upload failed");
                            Picasso.with(getActivity().getApplicationContext()).load(R.drawable.ic_user).into(profileImage);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            presentToast("upload succeeded");
                        }
                    });
            }
        } else {
            if (response == null) {
                // User pressed back button
                showToast("Canceled");
                return;
            }
            if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showToast("No Connection");
                return;
            }
            if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showToast("Unknown Error");
            }

        }
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public boolean isNewSignUp() {
        FirebaseUserMetadata metadata = FirebaseAuth.getInstance().getCurrentUser().getMetadata();
        return metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp();
    }
}
