package com.bookyrself.bookyrself.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.Host;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.MiniUser;
import com.bookyrself.bookyrself.presenters.EventDetailPresenter;
import com.bookyrself.bookyrself.utils.CircleTransform;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by benmedcalf on 11/22/17.
 */

public class EventDetailActivity extends AppCompatActivity implements EventDetailPresenter.EventDetailPresenterListener {

    @BindView(R.id.event_detail_date)
    TextView DateView;
    @BindView(R.id.item_event_detail_username)
    TextView HostUsernameTextview;
    @BindView(R.id.item_event_detail_citystate)
    TextView EventCityState;
    @BindView(R.id.item_event_detail_url)
    TextView HostUrlTextView;
    @BindView(R.id.event_detail_host_item)
    CardView HostcardView;
    @BindView(R.id.event_detail_toolbar)
    Toolbar Toolbar;
    @BindView(R.id.item_event_detail_userthumb)
    ImageView hostImageView;
    @BindView(R.id.event_detail_users_list)
    ListView usersListView;
    @BindView(R.id.event_detail_empty_state)
    View emptyState;
    @BindView(R.id.event_detail_linearlayout)
    View eventDetailContent;
    @BindView(R.id.empty_state_text_header)
    TextView emptyStateTextHeader;
    @BindView(R.id.empty_state_text_subheader)
    TextView emptyStateTextSubHeader;
    @BindView(R.id.empty_state_image)
    ImageView emptyStateImage;
    @BindView(R.id.empty_state_button)
    Button emptyStateButton;
    @BindView(R.id.event_detail_progressBar)
    ProgressBar progressBar;

    private List<MiniUser> miniUsers;
    private EventDetailPresenter presenter;
    private HashMap<String, String> idAndThumbUrlMap = new HashMap<>();
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        ButterKnife.bind(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        eventDetailContent.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        String eventId = getIntent().getStringExtra("eventId");
        presenter = new EventDetailPresenter(this);
        presenter.getEventDetailData(eventId);
    }

    @Override
    public void eventDataResponseReady(final EventDetail data, final List<MiniUser> miniUsersList) {
        showProgressbar(false);
        setSupportActionBar(Toolbar);
        Toolbar.setTitle(data.getEventname());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Host host = data.getHost();
        final String hostUsername = host.getUsername();
        String hostCityState = data.getCitystate();

        DateFormat inputformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date date = inputformat.parse(data.getDate());
            DateFormat outputFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
            String formattedDate = outputFormat.format(date);
            DateView.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        HostUsernameTextview.setText(hostUsername);
        final StorageReference profileImageReference = storageReference.child("images/" + host.getUserId());
        profileImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getApplicationContext())
                        .load(uri)
                        .resize(148, 148)
                        .centerCrop()
                        .transform(new CircleTransform())
                        .into(hostImageView);
            }
        }).addOnFailureListener(exception -> {
            // Handle any errors
            hostImageView.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_profile_black_24dp));
        });

        EventCityState.setText(hostCityState);
        miniUsers = miniUsersList;

        for (int i = 0; i < miniUsers.size(); i++) {
            String userId = miniUsers.get(i).getUserId();
            presenter.getUserThumbUrl(userId);
        }

        HostcardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HostcardView.getContext(), UserDetailActivity.class);
                intent.putExtra("userId", data.getHost().getUserId());
                startActivity(intent);
            }
        });
        eventDetailContent.setVisibility(View.VISIBLE);
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
    public void userThumbReady(String response, String id) {

        idAndThumbUrlMap.put(id, response);
        if (idAndThumbUrlMap.size() == miniUsers.size()) {
            UsersListAdapter adapter = new UsersListAdapter(this, miniUsers, storageReference);
            usersListView.setAdapter(adapter);
        }
    }

    @Override
    public void presentError(String message) {
        showProgressbar(false);
        Toolbar.setTitle("Event Detail Error");
        eventDetailContent.setVisibility(View.GONE);
        emptyStateButton.setVisibility(View.GONE);
        emptyStateImage.setImageDrawable(getDrawable(R.drawable.ic_error_empty_state));
        emptyStateTextHeader.setText("There was a problem loading the event");
        emptyStateTextSubHeader.setText(message);
        emptyState.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Adapter
     */
    private static class UsersListAdapter extends BaseAdapter {

        private final StorageReference mStorageReference;
        private Context mContext;
        private List<MiniUser> mMiniUsers;
        private LayoutInflater mInflater;


        private UsersListAdapter(Context context, List<MiniUser> miniUsers, StorageReference storageReference) {
            mMiniUsers = miniUsers;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContext = context;
            mStorageReference = storageReference;

        }

        @Override
        public int getCount() {
            return mMiniUsers.size();
        }

        @Override
        public Object getItem(int position) {
            return mMiniUsers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = mInflater.inflate(R.layout.item_event_detail_user, parent, false);
            final ImageView userThumb = rowView.findViewById(R.id.item_event_detail_userthumb);
            TextView userName = rowView.findViewById(R.id.item_event_detail_username);
            TextView cityState = rowView.findViewById(R.id.item_event_detail_citystate);
            TextView userUrl = rowView.findViewById(R.id.item_event_detail_url);
            TextView attendingStatusTextView = rowView.findViewById(R.id.item_event_detail_attending_textview);

            final MiniUser miniUser = (MiniUser) getItem(position);

            userName.setText(miniUser.getUsername());
            cityState.setText(miniUser.getCitystate());
            attendingStatusTextView.setText(miniUser.getAttendingStatus());
            userUrl.setClickable(true);
            userUrl.setMovementMethod(LinkMovementMethod.getInstance());
            String linkedText =
                    String.format("<a href=\"%s\">%s</a> ", ("http://" + miniUser.getUrl()), miniUser.getUrl());
            userUrl.setText(Html.fromHtml(linkedText));

            final StorageReference profileImageReference = mStorageReference.child("images/" + miniUser.getUserId());
            profileImageReference.getDownloadUrl().addOnSuccessListener(uri ->
                    Picasso.with(mContext)
                    .load(uri)
                    .resize(148, 148)
                    .centerCrop()
                    .transform(new CircleTransform())
                    .into(userThumb)).addOnFailureListener(exception ->
                        // Handle any errors
                        userThumb.setImageDrawable(mContext.getDrawable(R.drawable.ic_profile_black_24dp)));

            rowView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, UserDetailActivity.class);
                intent.putExtra("userId", miniUser.getUserId());
                mContext.startActivity(intent);
            });

            return rowView;
        }
    }
}
