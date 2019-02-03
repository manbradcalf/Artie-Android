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
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.presenters.EventInvitesFragmentPresenter;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventInvitesFragment extends Fragment implements BaseFragment, EventInvitesFragmentPresenter.EventInvitesUserDetailPresenterListener {

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


    private HashMap<EventDetail, String> eventDetailEventIdHashMap;
    private RecyclerView.LayoutManager layoutManager;
    private EventsAdapter adapter;
    private List<EventDetail> eventDetailsList;
    private EventInvitesFragmentPresenter presenter;


    public EventInvitesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_invites, container, false);
        ButterKnife.bind(this, view);
        eventDetailEventIdHashMap = new HashMap<>();
        eventDetailsList = new ArrayList<>();
        adapter = new EventsAdapter();
        presenter = new EventInvitesFragmentPresenter(this);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        toolbar.setTitle(R.string.title_event_invites);
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    // Signed in
                    presenter.getEventInvites(FirebaseAuth.getInstance().getUid());
                    showContent(false);
                    hideEmptyState();
                    showLoadingState(true);
                } else {
                    // Signed Out
                    showEmptyState(getString(R.string.event_invites_signed_out_header),
                            getString(R.string.empty_state_event_invites_signed_out_subheader),
                            getString(R.string.sign_in),
                            getActivity().getDrawable(R.drawable.ic_invitation));
                }
            }
        });
        return view;
    }

    @Override
    public void eventPendingInvitationResponseReturned(EventDetail event, String eventId) {
        showLoadingState(false);
        showContent(true);
        eventDetailsList.add(event);
        eventDetailEventIdHashMap.put(event, eventId);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void presentError(String message) {
        showLoadingState(false);
        showEmptyState(getString(R.string.error_header), message, "", getActivity().getDrawable(R.drawable.ic_error_empty_state));
    }

    @Override
    public void eventInviteAccepted(boolean accepted, String eventId) {
        if (recyclerView.getVisibility() == View.GONE) {
            showContent(true);
        }

        for (int i = 0; i < eventDetailsList.size(); i++) {
            if (eventId.equals(eventDetailEventIdHashMap.get(eventDetailsList.get(i)))) {
                eventDetailsList.remove(eventDetailsList.get(i));
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void noInvitesReturnedForUser() {
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
            emptyStateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
                }
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
            EventDetail event = eventDetailsList.get(position);
            viewHolderEvents.eventNameTextView.setText(event.getEventname());
            viewHolderEvents.eventLocationTextView.setText(event.getCitystate());
            DateFormat inputformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            try {
                Date date = inputformat.parse(event.getDate());
                DateFormat outputFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
                String formattedDate = outputFormat.format(date);
                viewHolderEvents.eventDateTextView.setText(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            viewHolderEvents.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: Holy shit this is ugly
                    // Get the event detail from event details list
                    // Then use that event detail to get the id from the eventdetail,id map
                    // THen pass that iD to the presenter method
                    // UGH
                    presenter.acceptEventInvite(FirebaseAuth.getInstance().getUid(), eventDetailEventIdHashMap.get(eventDetailsList.get(position)));
                }
            });

            viewHolderEvents.denyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.rejectInvite(FirebaseAuth.getInstance().getUid(), eventDetailEventIdHashMap.get(eventDetailsList.get(position)));
                }
            });
        }

        @Override
        public int getItemCount() {
            return eventDetailsList.size();
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
