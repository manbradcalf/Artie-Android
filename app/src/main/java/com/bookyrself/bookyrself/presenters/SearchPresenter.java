package com.bookyrself.bookyrself.presenters;

import android.util.Log;

import com.bookyrself.bookyrself.SearchService;
import com.bookyrself.bookyrself.models.searchrequest.Bool;
import com.bookyrself.bookyrself.models.searchrequest.Match;
import com.bookyrself.bookyrself.models.searchrequest.MultiMatch;
import com.bookyrself.bookyrself.models.searchrequest.Must;
import com.bookyrself.bookyrself.models.searchrequest.Body;
import com.bookyrself.bookyrself.models.searchrequest.Query;
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

    /**
     * Contract / Listener
     */
    public interface SearchPresenterListener {
        void searchResponseReady(List<Hit> hits);

        void startDateChanged(String date);

        void endDateChanged(String date);
    }


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
    public void executeSearch(FirebaseDatabase db, String what, String where, String fromWhen, String toWhen) {
        final Query query = createQuery(what, where, fromWhen, toWhen);
        final Body body = new Body();
        body.setQuery(query);
        SearchRequest request = new SearchRequest();
        request.setBody(body);
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

        dbref = db.getReference("search/response");
        childEventListener = dbref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i(this.toString(), dataSnapshot.toString());
                SearchEventsResponse responseEvents = dataSnapshot.child("hits").getValue(SearchEventsResponse.class);
                if (responseEvents.getHits() != null) {
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

    private Query createQuery(String what, String where, String fromWhen, String toWhen) {
        List<String> fields = Arrays.asList("username", "tags", "eventname");
        Query query = new Query();
        Bool bool = new Bool();
        Must must1 = new Must();
        Match match1 = new Match();
        match1.setCitystate(where);
        must1.setMatch(match1);
        Must must2 = new Must();
        MultiMatch multiMatch = new MultiMatch();
        multiMatch.setFields(fields);
        multiMatch.setQuery(what);
        must2.setMultiMatch(multiMatch);
        List<Must> musts = new ArrayList<>();
        musts.add(must1);
        musts.add(must2);
        bool.setMust(musts);
        query.setBool(bool);

        // Set the citystate

        // Set the what
        query.getBool().getMust().get(0).getMatch().setCitystate(where);
        query.getBool().getMust().get(1).getMultiMatch().setQuery(what);
        // Set the range
//        Query.getBool().getFilter().getBool().getMust().get(0).getRange().getDate().setGte(fromWhen);
//        Query.getBool().getFilter().getBool().getMust().get(0).getRange().getDate().setLte(toWhen);

        return query;
    }

    public void setStartDate(String date) {
        mListener.startDateChanged(date);
    }

    public void setEndDate(String date) {
        mListener.endDateChanged(date);
    }
}
