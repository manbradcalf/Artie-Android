package com.bookyrself.bookyrself.presenters;

import android.util.Log;

import com.bookyrself.bookyrself.FirebaseService;
import com.bookyrself.bookyrself.SearchService;
import com.bookyrself.bookyrself.models.EventDetailResponse.EventDetailResponse;
import com.bookyrself.bookyrself.models.SearchResponseEvents.Hit;
import com.bookyrself.bookyrself.models.SearchResponseEvents.SearchResponse2;
import com.bookyrself.bookyrself.models.searchrequest.Body;
import com.bookyrself.bookyrself.models.searchrequest.Bool;
import com.bookyrself.bookyrself.models.searchrequest.Bool_;
import com.bookyrself.bookyrself.models.searchrequest.Date;
import com.bookyrself.bookyrself.models.searchrequest.Filter;
import com.bookyrself.bookyrself.models.searchrequest.Match;
import com.bookyrself.bookyrself.models.searchrequest.MultiMatch;
import com.bookyrself.bookyrself.models.searchrequest.Must;
import com.bookyrself.bookyrself.models.searchrequest.Must_;
import com.bookyrself.bookyrself.models.searchrequest.Query;
import com.bookyrself.bookyrself.models.searchrequest.Range;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 11/22/17.
 */

public class EventDetailPresenter {

    private final EventDetailPresenterListener mListener;
    private final FirebaseService mFirebaseService;
    private DatabaseReference dbref;
    private ChildEventListener childEventListener;
    private FirebaseDatabase db;

    /**
     * Contract / Listener
     */
    public interface EventDetailPresenterListener {
        void eventDataResponseReady(EventDetailResponse data, String imgUrl);

        void showProgressbar(Boolean bool);

        void userThumbReady(String response, String id);
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
                        EventDetailResponse data = response.body();
                        mListener.eventDataResponseReady(data, imgUrl);
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
