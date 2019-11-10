package com.bookyrself.bookyrself.views.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.data.serverModels.User.User;
import com.bookyrself.bookyrself.presenters.ProfileFragmentPresenter;
import com.bookyrself.bookyrself.utils.CircleTransform;
import com.bookyrself.bookyrself.utils.EventDecorator;
import com.bookyrself.bookyrself.views.activities.EventDetailActivity;
import com.bookyrself.bookyrself.views.activities.ProfileEditActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends BaseFragment implements OnDateSelectedListener, ProfileFragmentPresenter.ProfilePresenterListener {

    private static final int RC_SIGN_IN = 123;
    private static final int RC_PROFILE_EDIT = 456;
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

    private ProfileFragmentPresenter presenter;
    private StorageReference storageReference;
    private HashMap<CalendarDay, String> calendarDaysWithEventIds;
    private User user;
    private List<CalendarDay> acceptedEventsCalendarDays = new ArrayList<>();
    private List<CalendarDay> pendingEventsCalendarDays = new ArrayList<>();
    private List<CalendarDay> unavailableDates = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new ProfileFragmentPresenter(this, getContext());
        user = new User();
        storageReference = FirebaseStorage.getInstance().getReference();
        calendarDaysWithEventIds = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.title_profile);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getUid() != null) {
            showContent(false);
            hideEmptyState();
            showLoadingState(true);
            setHasOptionsMenu(true);
            presenter.subscribe();
        } else {
            showSignedOutEmptyState();
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
                    showContent(false);
                    showSignedOutEmptyState();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void profileInfoReady(String userId, User user) {
        setLayout(user);
        showContent(true);
    }

    @Override
    public void eventReady(String eventId, EventDetail event) {
        if (event.getUsers() != null) {
            if (!event.getUsers().isEmpty()) {

                String[] s = event.getDate().split("-");
                int year = Integer.parseInt(s[0]);
                // I have to do weird logic on the month because months are 0 indexed
                // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
                int month = Integer.parseInt(s[1]) - 1;
                int day = Integer.parseInt(s[2]);
                CalendarDay calendarDay = CalendarDay.from(year, month, day);

                for (Map.Entry<String, Boolean> isUserAttending : event.getUsers().entrySet()) {
                    // If the user is attending or is the host
                    if (isUserAttending.getValue() || event.getHost().getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                        acceptedEventsCalendarDays.add(calendarDay);
                        calendarDaysWithEventIds.put(calendarDay, eventId);
                        calendarView.addDecorator(new EventDecorator(EventDecorator.INVITE_ACCEPTED, acceptedEventsCalendarDays, this.getContext()));
                    } else {
                        pendingEventsCalendarDays.add(calendarDay);
                        calendarDaysWithEventIds.put(calendarDay, eventId);
                        calendarView.addDecorator(new EventDecorator(EventDecorator.INVITE_PENDING, pendingEventsCalendarDays, this.getContext()));
                    }
                }
            }
        }
    }

    private void setLayout(final User user) {
        if (user != null) {
            emptyState.setVisibility(View.GONE);
            showLoadingState(false);
            calendarView.setOnDateChangedListener(this);
            userNameTextView.setText(user.getUsername());

            // Set your URL
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

            // Both edit buttons start the profile creation activity
            editBioButton.setOnClickListener(view -> editProfile(user));
            editInfoButton.setOnClickListener(view -> editProfile(user));

            profileContent.setVisibility(View.VISIBLE);

            if (user.getUnavailableDates() != null) {

                for (String date : user.getUnavailableDates().keySet()) {

                    String[] s = date.split("-");
                    int year = Integer.parseInt(s[0]);
                    // I have to do weird logic on the month because months are 0 indexed
                    // I can't use JodaTime because MaterialCalendarView only accepts Java Calendar
                    int month = Integer.parseInt(s[1]) - 1;
                    int day = Integer.parseInt(s[2]);

                    CalendarDay calendarDay = CalendarDay.from(year, month, day);
                    unavailableDates.add(calendarDay);

                    calendarView.addDecorator(new EventDecorator(EventDecorator.DATE_UNAVAILABLE, unavailableDates, this.getContext()));
                }
            }


        } else {
            userNameTextView.setText(R.string.user_not_in_db);
            profileContent.setVisibility(View.GONE);
        }

        // Load profile image
        final StorageReference profileImageReference = storageReference.child("images/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        profileImageReference.getDownloadUrl().addOnSuccessListener(uri -> Picasso.with(getActivity().getApplicationContext())
                .load(uri)
                .resize(148, 148)
                .centerCrop()
                .transform(new CircleTransform())
                .into(profileImage)).addOnFailureListener(exception -> {
            // Handle any errors
            profileImage.setImageDrawable(getContext().getDrawable((R.drawable.ic_add_a_photo_black_24dp)));
        });

        profileImage.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Choose Picture"), RC_PHOTO_SELECT);
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IdpResponse response = IdpResponse.fromResultIntent(data);
        if (resultCode == RESULT_OK) {
            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser != null) {
                switch (requestCode) {
                    case RC_SIGN_IN:
                        if (presenter.isNewSignUp()) {
                            // Successfully signed up
                            // Clear out activity's old user data if it exists
                            if (user.getTags() != null) {
                                user.setTags(null);
                            }
                            if (user.getBio() != null) {
                                user.setBio(null);
                            }
                            if (user.getCitystate() != null) {
                                user.setCitystate(null);
                            }
                            if (user.getUrl() != null) {
                                user.setUrl(null);
                            }

                            // Update user object to push to FB DB
                            user.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                            user.setUsername(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                            presenter.updateUser(user, FirebaseAuth.getInstance().getCurrentUser().getUid());
                            showCreatingUserLoadingToast();
                        } else {
                            // Successfully signed in
                            showLoadingState(true);
                            showToast("Signing In!");
                            presenter.subscribe();
                        }
                        setMenuVisibility(true);
                        return;

                    case RC_PROFILE_EDIT:
                        showToast("Profile Updated!");
                        return;
                    case RC_PHOTO_SELECT:
                        Uri selectedImage = data.getData();

                        // Upload to firebase
                        StorageReference profilePhotoRef = storageReference.child("images/users/" + fbUser.getUid());

                        Bitmap bmp = null;
                        try {
                            bmp = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                        byte[] imgData = baos.toByteArray();
                        UploadTask uploadTask = profilePhotoRef.putBytes(imgData);

                        uploadTask.addOnSuccessListener(taskSnapshot -> {
                            // Set the image to the profileImageThumb
                            Picasso.with(getActivity().getApplicationContext())
                                    .load(selectedImage)
                                    .resize(148, 148)
                                    .centerCrop()
                                    .transform(new CircleTransform())
                                    .into(profileImage);

                            showToast("upload succeeded");

                        }).addOnFailureListener(e -> {
                            showToast("upload failed");
                            Picasso.with(getActivity().getApplicationContext()).load(R.drawable.ic_user).into(profileImage);
                        });
                }
            }
        } else if (response == null) {
            // User pressed back button
            showToast("Canceled");
        } else if (response.getError() != null) {
            showToast(response.getError().getMessage());
        }
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay calendarDay, boolean selected) {
        if (acceptedEventsCalendarDays.contains(calendarDay)) {
            Intent intent = new Intent(getActivity(), EventDetailActivity.class);
            intent.putExtra("eventId", calendarDaysWithEventIds.get(calendarDay));
            startActivity(intent);
        } else if (pendingEventsCalendarDays.contains(calendarDay)) {
            Intent intent = new Intent(getActivity(), EventDetailActivity.class);
            intent.putExtra("eventId", calendarDaysWithEventIds.get(calendarDay));
            startActivity(intent);
        } else {
            // Show a dialog asking if you want to mark the date as unavailable
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            dialogBuilder.setTitle("Mark Date as unavailable?");
            dialogBuilder.setCancelable(true);

            dialogBuilder.setPositiveButton("Yes", (dialogInterface, i) -> {

                // Mark date unavailable
                String pattern = "yyyy-MM-dd";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String date = simpleDateFormat.format(calendarDay.getDate());
                presenter.markDateAsUnavailable(FirebaseAuth.getInstance().getUid(), date);

            }).setNegativeButton("Cancel", null).show();
        }
    }

    public void showContent(boolean show) {
        if (show) {
            profileContent.setVisibility(View.VISIBLE);
        } else {
            profileContent.setVisibility(View.GONE);
        }
    }

    public void showLoadingState(boolean show) {
        if (show) {
            progressbar.setVisibility(View.VISIBLE);
        } else {
            progressbar.setVisibility(View.GONE);
        }
    }

    private void showEmptyState(String header, String subHeader, String buttonText, Drawable image) {
        showContent(false);
        showLoadingState(false);
        emptyState.setVisibility(View.VISIBLE);
        emptyStateImage.setVisibility(View.VISIBLE);
        emptyStateTextHeader.setVisibility(View.VISIBLE);
        emptyStateTextSubHeader.setVisibility(View.VISIBLE);

        emptyStateTextHeader.setText(header);
        emptyStateTextSubHeader.setText(subHeader);
        emptyStateImage.setImageDrawable(image);
        if (!buttonText.equals("")) {
            emptyStateButton.setVisibility(View.VISIBLE);
            emptyStateButton.setText(buttonText);
            emptyStateButton.setOnClickListener(view -> {
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
            });
        } else {
            emptyStateButton.setVisibility(View.GONE);
        }
    }

    public void showSignedOutEmptyState() {
        showEmptyState(getString(R.string.auth_val_prop_header),
                getString(R.string.auth_val_prop_subheader),
                getString(R.string.sign_in),
                getActivity().getDrawable(R.drawable.ic_no_auth_profile));
        setMenuVisibility(false);
    }

    private void editProfile(User user) {
        Intent intent = new Intent(getActivity(), ProfileEditActivity.class);

        // Add intent extras to pre-populate edittexts in ProfileEditActivity
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

        startActivityForResult(intent, RC_PROFILE_EDIT);
    }

    // I toast a lot in this fragment so I added this for brevity and readability
    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showCreatingUserLoadingToast() {
        showToast("Signing Up!");
    }
}
