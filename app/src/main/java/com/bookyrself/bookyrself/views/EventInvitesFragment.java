package com.bookyrself.bookyrself.views;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.presenters.EventInvitesFragmentPresenter;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventInvitesFragment extends Fragment implements BaseFragment, EventInvitesFragmentPresenter.EventInvitesViewListener {

    private static final int RC_SIGN_IN = 123;

    @BindView(R.id.event_invites_progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.event_invites_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar_event_invites_fragment)
    Toolbar toolbar;
    @BindView(R.id.event_invites_empty_state)
    View emptyState;
    @BindView(R.id.empty_state_text_header)
    TextView emptyStateTextHeader;
    @BindView(R.id.empty_state_image)
    ImageView emptyStateImage;
    @BindView(R.id.empty_state_text_subheader)
    TextView emptyStateTextSubHeader;
    @BindView(R.id.empty_state_button)
    Button emptyStateButton;


    private RecyclerView.LayoutManager layoutManager;
    private EventsAdapter adapter;
    private List<Map.Entry<String, EventDetail>> events;
    private EventInvitesFragmentPresenter presenter;


    public EventInvitesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getUid() != null) {
            presenter = new EventInvitesFragmentPresenter(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_invites, container, false);
        ButterKnife.bind(this, view);
        events = new ArrayList<>();
        adapter = new EventsAdapter();
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        toolbar.setTitle(R.string.title_event_invites);
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() != null) {
                // Signed in
                showLoadingState(true);
                showContent(false);
                hideEmptyState();
                presenter.loadPendingInvites(FirebaseAuth.getInstance().getUid());
            } else {
                // Signed Out
                showEmptyState(getString(R.string.event_invites_signed_out_header),
                        getString(R.string.empty_state_event_invites_signed_out_subheader),
                        getString(R.string.sign_in),
                        getActivity().getDrawable(R.drawable.ic_invitation));
            }
        });
        return view;
    }

    @Override
    public void eventPendingInvitationResponseReturned(String eventId, EventDetail event) {
        events.add(new AbstractMap.SimpleEntry<>(eventId, event));
        adapter.notifyDataSetChanged();
        showLoadingState(false);
        showContent(true);
    }

    @Override
    public void presentError(String message) {
        showLoadingState(false);
        showEmptyState(getString(R.string.error_header), message, "", getActivity().getDrawable(R.drawable.ic_error_empty_state));
    }


    @Override
    public void removeEventFromList(String eventId, EventDetail eventDetail) {

        Map.Entry<String, EventDetail> entry = new AbstractMap.SimpleEntry<>(eventId,eventDetail);
        events.remove(entry);
        adapter.notifyDataSetChanged();

        if (events.isEmpty()) {
            showEmptyStateForNoInvites();
        }

    }

    @Override
    public void showEmptyStateForNoInvites() {
        showEmptyState(getString(R.string.empty_state_event_invites_no_invites_header),
                getString(R.string.empty_state_event_invites_no_invites_subheader),
                "", getActivity().getDrawable(R.drawable.ic_no_events_black_24dp));
    }


    @Override
    public void showContent(boolean show) {
        if (show) {
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
        }

    }

    @Override
    public void showLoadingState(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showEmptyState(String header, String subHeader, String buttonText, Drawable image) {

        showContent(false);
        showLoadingState(false);

        emptyState.setVisibility(View.VISIBLE);
        emptyStateTextHeader.setVisibility(View.VISIBLE);
        emptyStateTextSubHeader.setVisibility(View.VISIBLE);
        emptyStateImage.setVisibility(View.VISIBLE);
        emptyStateTextHeader.setText(header);
        emptyStateTextSubHeader.setText(subHeader);
        emptyStateImage.setImageDrawable(image);
        if (!buttonText.equals("")) {
            emptyStateButton.setVisibility(View.VISIBLE);
            emptyStateButton.setText(buttonText);
            emptyStateButton.setOnClickListener(view -> {
                List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.EmailBuilder().build());
                // Authenticate
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false, true)
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN);
            });
        } else {
            emptyStateButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideEmptyState() {
        emptyStateButton.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        emptyStateImage.setVisibility(View.GONE);
        emptyStateTextHeader.setVisibility(View.GONE);
        emptyStateTextSubHeader.setVisibility(View.GONE);
    }

    /**
     * Adapter
     */
    class EventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        EventsAdapter() {

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = getLayoutInflater().inflate(R.layout.item_event_invite, parent, false);
            return new ViewHolderEvents(view);

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            final ViewHolderEvents viewHolderEvents = (ViewHolderEvents) holder;
            EventDetail eventDetail = events.get(position).getValue();
            viewHolderEvents.eventNameTextView.setText(eventDetail.getEventname());
            viewHolderEvents.eventLocationTextView.setText(eventDetail.getCitystate());
            DateFormat inputformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            try {
                Date date = inputformat.parse(eventDetail.getDate());
                DateFormat outputFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
                String formattedDate = outputFormat.format(date);
                viewHolderEvents.eventDateTextView.setText(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            viewHolderEvents.acceptButton.setOnClickListener(view -> {
                presenter.acceptEventInvite(FirebaseAuth.getInstance().getUid(),
                        events.get(position).getKey(),
                        events.get(position).getValue());
            });

            viewHolderEvents.denyButton.setOnClickListener(view ->
                    presenter.rejectEventInvite(FirebaseAuth.getInstance().getUid(),
                            events.get(position).getKey(),
                            events.get(position).getValue()));
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        class ViewHolderEvents extends RecyclerView.ViewHolder {
            @BindView(R.id.event_item_invite_card)
            CardView eventCard;
            @BindView(R.id.event_item_invite_line1)
            TextView eventNameTextView;
            @BindView(R.id.event_item_invite_line3)
            TextView eventDateTextView;
            @BindView(R.id.event_item_invite_line2)
            TextView eventLocationTextView;
            @BindView(R.id.event_item_invite_image)
            ImageView eventImageThumbnail;
            @BindView(R.id.event_item_invite_accept_button)
            Button acceptButton;
            @BindView(R.id.event_item_invite_deny_button)
            Button denyButton;

            public ViewHolderEvents(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
