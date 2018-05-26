package com.bookyrself.bookyrself.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bookyrself.bookyrself.R;

public class ProfileCreationActivity extends AppCompatActivity {

    private Button submitButton;
    private EditText bio;
    private EditText username;
    private EditText location;
    private EditText tags;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creation);
        submitButton = findViewById(R.id.profile_creation_submit_button);
        bio = findViewById(R.id.profile_creation_bio);
        username = findViewById(R.id.profile_creation_username);
        location = findViewById(R.id.profile_creation_location);
        tags = findViewById(R.id.profile_creation_tags);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("username", username.getText());
                returnIntent.putExtra("bio", bio.getText().toString());
                returnIntent.putExtra("location", location.getText());
                returnIntent.putExtra("tags", tags.getText().toString());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

}
