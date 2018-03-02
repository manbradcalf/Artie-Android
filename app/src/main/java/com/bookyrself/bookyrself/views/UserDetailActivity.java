package com.bookyrself.bookyrself.views;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SearchResponseUsers._source;
import com.bookyrself.bookyrself.presenters.UserDetailPresenter;
import com.squareup.picasso.Picasso;

/**
 * Created by benmedcalf on 1/13/18.
 */

public class UserDetailActivity extends AppCompatActivity implements UserDetailPresenter.UserDetailPresenterListener {
    private static final String USERNAME = "USERNAME";
    private static final String CITYSTATE = "CITYSTATE";
    private static final String TAGS = "TAGS";
    private static final String URL = "URL";
    private static final String PROF_THUMB_URL = "PROF_THUMB_URL";
    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private TextView usernameTextView;
    private TextView cityStateTextView;
    private TextView tagsTextView;
    private TextView urlTextView;
    private TextView bioTextView;
    private ImageView profileImage;
    private UserDetailPresenter userDetailPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        coordinatorLayout = findViewById(R.id.coordinator_layout_user_detail_activity);
        usernameTextView = findViewById(R.id.username_user_detail_activity);
        cityStateTextView = findViewById(R.id.city_state_user_detail_activity);
        tagsTextView = findViewById(R.id.tags_user_detail_activity);
        urlTextView = findViewById(R.id.user_url_user_detail_activity);
        profileImage = findViewById(R.id.profile_image_user_detail_activity);
        bioTextView = findViewById(R.id.bio_body_user_detail_activity);
        userDetailPresenter = new UserDetailPresenter(this);
        userDetailPresenter.getUserInfo(getIntent().getStringExtra("userId"));

    }

    @Override
    public void userInfoReady(_source response) {
        StringBuilder listString = new StringBuilder();
        usernameTextView.setText(response.getUsername());
        cityStateTextView.setText(response.getCitystate());
        bioTextView.setText(response.getBio());
        Picasso.with(this)
                .load(response.getPicture())
                .into(profileImage);
        for (String s: response.getTags()) {
            listString.append(s+", ");
        }

        tagsTextView.setText(listString.toString());
    }

    @Override
    public void present_error() {
        Toast.makeText(this, "response was null because that id wasn't legit dumbass", Toast.LENGTH_LONG).show();
    }
}
