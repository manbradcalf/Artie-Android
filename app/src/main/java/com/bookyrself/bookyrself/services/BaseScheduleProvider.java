package com.bookyrself.bookyrself.services;

import android.support.annotation.NonNull;

import io.reactivex.Scheduler;

public interface BaseScheduleProvider {

    @NonNull
    Scheduler computation();

    @NonNull
    Scheduler io();

    @NonNull
    Scheduler ui();
}
