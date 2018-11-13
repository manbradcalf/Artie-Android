package com.bookyrself.bookyrself.views;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import com.bookyrself.bookyrself.presenters.DatePickerDialogPresenter;
import com.bookyrself.bookyrself.presenters.EventCreationPresenter;
import com.bookyrself.bookyrself.presenters.SearchPresenter;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by benmedcalf on 10/2/17.
 */

//TODO: Refactor this class to allow for any presenter, not just mSearchPresenter. See dataReady method
public class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener, DatePickerDialogPresenter.DatePickerDialogPresenterListener {

    public static final int FLAG_EVENT_CREATION = 1;
    public static final int FLAG_START_DATE = 2;
    public static final int FLAG_END_DATE = 3;
    private int flag;
    private SearchPresenter mSearchPresenter;
    private EventCreationPresenter mEventCreationPresenter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void setFlag(int i) {
        flag = i;
    }

    public void setSearchPresenter(SearchPresenter presenter) {
        mSearchPresenter = presenter;
    }

    public void setmEventCreationPresenter(EventCreationPresenter presenter) {
        mEventCreationPresenter = presenter;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateSelected(flag, format.format(calendar.getTime()));
    }


    @Override
    public void dateSelected(int flag, String date) {
        switch (flag) {
            case FLAG_EVENT_CREATION: mEventCreationPresenter.setDate(date);
                break;

            case FLAG_START_DATE: mSearchPresenter.setStartDate(date);
                break;

            case FLAG_END_DATE: mSearchPresenter.setEndDate(date);
                break;
        }
    }
}

