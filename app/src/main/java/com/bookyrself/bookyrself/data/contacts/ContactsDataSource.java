package com.bookyrself.bookyrself.data.contacts;

import com.bookyrself.bookyrself.data.serverModels.User.User;

import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;

interface ContactsDataSource {

    Observable<Map.Entry<String, User>> getContactsForUser(String userId);

}
