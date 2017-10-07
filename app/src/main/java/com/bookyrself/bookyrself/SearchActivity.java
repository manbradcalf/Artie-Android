package com.bookyrself.bookyrself;

import android.app.DatePickerDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bookyrself.bookyrself.models.searchresponse.Hit;
import com.bookyrself.bookyrself.presenters.SearchPresenter;
import com.bookyrself.bookyrself.utils.DatePickerDialogFragment;
import com.bookyrself.bookyrself.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchActivity extends MainActivity implements SearchPresenter.SearchPresenterListener {

    private SearchView searchViewWhat;
    private SearchView searchViewWhere;
    private TextView fromTextView;
    private TextView toTextView;
    private SearchPresenter presenter;
    private List<Hit> results;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

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
        presenter = new SearchPresenter(this);
        recyclerView = (RecyclerView) findViewById(R.id.search_recycler_view);
        adapter = new resultsAdapter();
        recyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        searchViewWhat = (SearchView) findViewById(R.id.search_what);
        searchViewWhat.setQueryHint("test");
        searchViewWhere = (SearchView) findViewById(R.id.search_where);
        searchViewWhere.setVisibility(View.GONE);
        fromTextView = (TextView) findViewById(R.id.from_textview);
        fromTextView.setVisibility(View.GONE);
        toTextView = (TextView) findViewById(R.id.to_textview);
        toTextView.setVisibility(View.GONE);

        searchViewWhat.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                presenter.executeSearch(db, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchViewWhat.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchViewWhere.setVisibility((View.VISIBLE));
                fromTextView.setVisibility(View.VISIBLE);
                toTextView.setVisibility(View.VISIBLE);
            }
        });

        fromTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialogFragment dialog = new DatePickerDialogFragment();
                dialog.setFlag(FLAG_START_DATE);
                dialog.setmSearchPresenter(presenter);
                dialog.show(getFragmentManager(), "datePicker");
            }
        });

        toTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialogFragment dialog = new DatePickerDialogFragment();
                dialog.setFlag(FLAG_END_DATE);
                dialog.setmSearchPresenter(presenter);
                dialog.show(getFragmentManager(), "datePicker");
            }
        });
    }

    @Override
    public void searchResponseReady(List<Hit> hits) {
        results = hits;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void startDateChanged(String date) {
        fromTextView.setText(date);
    }

    @Override
    public void endDateChanged(String date) {
        toTextView.setText(date);
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
            public ImageView profileImageThumb;


            public ViewHolder(View view) {
                super(view);
                cityStateTextView = view.findViewById(R.id.event_location_search_result);
                hostTextView = view.findViewById(R.id.event_host_search_result);
                eventNameTextView = view.findViewById(R.id.eventname_search_result);
                profileImageThumb = view.findViewById(R.id.user_image_search_result);
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
