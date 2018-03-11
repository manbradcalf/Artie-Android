package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.CardView;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.presenters.SearchPresenter;
import com.bookyrself.bookyrself.utils.DatePickerDialogFragment;
import com.bookyrself.bookyrself.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchActivity extends MainActivity implements SearchPresenter.SearchPresenterListener {

    public static final int FLAG_START_DATE = 2;
    public static final int FLAG_END_DATE = 3;
    private static final String LIST_STATE_KEY = "LIST_STATE";
    private static final int USER_SEARCH_FLAG = 0;
    private static final int EVENT_SEARCH_FLAG = 1;
    private SearchView searchViewWhat;
    private SearchView searchViewWhere;
    private ProgressBar progressBar;
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
    private ResultsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Boolean boolSearchEditable = false;
    private Parcelable listState;
    private View emptyState;
    private TextView emptyStateText;
    private ImageView emptyStateImage;

    int getContentViewId() {
        return R.layout.activity_search;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_search;
    }

    @Override
    void setLayout() {
        presenter = new SearchPresenter(this);
        recyclerView = findViewById(R.id.search_recycler_view);
        adapter = new ResultsAdapter();
        recyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        searchViewWhat = findViewById(R.id.search_what);
        searchViewWhat.setQueryHint(getString(R.string.search_what_query_hint));
        searchViewWhere = findViewById(R.id.search_where);
        searchViewWhere.setVisibility(View.GONE);
        searchViewWhere.setQueryHint(getString(R.string.search_where_query_hint));
        fromButton = findViewById(R.id.from_button);
        fromButton.setVisibility(View.GONE);
        toButton =  findViewById(R.id.to_button);
        toButton.setVisibility(View.GONE);
        usersButton = findViewById(R.id.users_toggle);
        usersButton.setVisibility(View.GONE);
        eventsButton = findViewById(R.id.events_toggle);
        eventsButton.setVisibility(View.GONE);
        radioGroup = findViewById(R.id.radio_group_search);
        radioGroup.check(R.id.users_toggle);
        searchButton = findViewById(R.id.search_btn);
        searchButton.setVisibility(View.GONE);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        emptyState = findViewById(R.id.empty_state_search);
        emptyStateText = findViewById(R.id.empty_state_text);
        emptyStateImage = findViewById(R.id.empty_state_image);


        /**
         * SearchView for What field
         */
        searchViewWhat.setOnSearchClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
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
            public void onClick(View view) {
                DatePickerDialogFragment dialog = new DatePickerDialogFragment();
                dialog.setFlag(FLAG_START_DATE);
                dialog.setmSearchPresenter(presenter);
                dialog.show(getFragmentManager(), "datePicker");
            }
        });

        toButton.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                DatePickerDialogFragment dialog = new DatePickerDialogFragment();
                dialog.setFlag(FLAG_END_DATE);
                dialog.setmSearchPresenter(presenter);
                dialog.show(getFragmentManager(), "datePicker");
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()

        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

            }
        });

        //TODO: I don't love the "search button". Could get rid of it? Could change it?
        // Either way, the following code will be what happens when a user submits a search
        // from the searchactivity
        searchButton.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                if (!boolSearchEditable) {
                    if (eventsButton.isChecked()) {
                        eventsResults = null;
                        usersResults = null;
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
                        eventsResults = null;
                        usersResults = null;
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
                    showSearchBar(true);
                }
            }
        });
    }

    @Override
    void checkAuth() {

    }


    @Override
    public void searchEventsResponseReady
            (List<com.bookyrself.bookyrself.models.SearchResponseEvents.Hit> hits) {

        // I hide the recyclerview to show the error empty state
        // If the previous search returned an error empty state, recyclerview
        // will have a visibility of GONE here. Fix that
        if (recyclerView.getVisibility() == View.GONE) {
            recyclerView.setVisibility(View.VISIBLE);
        }

        // If the last empty state was an error, make sure that it is now
        // a generic failed search. No errors will hit this method, so this is
        // safe.
        if (hits.size() == 0) {
            emptyStateText.setText(R.string.search_activity_no_results);
            emptyStateImage.setImageDrawable(getDrawable(R.drawable.ic_binoculars));
            emptyState.setVisibility(View.VISIBLE);
            showSearchBar(true);
        } else {
            emptyState.setVisibility(View.GONE);
        }

        eventsResults = hits;
        adapter.setViewType(ResultsAdapter.EVENT_VIEW_TYPE);
        boolSearchEditable = true;
        searchButton.setText("Edit Search");
        adapter.notifyDataSetChanged();
        showProgressbar(false);
    }

    @Override
    public void searchUsersResponseReady
            (List<com.bookyrself.bookyrself.models.SearchResponseUsers.Hit> hits) {

        // I hide the recyclerview to show the error empty state
        // If the previous search returned an error empty state, recyclerview
        // will have a visibility of GONE here. Fix that
        if (recyclerView.getVisibility() == View.GONE) {
            recyclerView.setVisibility(View.VISIBLE);
        }

        // If there are no results, update the empty state to show the binoculars and no results copy
        // If there are results, set the empty state to invisible
        if (hits.size() == 0) {
            emptyStateText.setText(R.string.search_activity_no_results);
            emptyStateImage.setImageDrawable(getDrawable(R.drawable.ic_binoculars));
            emptyState.setVisibility(View.VISIBLE);
            showSearchBar(true);
        } else {
            emptyState.setVisibility(View.GONE);
        }

        usersResults = hits;
        adapter.setViewType(ResultsAdapter.USER_VIEW_TYPE);
        boolSearchEditable = true;
        searchButton.setText("Edit Search");
        adapter.notifyDataSetChanged();
        showProgressbar(false);
    }

    private void showSearchBar(Boolean bool) {
        if (bool) {
            searchViewWhat.setVisibility(View.VISIBLE);
            searchViewWhere.setVisibility(View.VISIBLE);
            toButton.setVisibility(View.VISIBLE);
            fromButton.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            searchButton.setText(R.string.title_search);
            radioGroup.setVisibility(View.VISIBLE);
        } else {
            searchViewWhat.setVisibility(View.GONE);
            searchViewWhere.setVisibility(View.GONE);
            toButton.setVisibility(View.GONE);
            fromButton.setVisibility(View.GONE);
            searchButton.setVisibility(View.GONE);
            radioGroup.setVisibility(View.GONE);
        }
    }

    @Override
    public void startDateChanged(String date) {
        fromButton.setText(date);
    }

    @Override
    public void endDateChanged(String date) {
        toButton.setText(date);
    }

    @Override
    public void showProgressbar(Boolean bool) {
        if (bool) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    //TODO: Is imgUrl needed as a param here?
    @Override
    public void itemSelected(String id, String imgUrl, int flag) {
        if (flag == EVENT_SEARCH_FLAG) {
            Intent intent = new Intent(this, EventDetailActivity.class);
            intent.putExtra("eventId", id);
            intent.putExtra("imgUrl", imgUrl);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, UserDetailActivity.class);
            intent.putExtra("userId", id);
            startActivity(intent);
        }
    }

    @Override
    public void showError() {
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        emptyStateText.setText(R.string.search_error);
        emptyStateImage.setImageDrawable(getDrawable(R.drawable.ic_error_empty_state));
        emptyState.setVisibility(View.VISIBLE);
        showSearchBar(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        //Save list state
        listState = layoutManager.onSaveInstanceState();
        state.putParcelable(LIST_STATE_KEY, listState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (listState != null) {
            layoutManager.onRestoreInstanceState(listState);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (listState != null) {
            layoutManager.onRestoreInstanceState(listState);
        }
    }

    /**
     * Adapter
     */
    class ResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int USER_VIEW_TYPE = 0;
        private static final int EVENT_VIEW_TYPE = 1;
        private int mViewType;


        public ResultsAdapter() {

        }

        public void setViewType(int viewType) {
            mViewType = viewType;
        }


        @Override
        public int getItemViewType(int position) {
            return mViewType;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;

            switch (viewType) {
                case 0:
                    view = getLayoutInflater().inflate(R.layout.item_user_search_result, parent, false);
                    return new ViewHolderUsers(view);
                case 1:
                    view = getLayoutInflater().inflate(R.layout.item_event_search_result, parent, false);
                    return new ViewHolderEvents(view);
                default:
                    return null;
            }

        }

        //TODO: The app crashes if any of the properties in _source are null. How to remedy this?
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            if (holder.getItemViewType() == USER_VIEW_TYPE) {
                //TODO: Was getting index out of bounds errors here when toggling between Events and Users. Not sure how I feel about this check
                if (usersResults.size() > position) {
                    ViewHolderUsers viewHolderUsers = (ViewHolderUsers) holder;
                    viewHolderUsers.userCityStateTextView.setText(usersResults
                            .get(position)
                            .get_source()
                            .getCitystate());

                    viewHolderUsers.userNameTextView.setText(usersResults
                            .get(position)
                            .get_source()
                            .getUsername());

                    final int adapterPosition = holder.getAdapterPosition();

                    Picasso.with(getApplicationContext())
                            .load("https://f4.bcbits.com/img/0009619513_10.jpg")
                            .placeholder(R.drawable.ic_profile_black_24dp)
                            .error(R.drawable.ic_profile_black_24dp)
                            .transform(new RoundedTransformation(50, 4))
                            .resizeDimen(R.dimen.user_image_thumb_list_height, R.dimen.user_image_thumb_list_width)
                            .centerCrop()
                            .into(viewHolderUsers.userProfileImageThumb);

                    viewHolderUsers.userCardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            itemSelected(usersResults
                            .get(adapterPosition)
                            .get_id(), usersResults.get(adapterPosition).get_source().getPicture(), USER_VIEW_TYPE);
                        }
                    });
                }
            } else {
                if (eventsResults.size() > position) {
                    ViewHolderEvents viewHolderEvents = (ViewHolderEvents) holder;
                    viewHolderEvents.eventNameTextView.setText(eventsResults
                            .get(position)
                            .get_source()
                            .getEventname());
                    viewHolderEvents.eventHostTextView.setText(getString(R.string.event_item_hosted_by,
                            eventsResults.get(position)
                            .get_source()
                            .getHost()
                            .get(0)
                            .getUsername()));
                    viewHolderEvents.eventCityStateTextView.setText(getString(R.string.event_item_citystate,
                            eventsResults.get(position)
                            .get_source()
                            .getCitystate()));
                    //TODO: Creating adapterPosition here to be used in onClick feels like a hack but isn't particularly egregious IMO.
                    final int adapterPosition = holder.getAdapterPosition();

                    Picasso.with(getApplicationContext())
                            .load(eventsResults
                                    .get(position)
                                    .get_source()
                                    .getPicture())
                            .placeholder(R.drawable.ic_profile_black_24dp)
                            .error(R.drawable.ic_profile_black_24dp)
                            .transform(new RoundedTransformation(50, 4))
                            .resizeDimen(R.dimen.user_image_thumb_list_height, R.dimen.user_image_thumb_list_width)
                            .centerCrop()
                            .into(viewHolderEvents.eventImageThumb);

                    viewHolderEvents.eventCardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            itemSelected(eventsResults
                                    .get(adapterPosition)
                                    .get_id(), eventsResults.get(adapterPosition).get_source().getPicture(), EVENT_VIEW_TYPE);
                        }
                    });
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

        class ViewHolderEvents extends RecyclerView.ViewHolder {
            public CardView eventCardView;
            public TextView eventCityStateTextView;
            public TextView eventHostTextView;
            public TextView eventNameTextView;
            public ImageView eventImageThumb;

            public ViewHolderEvents(View view) {
                super(view);
                eventCardView = view.findViewById(R.id.search_result_card_events);
                eventCityStateTextView = view.findViewById(R.id.event_location_search_result);
                eventHostTextView = view.findViewById(R.id.event_host_search_result);
                eventNameTextView = view.findViewById(R.id.eventname_search_result);
                eventImageThumb = view.findViewById(R.id.event_image_search_result);
            }
        }

        class ViewHolderUsers extends RecyclerView.ViewHolder {
            public CardView userCardView;
            public TextView userCityStateTextView;
            public TextView userNameTextView;
            public ImageView userProfileImageThumb;


            public ViewHolderUsers(View itemView) {
                super(itemView);
                userCardView = itemView.findViewById(R.id.search_result_card_users);
                userCityStateTextView = itemView.findViewById(R.id.user_location_search_result);
                userNameTextView = itemView.findViewById(R.id.username_search_result);
                userProfileImageThumb = itemView.findViewById(R.id.user_image_search_result);
            }
        }
    }
}
