package com.bookyrself.bookyrself.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.Host;
import com.bookyrself.bookyrself.models.SerializedModels.EventDetail.MiniUser;
import com.bookyrself.bookyrself.presenters.EventDetailPresenter;
import com.bookyrself.bookyrself.utils.CircleTransform;
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
    TextView HostCityState;
    @BindView(R.id.item_event_detail_url)
    TextView HostUrlTextView;
    @BindView(R.id.event_detail_host_item)
    CardView HostcardView;
    @BindView(R.id.toolbar_event_detail)
    Toolbar Toolbar;
    @BindView(R.id.item_event_detail_userthumb)
    ImageView HostImageView;
    @BindView(R.id.event_detail_users_list)
    ListView usersListView;

    private List<MiniUser> miniUsers;
    private EventDetailPresenter presenter;
    private HashMap<String, String> idAndThumbUrlMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String eventId = intent.getStringExtra("eventId");
        presenter = new EventDetailPresenter(this);
        presenter.getEventDetailData(eventId);
    }

    @Override
    public void eventDataResponseReady(final EventDetail data) {


        setSupportActionBar(Toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.event_detail_toolbar);

        String date = data.getDate();
        Host host = data.getHost().get(0);
        String hostUsername = host.getUsername();
        String hostCityState = host.getCitystate();
        String hostURL = host.getUrl();
        String linkedText = String.format("<a href=\"%s\">%s</a>", ("http://" + hostURL), hostURL);

        HostUrlTextView.setClickable(true);
        HostUrlTextView.setText(Html.fromHtml(linkedText));
        HostUrlTextView.setMovementMethod(LinkMovementMethod.getInstance());

        DateView.setText("Date");
        DateFormat inputformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date d;
        try {
            d = inputformat.parse(date);
            DateFormat outputFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
            String formattedDate = outputFormat.format(d);
            DateView.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        HostUsernameTextview.setText(hostUsername);

        HostCityState.setText(hostCityState);

        Picasso.with(getApplicationContext())
                .load(hostURL)
                .placeholder(R.drawable.round)
                .error(R.drawable.round)
                .transform(new CircleTransform())
                .into(HostImageView);

        miniUsers = data.getUsers();

        for (int i = 0; i < miniUsers.size(); i++) {
            String userId = miniUsers.get(i).getUserId();
            presenter.getUserThumbUrl(userId);
        }


        HostcardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HostcardView.getContext(), UserDetailActivity.class);
                intent.putExtra("userId", data.getHost().get(0).getUserId());
                startActivity(intent);
            }
        });
        Toolbar.setTitle(data.getEventname());
    }

    @Override
    public void showProgressbar(Boolean bool) {

    }

    //TODO: Seems kinda hacky that I'm using this method to check map sizes and then attach adapter
    @Override
    public void userThumbReady(String response, String id) {

        idAndThumbUrlMap.put(id, response);
        if (idAndThumbUrlMap.size() == miniUsers.size()) {
            UsersListAdapter adapter = new UsersListAdapter(this, miniUsers, idAndThumbUrlMap);
            usersListView.setAdapter(adapter);
        }

    }

    @Override
    public void present_error() {
        Toast.makeText(this, "response was null because that id wasn't legit dumbass", Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private static class UsersListAdapter extends BaseAdapter {

        private HashMap mMap;
        private Context mContext;
        private List<MiniUser> mMiniUsers;
        private LayoutInflater mInflater;


        private UsersListAdapter(Context context, List<MiniUser> miniUsers, HashMap idAndThumbUrlMap) {
            mMiniUsers = miniUsers;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContext = context;
            mMap = idAndThumbUrlMap;
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

            MiniUser miniUser = (MiniUser) getItem(position);

            userName.setText(miniUser.getUsername());
            cityState.setText(miniUser.getCitystate());
            userUrl.setClickable(true);
            userUrl.setMovementMethod(LinkMovementMethod.getInstance());
            userUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(this.toString(), "Testing the text click");
                }
            });
            String linkedText =
                    String.format("<a href=\"%s\">%s</a> ", ("http://" + miniUser.getUrl()), miniUser.getUrl());
            userUrl.setText(Html.fromHtml(linkedText));

            String userId = miniUser.getUserId();

            Picasso.with(mContext)
                    .load(String.valueOf(mMap.get(userId)))
                    .placeholder(R.drawable.round)
                    .error(R.drawable.round)
                    .transform(new CircleTransform())
                    .into(userThumb);

            userThumb.setTag(R.id.item_event_detail_userthumb, miniUser.getUserId());

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, UserDetailActivity.class);
                    intent.putExtra("userId", (String) userThumb.getTag(R.id.item_event_detail_userthumb));
                    mContext.startActivity(intent);
                }
            });

            return rowView;
        }
    }
}
