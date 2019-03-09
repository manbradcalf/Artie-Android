package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.data.ResponseModels.SearchResponseEvents.Hit;
import com.bookyrself.bookyrself.data.ResponseModels.SearchResponseEvents._source;
import com.bookyrself.bookyrself.presenters.SearchPresenter;
import com.bookyrself.bookyrself.utils.CircleTransform;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchFragment extends Fragment implements SearchPresenter.SearchPresenterListener {

    @BindView(R.id.search_what)
    SearchView searchViewWhat;
    @BindView(R.id.search_where)
    SearchView searchViewWhere;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.from_button)
    Button fromButton;
    @BindView(R.id.to_button)
    Button toButton;
    @BindView(R.id.search_btn)
    Button searchButton;
    @BindView(R.id.events_toggle)
    RadioButton eventsButton;
    @BindView(R.id.users_toggle)
    RadioButton usersButton;
    @BindView(R.id.radio_group_search)
    RadioGroup radioGroup;
    @BindView(R.id.empty_state_view)
    View emptyState;
    @BindView(R.id.empty_state_text_header)
    TextView emptyStateTextHeader;
    @BindView(R.id.empty_state_text_subheader)
    TextView emptyStateTextSubHeader;
    @BindView(R.id.empty_state_image)
    ImageView emptyStateImage;
    @BindView(R.id.empty_state_button)
    Button emptyStateButton;
    @BindView(R.id.search_recycler_view)
    RecyclerView recyclerView;

    private static final int USER_SEARCH_FLAG = 0;
    private static final int EVENT_SEARCH_FLAG = 1;
    public static final int FLAG_START_DATE = 2;
    public static final int FLAG_END_DATE = 3;
    private SearchPresenter presenter;
    private List<Hit> eventsResults;
    private List<com.bookyrself.bookyrself.data.ResponseModels.SearchResponseUsers.Hit> usersResults;
    private ResultsAdapter adapter;
    private Boolean boolSearchEditable = false;
    private StorageReference storageReference;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setLayout();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void onPause() {
        super.onPause();
        searchViewWhat.clearFocus();
        searchViewWhere.clearFocus();
    }

    @Override
    public void onResume() {
        super.onResume();
        searchViewWhere.clearFocus();
        searchViewWhat.clearFocus();
    }

    private void setLayout() {
        emptyStateButton.setVisibility(View.GONE);
        if (presenter == null) {
            presenter = new SearchPresenter(this);
        }
        if (adapter == null) {
            adapter = new ResultsAdapter();
        }
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        searchViewWhat.setQueryHint(getString(R.string.search_what_query_hint));
        searchViewWhere.setVisibility(View.GONE);
        searchViewWhere.setQueryHint(getString(R.string.search_where_query_hint));
        fromButton.setVisibility(View.GONE);
        toButton.setVisibility(View.GONE);
        radioGroup.check(R.id.users_toggle);
        searchButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        if (adapter.getItemCount() == 0) {
            emptyStateTextHeader.setText(getString(R.string.search_empty_state_header));
            emptyStateTextSubHeader.setText(getString(R.string.search_empty_state_subheader));
            emptyStateImage.setImageDrawable(getActivity().getDrawable(R.drawable.ic_minivan));
        } else {
            // Hit this else clause if the fragment is restarted with data already.
            // We need to show the edit search button and unselect the search view
            searchButton.setVisibility(View.VISIBLE);
            searchButton.setText(R.string.search_fragment_edit_search_btn_text);
            searchViewWhat.clearFocus();
            searchViewWhat.setSelected(false);
            searchViewWhat.setIconified(false);
            if (searchViewWhere.getQuery() != null) {
                searchViewWhere.setVisibility(View.VISIBLE);
            }
        }

        searchViewWhat.setOnSearchClickListener(view -> {
            searchViewWhere.setVisibility((View.VISIBLE));
            fromButton.setVisibility(View.VISIBLE);
            toButton.setVisibility(View.VISIBLE);
            searchButton.setVisibility((View.VISIBLE));
            eventsButton.setVisibility(View.VISIBLE);
            usersButton.setVisibility(View.VISIBLE);
            searchButton.setText(R.string.search_fragment_search_button_text);
        });


        fromButton.setOnClickListener(view -> {
            DatePickerDialogFragment dialog = new DatePickerDialogFragment();
            dialog.setFlag(FLAG_START_DATE);
            dialog.setSearchPresenter(presenter);
            dialog.show(getActivity().getFragmentManager(), "datePicker");
        });

        toButton.setOnClickListener(view -> {
            DatePickerDialogFragment dialog = new DatePickerDialogFragment();
            dialog.setFlag(FLAG_END_DATE);
            dialog.setSearchPresenter(presenter);
            dialog.show(getActivity().getFragmentManager(), "datePicker");
        });

        searchButton.setOnClickListener(view -> {
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
                    showFullSearchBar(false);
                } else if (usersButton.isChecked()) {
                    eventsResults = null;
                    usersResults = null;
                    presenter.executeSearch(
                            USER_SEARCH_FLAG,
                            searchViewWhat.getQuery().toString(),
                            searchViewWhere.getQuery().toString(),
                            fromButton.getText().toString(),
                            toButton.getText().toString());
                    showFullSearchBar(false);
                }
            } else {
                boolSearchEditable = false;
                searchButton.setText(R.string.search_fragment_search_button_text);
                showFullSearchBar(true);
            }
        });

    }


    @Override
    public void searchEventsResponseReady
            (List<Hit> hits) {

        if (recyclerView.getVisibility() == View.GONE) {
            recyclerView.setVisibility(View.VISIBLE);
        }

        // If the last empty state was an error, make sure that it is now
        // a generic failed search. No service errors will hit this method,
        // as they are caught by retrofit, so this is safe.
        if (hits.size() == 0) {
            emptyStateTextHeader.setText(R.string.search_activity_no_results_header);
            emptyStateTextSubHeader.setText(R.string.search_activity_no_results_subheader);
            emptyStateImage.setImageDrawable(getActivity().getDrawable(R.drawable.ic_binoculars));
            emptyState.setVisibility(View.VISIBLE);
            showFullSearchBar(false);
        } else {
            emptyState.setVisibility(View.GONE);
        }

        eventsResults = hits;
        adapter.setViewType(ResultsAdapter.EVENT_VIEW_TYPE);
        boolSearchEditable = true;
        searchButton.setText(R.string.search_fragment_edit_search_btn_text);
        adapter.notifyDataSetChanged();
        showProgressbar(false);
    }

    @Override
    public void searchUsersResponseReady
            (List<com.bookyrself.bookyrself.data.ResponseModels.SearchResponseUsers.Hit> hits) {

        if (recyclerView.getVisibility() == View.GONE) {
            recyclerView.setVisibility(View.VISIBLE);
        }

        // If the last empty state was an error, make sure that it is now
        // a generic failed search. No service errors will hit this method,
        // as they are caught by retrofit, so this is safe.
        if (hits.size() == 0) {
            emptyStateTextHeader.setText(R.string.search_activity_no_results_header);
            emptyStateTextSubHeader.setText(R.string.search_activity_no_results_subheader);
            emptyStateImage.setImageDrawable(getActivity().getDrawable(R.drawable.ic_binoculars));
            emptyState.setVisibility(View.VISIBLE);
            showFullSearchBar(false);
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

    private void showFullSearchBar(Boolean bool) {
        if (bool) {
            boolSearchEditable = false;
            searchViewWhat.setVisibility(View.VISIBLE);
            searchViewWhere.setVisibility(View.VISIBLE);
            toButton.setVisibility(View.VISIBLE);
            fromButton.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            searchButton.setText(R.string.title_search);
            radioGroup.setVisibility(View.VISIBLE);
        } else {
            boolSearchEditable = true;
            searchViewWhere.setVisibility(View.GONE);
            toButton.setVisibility(View.GONE);
            fromButton.setVisibility(View.GONE);
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

    @Override
    public void itemSelected(String id, int flag) {
        if (flag == EVENT_SEARCH_FLAG) {
            Intent intent = new Intent(getActivity(), EventDetailActivity.class);
            intent.putExtra("eventId", id);
            startActivity(intent);
        } else if (flag == USER_SEARCH_FLAG) {
            Intent intent = new Intent(getActivity(), UserDetailActivity.class);
            intent.putExtra("userId", id);
            startActivity(intent);
        } else {
            Log.e(this.getTag(), "Unknown Item type selected");
        }
    }

    @Override
    public void showError() {
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        emptyStateTextHeader.setText(R.string.error_header);
        emptyStateTextSubHeader.setText(R.string.search_error_subheader);
        emptyStateImage.setImageDrawable(getActivity().getDrawable(R.drawable.ic_error_empty_state));
        emptyState.setVisibility(View.VISIBLE);
        showFullSearchBar(false);
    }


    /**
     * Adapter
     */
    class ResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int USER_VIEW_TYPE = 0;
        private static final int EVENT_VIEW_TYPE = 1;
        private int mViewType;


        ResultsAdapter() {

        }

        void setViewType(int viewType) {
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
                    view = getLayoutInflater().inflate(R.layout.item_event, parent, false);
                    return new ViewHolderEvents(view);
                default:
                    return null;
            }

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            if (usersResults != null && holder.getItemViewType() == USER_VIEW_TYPE) {
                buildUserViewHolder(holder, position);
            } else if (eventsResults != null && holder.getItemViewType() == EVENT_VIEW_TYPE) {
                buildEventViewHolder(holder, position);
            } else {
                Log.e(this.getClass().toString(), "Provided neither Event or User viewholder type");
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

        private void buildEventViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (eventsResults.size() > position) {

                _source event = eventsResults.get(position).get_source();
                ViewHolderEvents viewHolderEvents = (ViewHolderEvents) holder;

                // Set Event Name
                if (event.getEventname() != null) {
                    viewHolderEvents.eventNameTextView.setText(event.getEventname());
                }


                // Set Hostname
                if (event.getHost() != null) {
                    if (event.getHost().getUsername() != null) {
                        viewHolderEvents.eventHostTextView.setText(getString(R.string.event_item_hosted_by,
                                event.getHost().getUsername()));
                    }
                }

                // Set Event Location
                if (event.getCitystate() != null) {
                    viewHolderEvents.eventCityStateTextView.setText(getString(R.string.event_item_citystate,
                            event.getCitystate()));
                }


                // Set Event Image thumbnail

                final StorageReference eventImageReference = storageReference.child("images/events/" + eventsResults.get(position).get_id());
                eventImageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                    // Add downloaded image to event item's ImageView
                    Picasso.with(getContext())
                            .load(uri)
                            .resize(148, 148)
                            .centerCrop()
                            .transform(new CircleTransform())
                            .into(viewHolderEvents.eventImageThumb);
                }).addOnFailureListener(exception -> {

                    // Set placeholder image, log error
                    viewHolderEvents.eventImageThumb.setImageDrawable(getContext().getDrawable(R.drawable.ic_calendar_black_24dp));
                    Log.e("Event image  not loaded", exception.getLocalizedMessage());
                });

                // Set onClickListener to fire off intent in itemSelected()
                viewHolderEvents.eventCardView.setOnClickListener(v -> itemSelected(eventsResults
                        .get(position)
                        .get_id(), EVENT_VIEW_TYPE));
            }
        }

        private void buildUserViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (usersResults.size() > position) {
                final ViewHolderUsers viewHolderUsers = (ViewHolderUsers) holder;
                viewHolderUsers.userCityStateTextView.setText(usersResults
                        .get(position)
                        .get_source()
                        .getCitystate());

                // Set username
                viewHolderUsers.userNameTextView.setText(usersResults
                        .get(position)
                        .get_source()
                        .getUsername());

                // Set tags
                if (usersResults.get(position).get_source().getTags() != null) {
                    StringBuilder listString = new StringBuilder();
                    for (String s : usersResults.get(position).get_source().getTags()) {
                        listString.append(s + ", ");
                    }
                    // Regex to trim the trailing comma
                    viewHolderUsers.userTagsTextView.setText(listString.toString().replaceAll(", $", ""));
                }


                // Set user image thumbnail
                final StorageReference profileImageReference = storageReference.child("images/users/" + usersResults.get(position).get_id());
                profileImageReference.getDownloadUrl().addOnSuccessListener(uri ->

                        // Add downloaded image to the user item's ImageView
                        Picasso.with(getContext())
                        .load(uri)
                        .resize(148, 148)
                        .centerCrop()
                        .transform(new CircleTransform())
                        .into(viewHolderUsers.userProfileImageThumb))
                        .addOnFailureListener(exception -> {
                            // Handle any errors
                            viewHolderUsers.userProfileImageThumb.setImageDrawable(getContext().getDrawable(R.drawable.ic_person_white_16dp));
                            Log.e("ProfileImage not loaded", exception.getLocalizedMessage());
                        });

                // Set onClickListener to fire off intent in itemSelected()
                viewHolderUsers.userCardView.setOnClickListener(v -> itemSelected(usersResults
                        .get(position)
                        .get_id(), USER_VIEW_TYPE));
            }
        }

        /**
         * ViewHolder for events
         */
        class ViewHolderEvents extends RecyclerView.ViewHolder {
            CardView eventCardView;
            TextView eventCityStateTextView;
            TextView eventHostTextView;
            TextView eventNameTextView;
            ImageView eventImageThumb;

            ViewHolderEvents(View view) {
                super(view);
                eventCardView = view.findViewById(R.id.event_item_card);
                eventCityStateTextView = view.findViewById(R.id.event_item_line2);
                eventHostTextView = view.findViewById(R.id.event_item_line3);
                eventNameTextView = view.findViewById(R.id.event_item_line1);
                eventImageThumb = view.findViewById(R.id.event_item_image);
            }
        }

        /**
         * ViewHolder for users
         */
        class ViewHolderUsers extends RecyclerView.ViewHolder {
            CardView userCardView;
            TextView userCityStateTextView;
            TextView userNameTextView;
            TextView userTagsTextView;
            ImageView userProfileImageThumb;


            ViewHolderUsers(View itemView) {
                super(itemView);
                userCardView = itemView.findViewById(R.id.search_result_card_users);
                userCityStateTextView = itemView.findViewById(R.id.user_location_search_result);
                userNameTextView = itemView.findViewById(R.id.username_search_result);
                userTagsTextView = itemView.findViewById(R.id.user_tag_search_result);
                userProfileImageThumb = itemView.findViewById(R.id.user_image_search_result);
            }
        }
    }
}
