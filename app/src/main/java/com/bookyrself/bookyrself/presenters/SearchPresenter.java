package com.bookyrself.bookyrself.presenters;

import android.util.Log;
import android.widget.TextView;
import com.bookyrself.bookyrself.SearchService;
import com.bookyrself.bookyrself.models.searchrequest.Bool;
import com.bookyrself.bookyrself.models.searchrequest.Bool_;
import com.bookyrself.bookyrself.models.searchrequest.Date;
import com.bookyrself.bookyrself.models.searchrequest.Filter;
import com.bookyrself.bookyrself.models.searchrequest.Match;
import com.bookyrself.bookyrself.models.searchrequest.MultiMatch;
import com.bookyrself.bookyrself.models.searchrequest.Must;
import com.bookyrself.bookyrself.models.searchrequest.Body;
import com.bookyrself.bookyrself.models.searchrequest.Must_;
import com.bookyrself.bookyrself.models.searchrequest.Query;
import com.bookyrself.bookyrself.models.searchrequest.Range;
import com.bookyrself.bookyrself.models.searchrequest.SearchRequest;
import com.bookyrself.bookyrself.models.searchresponse.Hit;
import com.bookyrself.bookyrself.models.searchresponse.SearchEventsResponse;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
    private final SearchPresenterListener mListener;
    private final SearchService mService;
    private DatabaseReference dbref;
    private ChildEventListener childEventListener;
    private FirebaseDatabase db;

    /**
     * Contract / Listener
     */
    public interface SearchPresenterListener {
        void searchResponseReady(List<Hit> hits);

        void startDateChanged(String date);

        void endDateChanged(String date);

        void showProgressbar(Boolean bool);
    }


    /**
     * Constructor
     */
    public SearchPresenter(SearchPresenterListener listener, FirebaseDatabase db) {
        this.mListener = listener;
        this.mService = new SearchService();
        this.dbref = db.getReference("search/response");
        this.childEventListener = dbref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i(this.toString(), dataSnapshot.toString());
                SearchEventsResponse responseEvents = dataSnapshot.child("hits").getValue(SearchEventsResponse.class);
                Log.i(this.getClass().toString(), dataSnapshot.toString());
                if (responseEvents != null) {
                    List<Hit> hits = responseEvents.getHits();
                    mListener.searchResponseReady(hits);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * Methods
     */
    public void executeSearch(String what, String where, String fromWhen, String toWhen) {
        mListener.showProgressbar(true);
        final Query query = createQuery(what, where, fromWhen, toWhen);
        final Body body = new Body();
        body.setQuery(query);
        SearchRequest request = new SearchRequest();
        request.setBody(body);
        //TODO: Make the index and type toggleable to users
        request.setIndex("event_search");
        request.setType("events");
        mService
                .getAPI()
                .executeSearch(request)
                .enqueue(new Callback<List<Hit>>() {
                    @Override
                    public void onResponse(Call<List<Hit>> call, Response<List<Hit>> response) {

                    }

                    @Override
                    public void onFailure(Call<List<Hit>> call, Throwable t) {

                    }
                });
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
        if (!what.equals("")){
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
}
