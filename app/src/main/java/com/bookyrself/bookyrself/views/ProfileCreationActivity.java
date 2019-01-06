package com.bookyrself.bookyrself.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bookyrself.bookyrself.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileCreationActivity extends AppCompatActivity {

    @BindView(R.id.profile_creation_toolbar)
    android.support.v7.widget.Toolbar toolbar;
    @BindView(R.id.profile_creation_submit_button)
    Button submitButton;
    @BindView(R.id.profile_creation_bio)
    EditText bioEditText;
    @BindView(R.id.profile_creation_username)
    EditText usernameEditText;
    @BindView(R.id.profile_creation_location)
    EditText locationEditText;
    @BindView(R.id.profile_creation_tags)
    EditText tagsEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);
        ButterKnife.bind(this);
        String username = getIntent().getStringExtra("Username");
        String location = getIntent().getStringExtra("Location");
        String bio = getIntent().getStringExtra("Bio");
        String tags = getIntent().getStringExtra("Tags");

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

        if (tags != null) {
            tagsEditText.setText(tags.replaceAll("\\[|]|, $", ""));
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                if (!TextUtils.isEmpty(usernameEditText.getText().toString())) {
                    returnIntent.putExtra("username", usernameEditText.getText().toString());
                }
                if (!TextUtils.isEmpty(bioEditText.getText().toString())) {
                    returnIntent.putExtra("bio", bioEditText.getText().toString());
                }
                if (!TextUtils.isEmpty(locationEditText.getText().toString())) {
                    returnIntent.putExtra("location", locationEditText.getText().toString());
                }
                if (!TextUtils.isEmpty(tagsEditText.getText().toString())) {
                    returnIntent.putExtra("tags", tagsEditText.getText().toString());
                }
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

}
