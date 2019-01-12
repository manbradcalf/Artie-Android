package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.presenters.ProfilePresenter;
import com.bookyrself.bookyrself.utils.CircleTransform;
import com.bookyrself.bookyrself.utils.EventDecorator;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements OnDateSelectedListener, ProfilePresenter.ProfilePresenterListener {

    private static final int RC_SIGN_IN = 123;
    private static final int RC_PROFILE_CREATION = 456;
    private static final int RC_PHOTO_SELECT = 789;
    @BindView(R.id.profile_content)
    RelativeLayout profileContent;
    @BindView(R.id.bio_body_profile_activity)
    TextView bioTextView;
    @BindView(R.id.profile_image)
    ImageView profileImage;
    @BindView(R.id.city_state_profile_activity)
    TextView cityStateTextView;
    @BindView(R.id.tags_profile_activity)
    TextView tagsTextView;
    @BindView(R.id.user_url_profile_activity)
    TextView urlTextView;
    @BindView(R.id.profile_content_edit_info)
    Button editInfoButton;
    @BindView(R.id.profile_content_edit_bio)
    Button editBioButton;
    @BindView(R.id.username_profile_fragment)
    TextView userNameTextView;
    @BindView(R.id.profile_empty_state)
    View emptyState;
    @BindView(R.id.empty_state_text_header)
    TextView emptyStateTextHeader;
    @BindView(R.id.empty_state_image)
    ImageView emptyStateImage;
    @BindView(R.id.empty_state_text_subheader)
    TextView emptyStateTextSubHeader;
    @BindView(R.id.empty_state_button)
    Button emptyStateButton;
    @BindView(R.id.toolbar_profile)
    Toolbar toolbar;
    @BindView(R.id.profile_fragment_progressbar)
    ProgressBar progressbar;
    @BindView(R.id.profile_events_calendar)
    MaterialCalendarView calendarView;

    private ProfilePresenter presenter;
    private StorageReference storageReference;
    private List<CalendarDay> calendarDays = new ArrayList<>();
    private HashMap<CalendarDay, String> calendarDaysWithEventIds;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        calendarDaysWithEventIds = new HashMap<>();
        emptyState.setVisibility(View.GONE);
        profileContent.setVisibility(View.GONE);
        loadingState(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        user = new User();
        presenter = new ProfilePresenter(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        toolbar.setTitle(R.string.title_profile);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            emptyStateTextHeader.setText(getString(R.string.auth_val_prop_header));
            emptyStateTextSubHeader.setText(getString(R.string.auth_val_prop_subheader));
            emptyStateImage.setImageDrawable(getActivity().getDrawable(R.drawable.ic_no_auth_profile));
            emptyStateButton.setText("Join Now");
            emptyState.setVisibility(View.VISIBLE);
            profileContent.setVisibility(View.GONE);
            progressbar.setVisibility(View.GONE);
            emptyStateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: This is duplicated in EventFragment now
                    List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.EmailBuilder().build());
                // Authenticate
                startActivityForResult(
                        AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false, true)
                                    .setAvailableProviders(providers)
                                    .build(),
                RC_SIGN_IN);
            }
            });
        } else {
            // Get user data
            presenter.getUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.sign_out:
                if (getContext() != null) {
                    AuthUI.getInstance().signOut(getContext());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void profileInfoReady(User response) {
        setLayout(response);
    }

    // TODO: Find a way to not repeat myself for this, EventsFragment and UserDetailActivity
    @Override
    public void eventReady(EventDetail event, String eventId) {
        String[] s = event.getDate().split("-");
        int year = Integer.parseInt(s[0]);
        // I have to do weird logic on the month because months are 0 indexed
        // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
        int month = Integer.parseInt(s[1]) - 1;
        int day = Integer.parseInt(s[2]);
        CalendarDay calendarDay = CalendarDay.from(year, month, day);
        calendarDays.add(calendarDay);
        calendarDaysWithEventIds.put(calendarDay, eventId);
        calendarView.addDecorator(new EventDecorator(Color.BLUE, calendarDays, this.getContext()));
    }

    @Override
    public void presentToast(String error) {

    }

    @Override
    public void loadingState(Boolean show) {
        if (show) {
            progressbar.setVisibility(View.VISIBLE);
        } else {
            progressbar.setVisibility(View.GONE);
        }

    }

    public void setLayout(final User user) {

        if (user != null) {
            emptyState.setVisibility(View.GONE);
            loadingState(false);
            calendarView.setOnDateChangedListener(this);
            userNameTextView.setText(user.getUsername());

            // Set the user's URL
            urlTextView.setClickable(true);
            urlTextView.setMovementMethod(LinkMovementMethod.getInstance());
            if (user.getUrl() != null) {
                String linkedText =
                        String.format("<a href=\"%s\">%s</a> ", ("http://" + user.getUrl()), user.getUrl());
                urlTextView.setText(Html.fromHtml(linkedText));
            }


            if (user.getTags() != null) {
                tagsTextView.setText(user.getTags().toString().replaceAll("\\[|]|, $", ""));
            }
            if (user.getCitystate() != null) {
                cityStateTextView.setText(user.getCitystate());
            }

            if (user.getBio() != null) {
                bioTextView.setText(user.getBio());
            }
            profileContent.setVisibility(View.VISIBLE);
        } else {
            userNameTextView.setText("user not in fb db");
            profileContent.setVisibility(View.GONE);
        }

        // Both edit buttons start the profile creation activity
        editBioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProfile(user);
            }
        });
        editInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProfile(user);
            }
        });

        // Load profile image
        final StorageReference profileImageReference = storageReference.child("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                profileImage.setImageDrawable(getContext().getDrawable((R.drawable.ic_add_a_photo_black_24dp)));
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
    }

    private void editProfile(User user) {
        Intent intent = new Intent(getActivity(), ProfileCreationActivity.class);

        // Add intent extras to pre-populate edittexts in ProfileCreationActivity
        if (user.getUsername() != null) {
            intent.putExtra("Username", user.getUsername());
        }
        if (user.getBio() != null) {
            intent.putExtra("Bio", user.getBio());
        }
        if (user.getCitystate() != null) {
            intent.putExtra("Location", user.getCitystate());
        }
        if (user.getTags() != null) {
            intent.putExtra("Tags", String.valueOf(user.getTags()));
        }
        if (user.getUrl() != null) {
            intent.putExtra("Url", user.getUrl());
        }

        startActivityForResult(intent, RC_PROFILE_CREATION);
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
                        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                        UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                                .setDisplayName(data.getStringExtra("username"))
                                .build();
                        fbUser.updateProfile(changeRequest);
                    }
                    if (data.getStringExtra("location") != null) {
                        user.setCitystate(data.getStringExtra("location"));
                    }
                    if (data.getStringExtra("tags") != null) {
                        List<String> tagsList = Arrays.asList(data.getStringExtra("tags").split(", "));
                        user.setTags(tagsList);
                    }

                    if (data.getStringExtra("url") != null) {
                        user.setUrl(data.getStringExtra("url"));
                    }
                    if (user != null) {
                        presenter.patchUser(user, FirebaseAuth.getInstance().getCurrentUser().getUid());
                        showToast("Updated Profile!");
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

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay calendarDay, boolean selected) {
        Log.i("calendarDay = ", calendarDay.toString());
        if (calendarDays.contains(calendarDay)) {
            Intent intent = new Intent(getActivity(), EventDetailActivity.class);
            intent.putExtra("eventId", calendarDaysWithEventIds.get(calendarDay));
            startActivity(intent);
        }
    }
}
