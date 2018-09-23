package com.bookyrself.bookyrself.presenters;

import android.support.annotation.NonNull;

import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.services.FirebaseService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 11/22/17.
 */

public class EventDetailPresenter {

    private final EventDetailPresenterListener mListener;
    private final FirebaseService mFirebaseService;

    /**
     * Constructor
     */
    public EventDetailPresenter(EventDetailPresenterListener listener) {
        this.mListener = listener;
        this.mFirebaseService = new FirebaseService();
    }

    /**
     * Methods
     */
    public void getEventDetailData(String id) {
        mListener.showProgressbar(true);
        //TODO: Make the index and type toggleable to users
        mFirebaseService.getAPI().getEventData(id)
                .enqueue(new Callback<EventDetail>() {
                    @Override
                    public void onResponse(@NonNull Call<EventDetail> call, @NonNull Response<EventDetail> response) {
                        if (response.body() != null) {
                            EventDetail data = response.body();
                            mListener.eventDataResponseReady(data);
                        } else {
                            mListener.present_error();
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<EventDetail> call, @NonNull Throwable t) {

                    }
                });
    }

    //TODO: Will the final "id" variable here be stuck on the first id assigned?
    public void getUserThumbUrl(final String id) {
        mFirebaseService.getAPI().getUserThumbUrl(id)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        String data = response.body();
                        mListener.userThumbReady(data, id);
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                    }
                });
    }

    /**
     * Contract / Listener
     */
    public interface EventDetailPresenterListener {
        void eventDataResponseReady(EventDetail data);

        void showProgressbar(Boolean bool);

        void userThumbReady(String response, String id);

        void present_error();
    }
}
