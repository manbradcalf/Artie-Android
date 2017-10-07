package com.bookyrself.bookyrself.presenters;

import android.widget.TextView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.SearchService;
import com.bookyrself.bookyrself.models.SearchRequest;
import com.bookyrself.bookyrself.models.searchresponse.Hit;
import com.bookyrself.bookyrself.models.searchresponse.SearchEventsResponse;
import com.bookyrself.bookyrself.models.searchresponse._source;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        void searchResponseReady(List<Hit> hits, String query);

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
    //TODO: Add when
    public void executeSearch(FirebaseDatabase db, String what, String where) {
        SearchRequest request = new SearchRequest();
        final String query = createQuery(what, where);
        request.setQ(query);
        request.setIndex("firebase");
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
                SearchEventsResponse responseEvents = dataSnapshot.child("hits").getValue(SearchEventsResponse.class);
                if (responseEvents.getHits() != null) {
                    List<Hit> hits = responseEvents.getHits();
                    mListener.searchResponseReady(hits, query);
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

    private String createQuery(String what, String where) {
        String query = "";
        if (!what.equals("") && !where.equals("")){
            query = String.format("tags:%s, users:%s, eventname:%s, citystate:%s", what, what, what, where);
        } else if (what.equals("") && !where.equals("")) {
            query = String.format("citystate:%s", where);
        } else if (!what.equals("") && where.equals("")) {
            query = String.format("tags:%s, users:%s, eventname:%s", what, what, what);
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
