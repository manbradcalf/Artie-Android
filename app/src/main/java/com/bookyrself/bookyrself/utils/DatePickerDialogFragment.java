package com.bookyrself.bookyrself.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import com.bookyrself.bookyrself.presenters.DatePickerDialogPresenter;
import com.bookyrself.bookyrself.presenters.SearchPresenter;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by benmedcalf on 10/2/17.
 */

public class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener, DatePickerDialogPresenter.DatePickerDialogPresenterListener {

    public static final int FLAG_START_DATE = 2;
    public static final int FLAG_END_DATE = 3;
    private int flag = 0;
    private SearchPresenter mSearchPresenter;

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

    public void setmSearchPresenter(SearchPresenter presenter) {
        mSearchPresenter = presenter;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);

        dateReady(flag, format.format(calendar.getTime()));
    }


    @Override
    public void dateReady(int flag, String date) {
        if (flag == FLAG_START_DATE) {
            mSearchPresenter.setStartDate(date);
        } else if (flag == FLAG_END_DATE) {
            mSearchPresenter.setEndDate(date);
        }
    }
}

