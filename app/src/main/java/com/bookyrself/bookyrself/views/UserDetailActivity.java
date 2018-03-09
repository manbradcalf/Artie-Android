package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SearchResponseUsers._source;
import com.bookyrself.bookyrself.presenters.UserDetailPresenter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by benmedcalf on 1/13/18.
 */

public class UserDetailActivity extends AppCompatActivity implements UserDetailPresenter.UserDetailPresenterListener {

    private CardView emailUserCardview;
    private TextView usernameTextView;
    private TextView cityStateTextView;
    private TextView tagsTextView;
    private TextView urlTextView;
    private TextView bioTextView;
    private ImageView profileImage;
    private UserDetailPresenter userDetailPresenter;
    private ProgressBar profileImageProgressbar;
    private ProgressBar contentProgressBar;
    private TextView emailUserTextView;
    private String userEmailAddress;
    private Toolbar Toolbar;
    //TODO: figure out a consistent strategy for empty states when i dont have a headache
//    private RelativeLayout emptyState;
//    private TextView emptyStateTextView;
//    private ImageView emptyStateImageView;
    private RelativeLayout contentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        userDetailPresenter = new UserDetailPresenter(this);
        userDetailPresenter.getUserInfo(getIntent().getStringExtra("userId"));
//        emptyState = findViewById(R.id.empty_state_user_detail);
//        emptyStateTextView = findViewById(R.id.empty_state_text);
//        emptyStateTextView.setText("error loading user details");
//        emptyStateImageView = findViewById(R.id.empty_state_image);
//        emptyStateImageView.setImageDrawable(getDrawable(R.drawable.ic_binoculars));
//        emptyState.setVisibility(View.GONE);
        Toolbar = findViewById(R.id.toolbar_user_detail);
        Toolbar.setTitle("User Details");
//        contentProgressBar = findViewById(R.id.content_loading_progressbar);
        loading_state();
    }

    @Override
    public void userInfoReady(_source response) {

//        contentProgressBar.setVisibility(View.GONE);
        // Show the user details now that they're loaded
        usernameTextView = findViewById(R.id.username_user_detail_activity);
        cityStateTextView = findViewById(R.id.city_state_user_detail_activity);
        tagsTextView = findViewById(R.id.tags_user_detail_activity);
        urlTextView = findViewById(R.id.user_url_user_detail_activity);
        profileImageProgressbar = findViewById(R.id.profile_image_progressbar);
        profileImage = findViewById(R.id.profile_image_user_detail_activity);
        bioTextView = findViewById(R.id.bio_body_user_detail_activity);
        emailUserTextView = findViewById(R.id.message_user_detail_activity_text);
        emailUserCardview = findViewById(R.id.message_user_detail_activity_card);
        emailUserCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email_user();
            }
        });

        setSupportActionBar(Toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String toolbarText = getString(R.string.user_detail_toolbar, response.getUsername());
        getSupportActionBar().setTitle(toolbarText);

        StringBuilder listString = new StringBuilder();
        usernameTextView.setText(response.getUsername());
        cityStateTextView.setText(response.getCitystate());
        bioTextView.setText(response.getBio());
        emailUserTextView.setText("Email " + response.getUsername() + "!");
        userEmailAddress = response.getEmail();
        Picasso.with(this)
                .load(response.getPicture())
                .into(profileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap imageBitmap = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();
                        RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                        imageDrawable.setCircular(true);
                        imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                        profileImageProgressbar.setVisibility(View.GONE);
                        profileImage.setImageDrawable(imageDrawable);
                    }

                    @Override
                    public void onError() {
                        Log.e(this.getClass().toString(), "didn't load image");
                    }
                });
        for (String s : response.getTags()) {
            listString.append(s + ", ");
        }

        tagsTextView.setText(listString.toString());
        contentView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void present_error() {
        //TODO: This should be a legit empty state
//        emptyState.setVisibility(View.VISIBLE);
    }

    @Override
    public void loading_state() {
        contentView = findViewById(R.id.user_detail_content);
        contentView.setVisibility(View.GONE);
    }

    //TODO: I am using this method in both UserDetailActivity and EventDetailActivity presenters. I should consolidate
    @Override
    public void email_user() {

        if (userEmailAddress != null) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmailAddress});
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                present_error();
            }
        }
    }
}
