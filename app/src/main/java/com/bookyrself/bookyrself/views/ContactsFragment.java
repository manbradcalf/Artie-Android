package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.models.SearchResponseUsers._source;
import com.bookyrself.bookyrself.presenters.ContactsPresenter;
import com.bookyrself.bookyrself.utils.CircleTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactsFragment extends Fragment implements ContactsPresenter.ContactsPresenterListener {

    private static final int RC_SIGN_IN = 123;
    @BindView(R.id.contacts_recyclerview)
    RecyclerView recyclerView;

    @BindView(R.id.toolbar_contacts_fragment)
    Toolbar toolbar;

    @BindView(R.id.contacts_empty_state)
    LinearLayout emptyState;
    @BindView(R.id.empty_state_text_header)
    TextView emptyStateTextHeader;
    @BindView(R.id.empty_state_image)
    ImageView emptyStateImage;
    @BindView(R.id.empty_state_text_subheader)
    TextView emptyStateTextSubHeader;
    @BindView(R.id.empty_state_button)
    Button emptyStateButton;

    ContactsAdapter adapter;
    ContactsPresenter presenter;
    RecyclerView.LayoutManager layoutManager;
    List<String> contactIds;
    List<_source> contacts;
    Map<_source, String> contactsMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, view);
        toolbar.setTitle(R.string.contacts_toolbar);
        contactsMap = new HashMap<>();
        contacts = new ArrayList<>();
        presenter = new ContactsPresenter(this);
        adapter = new ContactsAdapter();
        recyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        emptyState.setVisibility(View.GONE);
        presenter.getContactIds(FirebaseAuth.getInstance().getUid());
    }

    @Override
    public void presentError() {

    }

    @Override
    public void loadingState() {

    }

    // First I get a list of contactIds via the presenter,
    // upon receiving those ids, I then call getUsers to iterate through
    // each id to get the relevant user data.
    @Override
    public void contactsReturned(List<String> ids) {
        contactIds = ids;
        presenter.getUsers(ids);
    }

    //TODO: Do what?
    //  When I've iterated through all the ids, I notify the adapter of a change
    // Also I am adding to a map of userId with userInfo so I can start a UserDetail activity with the id
    // since the id isn't on the _source object
    @Override
    public void userReturned(String id, _source user) {
        contacts.add(user);
        contactsMap.put(user, id);
        if (contacts.size() == contactIds.size()) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void noUsersReturned() {
        emptyState.setVisibility(View.VISIBLE);
        emptyStateTextHeader.setText(R.string.contacts_empty_state_header);
        emptyStateTextSubHeader.setText(R.string.contacts_empty_state_subheader);
        emptyStateButton.setText(R.string.contacts_empty_state_button_text);
        emptyStateImage.setImageDrawable(getContext().getDrawable(R.drawable.ic_person_add_black_24dp));

    }

    class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ContactsAdapter() {

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_user_search_result, parent, false);
            return new ViewHolderContacts(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            ViewHolderContacts viewHolderContacts = (ViewHolderContacts) holder;
            if (contacts.get(position).getTags() != null) {
                StringBuilder listString = new StringBuilder();
                for (String s : contacts.get(position).getTags()) {
                    listString.append(s + ", ");
                }
                viewHolderContacts.userTagsTextView.setText(listString.toString());

            }
            viewHolderContacts.userNameTextView.setText(contacts.get(position).getUsername());
            viewHolderContacts.userCityStateTextView.setText(contacts.get(position).getCitystate());
            Picasso.with(getActivity())
                    .load("https://img.etsystatic.com/il/9a1dbd/1358791570/il_570xN.1358791570_ib1c.jpg")
                    .placeholder(R.drawable.round)
                    .error(R.drawable.round)
                    .transform(new CircleTransform())
                    .resize(100, 100)
                    .into(viewHolderContacts.userProfileImageThumb);
            viewHolderContacts.userCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), UserDetailActivity.class);
                    intent.putExtra("userId", contactsMap.get(contacts.get(position)));
                    startActivity(intent);
                }
            });
        }


        @Override
        public int getItemCount() {
            return contacts.size();
        }

        class ViewHolderContacts extends RecyclerView.ViewHolder {

            @BindView(R.id.search_result_card_users)
            CardView userCardView;
            @BindView(R.id.user_location_search_result)
            TextView userCityStateTextView;
            @BindView(R.id.username_search_result)
            TextView userNameTextView;
            @BindView(R.id.user_tag_search_result)
            TextView userTagsTextView;
            @BindView(R.id.user_image_search_result)
            ImageView userProfileImageThumb;

            ViewHolderContacts(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
