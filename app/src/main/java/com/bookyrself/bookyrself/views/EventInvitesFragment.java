package com.bookyrself.bookyrself.views;

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
import android.widget.TextView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.presenters.EventInvitesFragmentPresenter;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventInvitesFragment extends Fragment implements EventInvitesFragmentPresenter.EventInvitesUserDetailPresenterListener {

    @BindView(R.id.event_invites_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar_event_invites_fragment)
    Toolbar toolbar;

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
        toolbar.setTitle(R.string.event_invites_toolbar);
        eventDetailEventIdHashMap = new HashMap<>();
        eventDetailsList = new ArrayList<>();
        adapter = new EventsAdapter();
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        presenter = new EventInvitesFragmentPresenter(this);
        presenter.getEventInvites(FirebaseAuth.getInstance().getUid());

        return view;
    }

    @Override
    public void eventsPendingInvitationResponseReturned(EventDetail event, String eventId) {
        eventDetailsList.add(event);
        eventDetailEventIdHashMap.put(event, eventId);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void presentError(String message) {

    }

    @Override
    public void eventInviteAccepted(String eventId) {

        //TODO: This is fucked
        for(int i = 0; i < eventDetailsList.size(); i++){
            if (eventId.equals(eventDetailEventIdHashMap.get(eventDetailsList.get(i)))) {
                eventDetailsList.remove(eventDetailsList.get(i));
                adapter.notifyDataSetChanged();
            }
        }
    }

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
