package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.models.EventDetailResponse.EventDetailResponse;
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
     * Contract / Listener
     */
    public interface EventDetailPresenterListener {
        void eventDataResponseReady(EventDetailResponse data, String imgUrl);

        void showProgressbar(Boolean bool);

        void userThumbReady(String response, String id);

        void present_error();
    }


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
    public void getEventDetailData(String id, final String imgUrl) {
        mListener.showProgressbar(true);
        //TODO: Make the index and type toggleable to users
        mFirebaseService.getAPI().getEventData(id)
                .enqueue(new Callback<EventDetailResponse>() {
                    @Override
                    public void onResponse(Call<EventDetailResponse> call, Response<EventDetailResponse> response) {
                        if (response.body() != null) {
                            EventDetailResponse data = response.body();
                            mListener.eventDataResponseReady(data, imgUrl);
                        } else {
                            mListener.present_error();
                        }

                    }

                    @Override
                    public void onFailure(Call<EventDetailResponse> call, Throwable t) {

                    }
                });
    }

    //TODO: Will the final "id" variable here be stuck on the first id assigned?
    public void getUserThumbUrl(final String id) {
        mFirebaseService.getAPI().getUserThumbUrl(id)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        String data = response.body();
                        mListener.userThumbReady(data, id);
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });
    }
}
