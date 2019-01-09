package com.bookyrself.bookyrself.presenters;

/**
 * Created by benmedcalf on 10/2/17.
 */

public class DatePickerDialogPresenter {
    private final DatePickerDialogPresenterListener mListener;

    /**
     * Constructor
     */
    //TODO: Currently not needed as I can use the SearchPresenter. It's late, I probably should use the dialogfragmentpresenter?
    public DatePickerDialogPresenter(DatePickerDialogPresenterListener listener) {
        mListener = listener;
    }

    /**
     * Contract / Listener
     */
    public interface DatePickerDialogPresenterListener {
        void dateSelected(int flag, String date);
    }



}
