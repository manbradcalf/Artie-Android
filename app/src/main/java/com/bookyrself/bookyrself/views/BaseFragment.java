package com.bookyrself.bookyrself.views;

import android.graphics.drawable.Drawable;

interface BaseFragment {

    void showContent(boolean show);

    void showLoadingState(boolean show);

    void showEmptyState(String header, String subHeader, String buttonText, Drawable image);

    void hideEmptyState();

    void presentError(String message);

    void showSignedOutEmptyState();

}
