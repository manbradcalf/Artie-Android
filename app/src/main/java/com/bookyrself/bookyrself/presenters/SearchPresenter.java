package com.bookyrself.bookyrself.presenters;

import android.util.Log;

import com.bookyrself.bookyrself.models.SerializedModels.SearchRequest.Body;
import com.bookyrself.bookyrself.models.SerializedModels.SearchRequest.Bool;
import com.bookyrself.bookyrself.models.SerializedModels.SearchRequest.Bool_;
import com.bookyrself.bookyrself.models.SerializedModels.SearchRequest.Date;
import com.bookyrself.bookyrself.models.SerializedModels.SearchRequest.Filter;
import com.bookyrself.bookyrself.models.SerializedModels.SearchRequest.Match;
import com.bookyrself.bookyrself.models.SerializedModels.SearchRequest.MultiMatch;
import com.bookyrself.bookyrself.models.SerializedModels.SearchRequest.Must;
import com.bookyrself.bookyrself.models.SerializedModels.SearchRequest.Must_;
import com.bookyrself.bookyrself.models.SerializedModels.SearchRequest.Query;
import com.bookyrself.bookyrself.models.SerializedModels.SearchRequest.Range;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseEvents.SearchResponse2;
import com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.SearchResponseUsers;
import com.bookyrself.bookyrself.services.SearchService;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by benmedcalf on 9/16/17.
 */

public class SearchPresenter {
    private static final int EVENT_SEARCH_FLAG = 1;
    private final SearchPresenterListener mListener;
    private final SearchService mService;

    /**
     * Constructor
     */
    public SearchPresenter(SearchPresenterListener listener) {
        this.mListener = listener;
        this.mService = new SearchService();

    }

    /**
     * Methods
     */
    public void executeSearch(int searchType, String what, String where, String fromWhen, String toWhen) {
        mListener.showProgressbar(true);
        final Query query = createQuery(what, where, fromWhen, toWhen);
        final Body body = new Body();
        body.setQuery(query);
        body.setSize(100);
        //TODO: Make the index and type toggleable to users
        if (searchType == EVENT_SEARCH_FLAG) {
            mService
                    .getAPI()
                    .executeEventsSearch(body)
                    .enqueue(new Callback<SearchResponse2>() {
                        @Override
                        public void onResponse(Call<SearchResponse2> call, Response<SearchResponse2> response) {
                            Log.i(this.toString(), response.toString());
                            if (response.body() != null) {
                                List<com.bookyrself.bookyrself.models.SerializedModels.SearchResponseEvents.Hit> hits = response.body().getHits().getHits();
                                mListener.searchEventsResponseReady(hits);
                            }
                        }

                        @Override
                        public void onFailure(Call<SearchResponse2> call, Throwable t) {
                            Log.e(getClass().toString(), call.request().body().toString());
                            Log.e(getClass().toString(), t.getMessage());
                            mListener.showError();
                        }
                    });
        } else {
            mService
                    .getAPI()
                    .executeUsersSearch(body)
                    .enqueue(new Callback<SearchResponseUsers>() {
                        @Override
                        public void onResponse(Call<SearchResponseUsers> call, Response<SearchResponseUsers> response) {
                            if (response.body() != null) {
                                List<com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Hit> hits = response.body().getHits().getHits();
                                mListener.searchUsersResponseReady(hits);
                            } else if (response.errorBody() != null) {
                                mListener.showError();
                            }
                        }

                        @Override
                        public void onFailure(Call<SearchResponseUsers> call, Throwable t) {
                            Log.e(getClass().toString(), call.request().body().toString());
                            Log.e(getClass().toString(), t.getMessage());
                            mListener.showError();
                        }
                    });
        }
    }

    private Query createQuery(String what, String where, String fromWhen, String toWhen) {
        List<String> fields = Arrays.asList("username", "tags", "eventname");
        Query query = new Query();
        Bool bool = new Bool();
        List<Must> musts = new ArrayList<>();

        // Set the "Where"
        if (!where.equals("")) {
            Must must1 = new Must();
            Match match1 = new Match();
            match1.setCitystate(where);
            must1.setMatch(match1);
            musts.add(must1);
        }

        // Set the "what"
        if (!what.equals("")) {
            Must must2 = new Must();
            MultiMatch multiMatch = new MultiMatch();
            multiMatch.setFields(fields);
            multiMatch.setQuery(what);
            must2.setMultiMatch(multiMatch);
            musts.add(must2);
        }

        if (!musts.isEmpty()) {
            bool.setMust(musts);
        }
        query.setBool(bool);

        //TODO: this coniditional check with string literals is gross, fix this at some point
        // Set the daterange
        if (!toWhen.equals("To") && !fromWhen.equals("From")) {
            Filter filter = new Filter();
            Bool_ bool_ = new Bool_();
            Must_ must_ = new Must_();
            Range range = new Range();
            Date date = new Date();
            date.setLte(toWhen);
            date.setGte(fromWhen);
            range.setDate(date);
            must_.setRange(range);
            bool_.setMust(must_);
            filter.setBool(bool_);
            bool.setFilter(filter);
        }

        return query;
    }

    public void setStartDate(String date) {
        mListener.startDateChanged(date);
    }

    public void setEndDate(String date) {
        mListener.endDateChanged(date);
    }

    public void clearStartDate() {
        mListener.startDateChanged("From");
    }

    public void clearEndDate() {
        mListener.endDateChanged("To");
    }

    /**
     * Contract / Listener
     */
    public interface SearchPresenterListener {
        void searchEventsResponseReady(List<com.bookyrself.bookyrself.models.SerializedModels.SearchResponseEvents.Hit> hits);

        void searchUsersResponseReady(List<com.bookyrself.bookyrself.models.SerializedModels.SearchResponseUsers.Hit> hits);

        void startDateChanged(String date);

        void endDateChanged(String date);

        void showProgressbar(Boolean bool);

        void itemSelected(String id, int flag);

        void showError();
    }
}
