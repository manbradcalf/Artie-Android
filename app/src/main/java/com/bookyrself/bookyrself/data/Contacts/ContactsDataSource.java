package com.bookyrself.bookyrself.data.Contacts;

import com.bookyrself.bookyrself.data.ServerModels.User.User;

import java.util.Map;

import io.reactivex.Flowable;

interface ContactsDataSource {

    Flowable<Map.Entry<String, User>> getContactsForUser(String userId);

}
