package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SearchResponseUsers._source;
import com.bookyrself.bookyrself.presenters.UserDetailPresenter;
import com.squareup.picasso.Picasso;

/**
 * Created by benmedcalf on 1/13/18.
 */

public class UserDetailActivity extends AppCompatActivity implements UserDetailPresenter.UserDetailPresenterListener {

    private TextView usernameTextView;
    private TextView cityStateTextView;
    private TextView tagsTextView;
    private TextView urlTextView;
    private TextView bioTextView;
    private ImageView profileImage;
    private UserDetailPresenter userDetailPresenter;
    private TextView emailUserTextView;
    private String userEmailAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        usernameTextView = findViewById(R.id.username_user_detail_activity);
        cityStateTextView = findViewById(R.id.city_state_user_detail_activity);
        tagsTextView = findViewById(R.id.tags_user_detail_activity);
        urlTextView = findViewById(R.id.user_url_user_detail_activity);
        profileImage = findViewById(R.id.profile_image_user_detail_activity);
        bioTextView = findViewById(R.id.bio_body_user_detail_activity);
        userDetailPresenter = new UserDetailPresenter(this);
        userDetailPresenter.getUserInfo(getIntent().getStringExtra("userId"));
        emailUserTextView = findViewById(R.id.message_user_detail_activity_text);
        emailUserTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email_user();
            }
        });

    }

    @Override
    public void userInfoReady(_source response) {
        StringBuilder listString = new StringBuilder();
        usernameTextView.setText(response.getUsername());
        cityStateTextView.setText(response.getCitystate());
        bioTextView.setText(response.getBio());
        emailUserTextView.setText("Email " + response.getUsername() + "!");
        userEmailAddress = response.getEmail();
        Picasso.with(this)
                .load(response.getPicture())
                .into(profileImage);
        for (String s : response.getTags()) {
            listString.append(s + ", ");
        }

        tagsTextView.setText(listString.toString());
    }

    @Override
    public void present_error() {
        //TODO: This should be a legit empty state
        Toast.makeText(this, "response was null because that id wasn't legit dumbass", Toast.LENGTH_LONG).show();
    }

    @Override
    public void email_user() {

        if (userEmailAddress != null) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmailAddress});
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
            else {
                present_error();
            }
        }
    }
}
