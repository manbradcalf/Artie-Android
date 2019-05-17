package com.bookyrself.bookyrself.data.Profile;

import com.bookyrself.bookyrself.data.ServerModels.User.User;

import io.reactivex.Flowable;

public interface ProfileDataSource {

    Flowable<User> getProfileInfo(String userId);

    Flowable<User> updateProfileInfo(String userId, User user);

}
