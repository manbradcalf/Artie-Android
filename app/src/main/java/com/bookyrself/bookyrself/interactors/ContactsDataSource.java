package com.bookyrself.bookyrself.interactors;

import android.support.v4.util.Pair;

import com.bookyrself.bookyrself.models.SerializedModels.User.User;

import io.reactivex.Flowable;

interface ContactsDataSource {

    Flowable<Pair<String, User>> getContactsForUser(String userId);

}
