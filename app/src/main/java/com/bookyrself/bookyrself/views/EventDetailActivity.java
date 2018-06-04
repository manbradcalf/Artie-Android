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
import com.bookyrself.bookyrself.models.EventDetailResponse.EventDetailResponse;
import com.bookyrself.bookyrself.models.EventDetailResponse.Host;
import com.bookyrself.bookyrself.models.EventDetailResponse.User;
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

/**
 * Created by benmedcalf on 11/22/17.
 */

public class EventDetailActivity extends AppCompatActivity implements EventDetailPresenter.EventDetailPresenterListener {

    private TextView DateView;
    private TextView Host;
    private TextView HostCityState;
    private TextView HostUrlTextView;
    private CardView HostcardView;
    private Toolbar Toolbar;
    private ImageView HostImageView;
    private ListView usersListView;
    private List<User> users;
    private EventDetailPresenter presenter;
    private HashMap<String, String> idAndThumbUrlMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        presenter = new EventDetailPresenter(this);
        Intent intent = getIntent();
        String eventId = intent.getStringExtra("eventId");
        presenter.getEventDetailData(eventId);
        Toolbar = findViewById(R.id.toolbar_event_detail);
    }

    @Override
    public void eventDataResponseReady(final EventDetailResponse data) {


        setSupportActionBar(Toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.event_detail_toolbar);

        String date = data.getDate();
        Host host = data.getHost().get(0);
        String hostUsername = host.getUsername();
        String hostCityState = host.getCitystate();
        String hostURL = host.getUrl();
        String linkedText = String.format("<a href=\"%s\">%s</a>", ("http://" + hostURL), hostURL);

        HostUrlTextView = findViewById(R.id.item_event_detail_url);
        HostUrlTextView.setClickable(true);
        HostUrlTextView.setText(Html.fromHtml(linkedText));
        HostUrlTextView.setMovementMethod(LinkMovementMethod.getInstance());

        DateView = findViewById(R.id.event_detail_date);
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

        Host = findViewById(R.id.item_event_detail_username);
        Host.setText(hostUsername);
        HostCityState = findViewById(R.id.item_event_detail_citystate);
        HostCityState.setText(hostCityState);
        HostImageView = findViewById(R.id.item_event_detail_userthumb);
        Picasso.with(getApplicationContext())
                .load(hostURL)
                .placeholder(R.drawable.round)
                .error(R.drawable.round)
                .transform(new CircleTransform())
                .into(HostImageView);

        usersListView = findViewById(R.id.event_detail_users_list);
        users = data.getUsers();

        for (int i = 0; i < users.size(); i++) {
            String userId = users.get(i).getUserId();
            presenter.getUserThumbUrl(userId);
        }

        HostcardView = findViewById(R.id.event_detail_host_item);
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
        if (idAndThumbUrlMap.size() == users.size()) {
            UsersListAdapter adapter = new UsersListAdapter(this, users, idAndThumbUrlMap);
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
        private List<User> mUsers;
        private LayoutInflater mInflater;


        private UsersListAdapter(Context context, List<User> users, HashMap idAndThumbUrlMap) {
            mUsers = users;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContext = context;
            mMap = idAndThumbUrlMap;
        }

        @Override
        public int getCount() {
            return mUsers.size();
        }

        @Override
        public Object getItem(int position) {
            return mUsers.get(position);
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

            User user = (User) getItem(position);

            userName.setText(user.getUsername());
            cityState.setText(user.getCitystate());
            userUrl.setClickable(true);
            userUrl.setMovementMethod(LinkMovementMethod.getInstance());
            userUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(this.toString(), "Testing the text click");
                }
            });
            String linkedText =
                    String.format("<a href=\"%s\">%s</a> ", ("http://" + user.getUrl()), user.getUrl());
            userUrl.setText(Html.fromHtml(linkedText));

            String userId = user.getUserId();

            Picasso.with(mContext)
                    .load(String.valueOf(mMap.get(userId)))
                    .placeholder(R.drawable.round)
                    .error(R.drawable.round)
                    .transform(new CircleTransform())
                    .into(userThumb);

            userThumb.setTag(R.id.item_event_detail_userthumb, user.getUserId());

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
