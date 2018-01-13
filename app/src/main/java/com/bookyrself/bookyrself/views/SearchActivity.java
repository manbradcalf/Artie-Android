package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.presenters.SearchPresenter;
import com.bookyrself.bookyrself.utils.DatePickerDialogFragment;
import com.bookyrself.bookyrself.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchActivity extends MainActivity implements SearchPresenter.SearchPresenterListener {

    private static final String LIST_STATE_KEY = "LIST_STATE";
    private static final int USER_SEARCH_FLAG = 0;
    private static final int EVENT_SEARCH_FLAG = 1;
    public static final int FLAG_START_DATE = 2;
    public static final int FLAG_END_DATE = 3;

    private int searchType;
    private SearchView searchViewWhat;
    private SearchView searchViewWhere;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private Button fromButton;
    private Button toButton;
    private Button searchButton;
    private RadioButton eventsButton;
    private RadioButton usersButton;
    private RadioGroup radioGroup;
    private SearchPresenter presenter;
    private List<com.bookyrself.bookyrself.models.SearchResponseEvents.Hit> eventsResults;
    private List<com.bookyrself.bookyrself.models.SearchResponseUsers.Hit> usersResults;
    private RecyclerView recyclerView;
    private resultsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Boolean boolSearchEditable = false;
    private Parcelable listState;

    int getContentViewId() {
        return R.layout.activity_search;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_search;
    }

    @Override
    void setLayout() {
        presenter = new SearchPresenter(this, db);
        recyclerView = (RecyclerView) findViewById(R.id.search_recycler_view);
        adapter = new resultsAdapter();
        recyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        searchViewWhat = (SearchView) findViewById(R.id.search_what);
        searchViewWhat.setQueryHint(getString(R.string.search_what_query_hint));
        searchViewWhere = (SearchView) findViewById(R.id.search_where);
        searchViewWhere.setVisibility(View.GONE);
        searchViewWhere.setQueryHint(getString(R.string.search_where_query_hint));
        fromButton = (Button) findViewById(R.id.from_button);
        fromButton.setVisibility(View.GONE);
        toButton = (Button) findViewById(R.id.to_button);
        toButton.setVisibility(View.GONE);
        usersButton = findViewById(R.id.users_toggle);
        usersButton.setVisibility(View.GONE);
        eventsButton = findViewById(R.id.events_toggle);
        eventsButton.setVisibility(View.GONE);
        radioGroup = findViewById(R.id.radio_group_search);
        radioGroup.check(R.id.users_toggle);
        searchButton = (Button) findViewById(R.id.search_btn);
        searchButton.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        emptyStateText = findViewById(R.id.search_empty_state);
        emptyStateText.setVisibility(View.GONE);


                    /**
                     * SearchView for What field
                     */
                    searchViewWhat.setOnSearchClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick (View view){
                searchViewWhere.setVisibility((View.VISIBLE));
                fromButton.setVisibility(View.VISIBLE);
                toButton.setVisibility(View.VISIBLE);
                searchButton.setVisibility((View.VISIBLE));
                eventsButton.setVisibility(View.VISIBLE);
                usersButton.setVisibility(View.VISIBLE);
                searchButton.setText("Search!");
            }
            });

            /**
             * Buttons for "From" and "To" date fields
             */
        fromButton.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick (View view){
                DatePickerDialogFragment dialog = new DatePickerDialogFragment();
                dialog.setFlag(FLAG_START_DATE);
                dialog.setmSearchPresenter(presenter);
                dialog.show(getFragmentManager(), "datePicker");
            }
            });

        toButton.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick (View view){
                DatePickerDialogFragment dialog = new DatePickerDialogFragment();
                dialog.setFlag(FLAG_END_DATE);
                dialog.setmSearchPresenter(presenter);
                dialog.show(getFragmentManager(), "datePicker");
            }
            });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()

            {
                @Override
                public void onCheckedChanged (RadioGroup group,int checkedId){

            }
            });

            //TODO: I don't love the "search button". Could get rid of it? Could change it?
            // Either way, the following code will be what happens when a user submits a search
            // from the searchactivity
        searchButton.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick (View view){
                if (!boolSearchEditable) {
                    if (eventsButton.isChecked()) {
                        presenter.executeSearch(
                                EVENT_SEARCH_FLAG,
                                searchViewWhat.getQuery().toString(),
                                searchViewWhere.getQuery().toString(),
                                fromButton.getText().toString(),
                                toButton.getText().toString());
                        searchViewWhere.setVisibility((View.GONE));
                        fromButton.setVisibility(View.GONE);
                        toButton.setVisibility(View.GONE);
                        radioGroup.setVisibility(View.GONE);
                    } else if (usersButton.isChecked()) {
                        // CLearing results member variables so getSize can work sanely
//                        eventsResults = null;
//                        usersResults = null;
                        presenter.executeSearch(
                                USER_SEARCH_FLAG,
                                searchViewWhat.getQuery().toString(),
                                searchViewWhere.getQuery().toString(),
                                fromButton.getText().toString(),
                                toButton.getText().toString());
                        searchViewWhere.setVisibility((View.GONE));
                        fromButton.setVisibility(View.GONE);
                        toButton.setVisibility(View.GONE);
                        radioGroup.setVisibility(View.GONE);

                    }
                } else {
                    boolSearchEditable = false;
                    searchButton.setText("SEARCH!");
                    searchViewWhere.setVisibility((View.VISIBLE));
                    fromButton.setVisibility(View.VISIBLE);
                    toButton.setVisibility(View.VISIBLE);
                    radioGroup.setVisibility(View.VISIBLE);

                }
            }
            });
        }

        @Override
        void checkAuth () {

        }


        @Override
        public void searchEventsResponseReady
        (List < com.bookyrself.bookyrself.models.SearchResponseEvents.Hit > hits) {
            recyclerView.removeAllViewsInLayout();
            adapter.setViewType(1);
            boolSearchEditable = true;
            searchButton.setText("Edit Search");
            eventsResults = hits;
            adapter.notifyDataSetChanged();
            showProgressbar(false);
            if (hits.isEmpty()) {
                emptyStateText.setVisibility(View.VISIBLE);
            } else {
                emptyStateText.setVisibility(View.GONE);
            }
        }

        @Override
        public void searchUsersResponseReady
        (List < com.bookyrself.bookyrself.models.SearchResponseUsers.Hit > hits) {
            recyclerView.removeAllViewsInLayout();
            adapter.setViewType(0);
            boolSearchEditable = true;
            searchButton.setText("Edit Search");
            usersResults = hits;
            adapter.notifyDataSetChanged();
            showProgressbar(false);
            if (hits.isEmpty()) {
                emptyStateText.setVisibility(View.VISIBLE);
            } else {
                emptyStateText.setVisibility(View.GONE);
            }
        }

        @Override
        public void startDateChanged (String date){
            fromButton.setText(date);
        }

        @Override
        public void endDateChanged (String date){
            toButton.setText(date);
        }

        @Override
        public void showProgressbar (Boolean bool){
            if (bool) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void itemSelected (String id, String imgUrl){
            Intent intent = new Intent(this, EventDetailActivity.class);
            intent.putExtra("eventId", id);
            intent.putExtra("imgUrl", imgUrl);
            startActivity(intent);
        }

        @Override
        protected void onSaveInstanceState (Bundle state){
            super.onSaveInstanceState(state);

            //Save list state
            listState = layoutManager.onSaveInstanceState();
            state.putParcelable(LIST_STATE_KEY, listState);
        }

        @Override
        protected void onRestoreInstanceState (Bundle state){
            super.onRestoreInstanceState(state);
            if (listState != null) {
                layoutManager.onRestoreInstanceState(listState);
            }
        }

        @Override
        protected void onResume () {
            super.onResume();

            if (listState != null) {
                layoutManager.onRestoreInstanceState(listState);
            }
        }

        /**
         * Adapter
         */
        class resultsAdapter extends RecyclerView.Adapter<resultsAdapter.ViewHolder> {

            private static final int USER_VIEW_TYPE = 0;
            private static final int EVENT_VIEW_TYPE = 1;
            private int mViewType;


            public resultsAdapter() {

            }

            public void setViewType(int viewType) {
                mViewType = viewType;
            }

            class ViewHolder extends RecyclerView.ViewHolder {
                public TextView eventCityStateTextView;
                public TextView eventHostTextView;
                public TextView eventNameTextView;
                public TextView userIdTextView;
                public ImageView eventImageThumb;

                public TextView userCityStateTextView;
                public TextView userNameTextView;
                public ImageView userProfileImageThumb;


                public ViewHolder(View view) {
                    super(view);
                    if (mViewType == EVENT_VIEW_TYPE) {
                        eventCityStateTextView = view.findViewById(R.id.event_location_search_result);
                        eventHostTextView = view.findViewById(R.id.event_host_search_result);
                        eventNameTextView = view.findViewById(R.id.eventname_search_result);
                        eventImageThumb = view.findViewById(R.id.event_image_search_result);
                    } else {
                        userCityStateTextView = view.findViewById(R.id.user_location_search_result);
                        userNameTextView = view.findViewById(R.id.username_search_result);
                        userProfileImageThumb = view.findViewById(R.id.user_image_search_result);
                    }
                }
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view;
                ViewHolder vh;

                if (mViewType == USER_VIEW_TYPE) {
                    view = getLayoutInflater().inflate(R.layout.item_user_search_result, parent, false);
                    vh = new ViewHolder(view);
                } else {
                    view = getLayoutInflater().inflate(R.layout.item_event_search_result, parent, false);
                    vh = new ViewHolder(view);
                }
                return vh;
            }

            //TODO: The app crashes if any of the properties in _source are null. How to remedy this?
            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                {
                    if (mViewType == EVENT_VIEW_TYPE) {
                        holder.eventNameTextView.setText(eventsResults
                                .get(position)
                                .get_source()
                                .getEventname());
                        holder.eventHostTextView.setText(eventsResults
                                .get(position)
                                .get_source()
                                .getHost()
                                .get(0)
                                .getUsername());
                        holder.eventCityStateTextView.setText(eventsResults
                                .get(position)
                                .get_source()
                                .getCitystate());
                        //TODO: Creating adapterPosition here to be used in onClick feels like a hack but isn't particularly egregious IMO.
                        final int adapterPosition = holder.getAdapterPosition();

                        Picasso.with(getApplicationContext())
                                .load("https://pbs.twimg.com/profile_images/749059478146785281/_gziqED3.jpg")
                                .placeholder(R.drawable.ic_profile_black_24dp)
                                .error(R.drawable.ic_profile_black_24dp)
                                .transform(new RoundedTransformation(50, 4))
                                .resizeDimen(R.dimen.user_image_thumb_list_height, R.dimen.user_image_thumb_list_width)
                                .centerCrop()
                                .into(holder.eventImageThumb);

                        holder.eventImageThumb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                itemSelected(eventsResults
                                        .get(adapterPosition)
                                        .get_id(), eventsResults.get(adapterPosition).get_source().getPicture());
                            }
                        });


                    } else if (mViewType == USER_VIEW_TYPE) {
                        holder.userCityStateTextView.setText(usersResults
                                .get(position)
                                .get_source()
                                .getCitystate());

                        holder.userNameTextView.setText(usersResults
                                .get(position)
                                .get_source()
                                .getUsername());

                        Picasso.with(getApplicationContext())
                                .load("https://f4.bcbits.com/img/0009619513_10.jpg")
                                .placeholder(R.drawable.ic_profile_black_24dp)
                                .error(R.drawable.ic_profile_black_24dp)
                                .transform(new RoundedTransformation(50, 4))
                                .resizeDimen(R.dimen.user_image_thumb_list_height, R.dimen.user_image_thumb_list_width)
                                .centerCrop()
                                .into(holder.userProfileImageThumb);
                    }
                }
            }

            @Override
            public int getItemCount() {
                if (eventsResults != null) {
                    return eventsResults.size();
                } else if (usersResults != null) {
                    return usersResults.size();
                } else {
                    return 0;
                }
            }
        }
    }
