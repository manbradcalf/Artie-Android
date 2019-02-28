package com.bookyrself.bookyrself.presenters;

import android.util.Log;

import com.bookyrself.bookyrself.data.ResponseModels.SearchRequest.Body;
import com.bookyrself.bookyrself.data.ResponseModels.SearchRequest.Bool;
import com.bookyrself.bookyrself.data.ResponseModels.SearchRequest.Bool_;
import com.bookyrself.bookyrself.data.ResponseModels.SearchRequest.Date;
import com.bookyrself.bookyrself.data.ResponseModels.SearchRequest.Filter;
import com.bookyrself.bookyrself.data.ResponseModels.SearchRequest.Match;
import com.bookyrself.bookyrself.data.ResponseModels.SearchRequest.MultiMatch;
import com.bookyrself.bookyrself.data.ResponseModels.SearchRequest.Must;
import com.bookyrself.bookyrself.data.ResponseModels.SearchRequest.Must_;
import com.bookyrself.bookyrself.data.ResponseModels.SearchRequest.Query;
import com.bookyrself.bookyrself.data.ResponseModels.SearchRequest.Range;
import com.bookyrself.bookyrself.data.ResponseModels.SearchResponseEvents.SearchResponse2;
import com.bookyrself.bookyrself.data.ResponseModels.SearchResponseUsers.SearchResponseUsers;
import com.bookyrself.bookyrself.services.SearchService;

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
    private final SearchPresenterListener listener;
    private final SearchService service;

    /**
     * Constructor
     */
    public SearchPresenter(SearchPresenterListener listener) {
        this.listener = listener;
        this.service = new SearchService();

    }

    /**
     * Methods
     */
    public void executeSearch(int searchType, String what, String where, String fromWhen, String toWhen) {
        listener.showProgressbar(true);
        final Query query = createQuery(what, where, fromWhen, toWhen);
        final Body body = new Body();
        body.setQuery(query);
        body.setSize(100);
        //TODO: Make the index and type toggleable to users
        if (searchType == EVENT_SEARCH_FLAG) {
            service
                    .getAPI()
                    .executeEventsSearch(body)
                    .enqueue(new Callback<SearchResponse2>() {
                        @Override
                        public void onResponse(Call<SearchResponse2> call, Response<SearchResponse2> response) {
                            Log.i(this.toString(), response.toString());
                            if (response.body() != null) {
                                List<com.bookyrself.bookyrself.data.ResponseModels.SearchResponseEvents.Hit> hits = response.body().getHits().getHits();
                                listener.searchEventsResponseReady(hits);
                            }
                        }

                        @Override
                        public void onFailure(Call<SearchResponse2> call, Throwable t) {
                            Log.e(getClass().toString(), call.request().body().toString());
                            Log.e(getClass().toString(), t.getMessage());
                            listener.showError();
                        }
                    });
        } else {
            service
                    .getAPI()
                    .executeUsersSearch(body)
                    .enqueue(new Callback<SearchResponseUsers>() {
                        @Override
                        public void onResponse(Call<SearchResponseUsers> call, Response<SearchResponseUsers> response) {
                            if (response.body() != null) {
                                List<com.bookyrself.bookyrself.data.ResponseModels.SearchResponseUsers.Hit> hits = response.body().getHits().getHits();
                                listener.searchUsersResponseReady(hits);
                            } else if (response.errorBody() != null) {
                                listener.showError();
                            }
                        }

                        @Override
                        public void onFailure(Call<SearchResponseUsers> call, Throwable t) {
                            Log.e(getClass().toString(), call.request().body().toString());
                            Log.e(getClass().toString(), t.getMessage());
                            listener.showError();
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
        listener.startDateChanged(date);
    }

    public void setEndDate(String date) {
        listener.endDateChanged(date);
    }

    public void clearStartDate() {
        listener.startDateChanged("From");
    }

    public void clearEndDate() {
        listener.endDateChanged("To");
    }

    /**
     * Contract / Listener
     */
    public interface SearchPresenterListener {
        void searchEventsResponseReady(List<com.bookyrself.bookyrself.data.ResponseModels.SearchResponseEvents.Hit> hits);

        void searchUsersResponseReady(List<com.bookyrself.bookyrself.data.ResponseModels.SearchResponseUsers.Hit> hits);

        void startDateChanged(String date);

        void endDateChanged(String date);

        void showProgressbar(Boolean bool);

        void itemSelected(String id, int flag);

        void showError();
    }
}
