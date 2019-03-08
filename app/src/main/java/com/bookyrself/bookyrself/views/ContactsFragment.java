package com.bookyrself.bookyrself.views;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.data.ResponseModels.User.User;
import com.bookyrself.bookyrself.presenters.ContactsFragmentPresenter;
import com.bookyrself.bookyrself.utils.CircleTransform;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;

import static android.app.Activity.RESULT_OK;

public class ContactsFragment extends Fragment implements BaseFragment, ContactsFragmentPresenter.ContactsPresenterListener {

    private static final int RC_SIGN_IN = 123;
    @BindView(R.id.contacts_recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar_contacts_fragment)
    Toolbar toolbar;
    @BindView(R.id.contacts_fragment_progressbar)
    ProgressBar progressbar;
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

    private ContactsAdapter adapter;
    private ContactsFragmentPresenter presenter;
    private RecyclerView.LayoutManager layoutManager;
    private List<User> contacts;
    private Map<User, String> contactsMap;
    private StorageReference storageReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, view);
        toolbar.setTitle(R.string.contacts_toolbar);
        contactsMap = new HashMap<>();
        contacts = new ArrayList<>();
        presenter = new ContactsFragmentPresenter(this);
        adapter = new ContactsAdapter();
        recyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        storageReference = FirebaseStorage.getInstance().getReference();
        showLoadingState(true);

        return view;
    }

    @Override
    public void showLoadingState(boolean show) {
        if (show && progressbar.getVisibility() == View.GONE) {
            progressbar.setVisibility(View.VISIBLE);
            hideEmptyState();
        } else {
            progressbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        contacts.clear();
        presenter.unsubscribe();
    }

    @Override
    public void showEmptyState(String header, String subHeader, String buttonText, Drawable image) {
        showContent(false);
        showLoadingState(false);
        emptyState.setVisibility(View.VISIBLE);
        emptyStateImage.setVisibility(View.VISIBLE);
        emptyStateTextHeader.setVisibility(View.VISIBLE);
        emptyStateTextSubHeader.setVisibility(View.VISIBLE);

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

    @Override
    public void showContent(boolean show) {
        if (show) {
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_SIGN_IN:
                    hideEmptyState();
                    showLoadingState(true);
                    presenter.subscribe();
            }
        }
    }

    @Override
    public void noContactsReturned() {
        showEmptyState(getString(R.string.contacts_empty_state_no_content_header),
                getString(R.string.contacts_empty_state_no_content_subheader),
                "",
                getActivity().getDrawable(R.drawable.ic_person_add_black_24dp));
    }

    @Override
    public void contactReturned(String id, User user) {

        showLoadingState(false);
        hideEmptyState();
        showContent(true);
        contacts.add(user);
        contactsMap.put(user, id);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void presentError(String error) {
        showEmptyState(getString(R.string.error_header),
                error,
                "",
                getActivity().getDrawable(R.drawable.ic_error_empty_state));
    }

    @Override
    public void showSignedOutEmptyState() {
        showEmptyState(getString(R.string.contacts_empty_state_signed_out_header),
                getString(R.string.contacts_empty_state_no_content_subheader),
                getString(R.string.sign_in),
                getActivity().getDrawable(R.drawable.ic_person_add_black_24dp));
    }


    /**
     * RecyclerView Adapter
     */
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
            final ViewHolderContacts viewHolderContacts = (ViewHolderContacts) holder;
            if (contacts.get(position).getTags() != null) {
                StringBuilder listString = new StringBuilder();
                for (String s : contacts.get(position).getTags()) {
                    listString.append(s + ", ");
                }
                viewHolderContacts.userTagsTextView.setText(listString.toString().replaceAll(", $", ""));
            }

            final StorageReference profileImageReference = storageReference.child("/images/users/" + contactsMap.get(contacts.get(position)));
            profileImageReference
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> Picasso.with(getActivity())
                            .load(uri)
                            .placeholder(R.drawable.round)
                            .error(R.drawable.round)
                            .transform(new CircleTransform())
                            .resize(100, 100)
                            .into(viewHolderContacts.userProfileImageThumb)).addOnFailureListener(e -> {
                                // Handle any errors
                                Log.e("ContactsFragment: ", "image not dowloaded");
                                viewHolderContacts.userProfileImageThumb.setImageDrawable(getContext().getDrawable(R.drawable.ic_profile_black_24dp));
                            });

            viewHolderContacts.userNameTextView.setText(contacts.get(position).getUsername());
            viewHolderContacts.userCityStateTextView.setText(contacts.get(position).getCitystate());
            viewHolderContacts.userCardView.setOnClickListener(view -> {
                Intent intent = new Intent(getActivity(), UserDetailActivity.class);
                intent.putExtra("userId", contactsMap.get(contacts.get(position)));
                startActivity(intent);
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
