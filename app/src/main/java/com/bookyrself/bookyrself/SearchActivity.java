package com.bookyrself.bookyrself;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bookyrself.bookyrself.presenters.SearchPresenter;
import com.bookyrself.bookyrself.utils.DatePickerDialogFragment;
import com.bookyrself.bookyrself.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchActivity extends MainActivity implements SearchPresenter.SearchPresenterListener {

    private SearchView searchViewWhat;
    private SearchView searchViewWhere;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private Button fromButton;
    private Button toButton;
    private Button searchButton;
    private SearchPresenter presenter;
    private List<com.bookyrself.bookyrself.models.SearchResponseEvents.Hit> results;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Boolean boolSearchEditable = false;

    public static final int FLAG_START_DATE = 0;
    public static final int FLAG_END_DATE = 1;

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
        searchViewWhat.setQueryHint("test");
        searchViewWhere = (SearchView) findViewById(R.id.search_where);
        searchViewWhere.setVisibility(View.GONE);
        fromButton = (Button) findViewById(R.id.from_button);
        fromButton.setVisibility(View.GONE);
        toButton = (Button) findViewById(R.id.to_button);
        toButton.setVisibility(View.GONE);
        searchButton = (Button) findViewById(R.id.search_btn);
        searchButton.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        emptyStateText = findViewById(R.id.search_empty_state);
        emptyStateText.setVisibility(View.GONE);


        /**
         * SearchView for What field
         */
        searchViewWhat.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchViewWhere.setVisibility((View.VISIBLE));
                fromButton.setVisibility(View.VISIBLE);
                toButton.setVisibility(View.VISIBLE);
                searchButton.setVisibility((View.VISIBLE));
                searchButton.setText("Search!");
            }
        });

        /**
         * Buttons for "From" and "To" date fields
         */
        fromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialogFragment dialog = new DatePickerDialogFragment();
                dialog.setFlag(FLAG_START_DATE);
                dialog.setmSearchPresenter(presenter);
                dialog.show(getFragmentManager(), "datePicker");
            }
        });

        toButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialogFragment dialog = new DatePickerDialogFragment();
                dialog.setFlag(FLAG_END_DATE);
                dialog.setmSearchPresenter(presenter);
                dialog.show(getFragmentManager(), "datePicker");
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!boolSearchEditable) {
                    presenter.executeSearch(
                            searchViewWhat.getQuery().toString(),
                            searchViewWhere.getQuery().toString(),
                            fromButton.getText().toString(),
                            toButton.getText().toString());
                    searchViewWhere.setVisibility((View.GONE));
                    fromButton.setVisibility(View.GONE);
                    toButton.setVisibility(View.GONE);
                } else {
                    boolSearchEditable = false;
                    searchButton.setText("SEARCH!");
                    searchViewWhere.setVisibility((View.VISIBLE));
                    fromButton.setVisibility(View.VISIBLE);
                    toButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    void checkAuth() {

    }


    @Override
    public void searchResponseReady(List<com.bookyrself.bookyrself.models.SearchResponseEvents.Hit> hits) {
        recyclerView.removeAllViewsInLayout();
        boolSearchEditable = true;
        searchButton.setText("Edit Search");
        results = hits;
        adapter.notifyDataSetChanged();
        showProgressbar(false);
        if (hits.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            emptyStateText.setVisibility(View.GONE);
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

    /**
     * Adapter
     */
    class resultsAdapter extends RecyclerView.Adapter<resultsAdapter.ViewHolder> {

        public resultsAdapter() {

        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView cityStateTextView;
            public TextView hostTextView;
            public TextView eventNameTextView;
            public TextView userIdTextView;
            public ImageView profileImageThumb;


            public ViewHolder(View view) {
                super(view);
                cityStateTextView = view.findViewById(R.id.event_location_search_result);
                hostTextView = view.findViewById(R.id.event_host_search_result);
                eventNameTextView = view.findViewById(R.id.eventname_search_result);
                profileImageThumb = view.findViewById(R.id.user_image_search_result);
                userIdTextView = view.findViewById(R.id.userid);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_event_search_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.eventNameTextView.setText(results.get(position).get_source().getEventname());
            holder.hostTextView.setText(results.get(position).get_source().getHost());
            holder.cityStateTextView.setText(results.get(position).get_source().getCitystate());
            holder.userIdTextView.setText(results.get(position).get_source().getGuid());
            Picasso.with(getApplicationContext())
                    .load("https://pbs.twimg.com/profile_images/749059478146785281/_gziqED3.jpg")
                    .placeholder(R.drawable.ic_profile_black_24dp)
                    .error(R.drawable.ic_profile_black_24dp)
                    .transform(new RoundedTransformation(50, 4))
                    .resizeDimen(R.dimen.user_image_thumb_list_height, R.dimen.user_image_thumb_list_width)
                    .centerCrop()
                    .into(holder.profileImageThumb);
        }

        @Override
        public int getItemCount() {
            if (results != null) {
                return results.size();
            } else {
                return 0;
            }
        }
    }
}
