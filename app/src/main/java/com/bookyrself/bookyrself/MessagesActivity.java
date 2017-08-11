package com.bookyrself.bookyrself;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MessagesActivity extends MainActivity {

    @Override
    int getContentViewId() {
        return R.layout.activity_messages;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_messages;
    }

}
