package com.bookyrself.bookyrself.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.data.Profile.ProfileRepo;
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.Host;
import com.bookyrself.bookyrself.data.ResponseModels.User.EventInviteInfo;
import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ProfileEditActivity extends AppCompatActivity {

    @BindView(R.id.profile_edit_toolbar)
    android.support.v7.widget.Toolbar toolbar;
    @BindView(R.id.profile_edit_fab)
    FloatingActionButton fab;
    @BindView(R.id.profile_edit_bio)
    EditText bioEditText;
    @BindView(R.id.profile_edit_username)
    EditText usernameEditText;
    @BindView(R.id.profile_edit_location)
    EditText locationEditText;
    @BindView(R.id.profile_edit_tags)
    EditText tagsEditText;
    @BindView(R.id.profile_edit_url)
    EditText urlEditText;

    private ProfileRepo profileRepo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        ButterKnife.bind(this);
        this.profileRepo = MainActivity.getProfileRepo();
        String username = getIntent().getStringExtra("Username");
        String location = getIntent().getStringExtra("Location");
        String bio = getIntent().getStringExtra("Bio");
        String tags = getIntent().getStringExtra("Tags");
        String url = getIntent().getStringExtra("Url");

        // Set existing data
        if (bio != null) {
            bioEditText.setText(bio);
        }
        if (location != null) {
            locationEditText.setText(location);
        }
        if (username != null) {
            usernameEditText.setText(username);
        }
        if (url != null) {
            urlEditText.setText(url);
        }

        if (tags != null) {
            tagsEditText.setText(tags.replaceAll("\\[|]|, $", ""));
        }
        fab.setOnClickListener(view -> {
            User user = new User();
            Intent returnIntent = new Intent();
            if (!TextUtils.isEmpty(usernameEditText.getText().toString())) {

                user.setUsername(usernameEditText.getText().toString());

            }
            if (!TextUtils.isEmpty(bioEditText.getText().toString())) {
                user.setBio(bioEditText.getText().toString());
            }
            if (!TextUtils.isEmpty(locationEditText.getText().toString())) {
                user.setCitystate(locationEditText.getText().toString());
            }
            if (!TextUtils.isEmpty(tagsEditText.getText().toString())) {
                String tagsString = tagsEditText.getText().toString();
                List<String> tagsList = Arrays.asList(tagsString.split("\\s*,\\s*"));
                user.setTags(tagsList);
            }
            if (!TextUtils.isEmpty(urlEditText.getText())) {
                user.setUrl(urlEditText.getText().toString());
            }

            //TODO: Should this be in a presenter? works fine for its simple purpose here
            profileRepo.updateProfileInfo(FirebaseAuth.getInstance().getUid(), user)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

                    // Get my event Invites
                    .flatMap(user1 -> FirebaseService.getAPI()
                            .getUsersEventInvites(FirebaseAuth.getInstance().getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()))

                    // Only get events I am hosting
                    .map(stringEventInviteInfoHashMap -> {

                        HashMap<String, EventInviteInfo> events = new HashMap<>();

                        for (Map.Entry<String, EventInviteInfo> entry : stringEventInviteInfoHashMap.entrySet()) {
                            if (entry.getValue().getIsHost()) {
                                events.put(entry.getKey(), entry.getValue());
                            }
                        }
                        return events;
                    })

                    // Emit an eventId for each event I'm hosting
                    .flatMapIterable(HashMap::entrySet)
                    .map(Map.Entry::getKey)

                    // Update the events I'm hosting with the new data
                    .map(eventIdOfHostedEvent -> {

                        Host host = new Host();
                        host.setUserId(FirebaseAuth.getInstance().getUid());
                        host.setUsername(user.getUsername());
                        host.setUrl(user.getUrl());
                        host.setCitystate(user.getCitystate());

                        return FirebaseService.getAPI()
                                .updateEventHost(host, eventIdOfHostedEvent)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe();
                    })

                    .subscribe(host -> {

                                // Update the firebase user
                                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(usernameEditText.getText().toString())
                                        .build();
                                FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdate);

                                // Finish the activity with a success
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            },
                            throwable -> {
                                Toast.makeText(this, "Unable to update profile!", Toast.LENGTH_SHORT).show();
                                        Log.e("ProfileEditActivity: ",throwable.getMessage(), throwable);
                            });
        });
    }

}
