package com.bookyrself.bookyrself.data.contacts;

import com.bookyrself.bookyrself.data.serverModels.user.User;

import java.util.Map;

import io.reactivex.Flowable;

interface ContactsDataSource {

    Flowable<Map.Entry<String, User>> getContactsForUser(String userId);

}
