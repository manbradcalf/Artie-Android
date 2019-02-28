package com.bookyrself.bookyrself.data.Contacts;

import android.support.v4.util.Pair;

import com.bookyrself.bookyrself.data.ResponseModels.User.User;

import io.reactivex.Flowable;

interface ContactsDataSource {

    Flowable<Pair<String, User>> getContactsForUser(String userId);

}
