package com.example.clown.activities;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.widget.SearchView;

import com.example.clown.adapter.SuggestedUserAdapter;
import com.example.clown.databinding.ActivityCommunityBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommunityActivity extends BaseActivity implements SuggestedUserAdapter.ISuggestedUserListener {
    private static final String TAG = CommunityActivity.class.getName();

    private ActivityCommunityBinding binding;
    private List<User> mSuggestedUsers;
    private SuggestedUserAdapter mSuggestedUserAdapter;
    private String mQuery;
    private boolean mIsEmptyQuery;

    private final long DELAY = 2000;
    private long LAST_TEXT_EDIT = 0;
    private Handler mHandler;

    private static final int QUERY_TYPE_PHONE_NUMBER = 0;
    private static final int QUERY_TYPE_USERNAME = 1;
    private static final int QUERY_TYPE_EMAIL = 2;

    private final Runnable input_finish_checker = new Runnable() {
        @Override
        public void run() {
            if (mIsEmptyQuery) return;

            if (System.currentTimeMillis() - LAST_TEXT_EDIT >= DELAY) {
                switch (getQueryType(mQuery)) {
                    case QUERY_TYPE_PHONE_NUMBER:
                        findUsersByPhoneNumber(mQuery); break;
                    case QUERY_TYPE_EMAIL:
                        findUserByEmail(mQuery); break;
                    case QUERY_TYPE_USERNAME:
                        findUserByUsername(mQuery); break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Initialize();
        
        setListeners();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(null, MainActivity.class, null);
    }

    private void Initialize() {
        Log.e(TAG, "Initialized!");

        binding = ActivityCommunityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mSuggestedUsers = new ArrayList<>();
        mHandler = new Handler();
        mIsEmptyQuery = true;

        mSuggestedUserAdapter = new SuggestedUserAdapter(this, mSuggestedUsers, this);
        mSuggestedUserAdapter.setCurrentUser(mCurrentUser);
        binding.communityRecyclerView.setAdapter(mSuggestedUserAdapter);

        isLoading(false);
    }

    private void setListeners() {
        binding.communitySearchView.setOnQueryTextListener(mOnUserQueryChanged);
    }

    private void isLoading(Boolean isLoading) {
        if (isLoading) binding.progressBar.setVisibility(View.VISIBLE);
        else binding.progressBar.setVisibility(View.INVISIBLE);
    }

    private int getQueryType(String query) {
        if (Patterns.PHONE.matcher(query).matches()) return QUERY_TYPE_PHONE_NUMBER;

        if (Patterns.EMAIL_ADDRESS.matcher(query).matches()) return QUERY_TYPE_EMAIL;

        return QUERY_TYPE_USERNAME;
    }

    private final SearchView.OnQueryTextListener mOnUserQueryChanged = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (query.isEmpty()) {
                resetResults();

                isLoading(false);

                mIsEmptyQuery = true;

                return true;
            }

            isLoading(true);

            mIsEmptyQuery = false;

            mQuery = query;

            LAST_TEXT_EDIT = System.currentTimeMillis();

            mHandler.postDelayed(input_finish_checker, DELAY);

            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (newText.isEmpty()) {
                resetResults();

                isLoading(false);

                mIsEmptyQuery = true;

                return true;
            }

            isLoading(true);

            mIsEmptyQuery = false;

            mQuery = newText;

            LAST_TEXT_EDIT = System.currentTimeMillis();

            mHandler.postDelayed(input_finish_checker, DELAY);

            return true;
        }
    };

    private void resetResults() {
        int oldSize = mSuggestedUsers.size();
        mSuggestedUsers.clear();
        mSuggestedUserAdapter.notifyItemRangeRemoved(oldSize, oldSize);
    }

    private void findUsersByPhoneNumber(String queryValue) {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_PHONE_NUMBER, queryValue)
                .get()
                .addOnCompleteListener(mOnUserQueryCompleted);
    }

    private void findUserByEmail(String queryValue) {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereGreaterThanOrEqualTo(Constants.KEY_EMAIL, queryValue)
                .get()
                .addOnCompleteListener(mOnUserQueryCompleted);
    }

    private void findUserByUsername(String queryValue) {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereGreaterThanOrEqualTo(Constants.KEY_USERNAME, queryValue)
                .get()
                .addOnCompleteListener(mOnUserQueryCompleted);
    }

    protected final OnCompleteListener<QuerySnapshot> mOnUserQueryCompleted = task -> {
        resetResults();

        if (task.isSuccessful() && task.getResult() != null) {
            for (DocumentSnapshot docSnap : task.getResult().getDocuments()) {
                User result = docSnap.toObject(User.class);

                if (result != null && result.getID().equals(mCurrentUser.getID())) continue;

                mSuggestedUsers.add(result);
                mSuggestedUserAdapter.notifyItemInserted(mSuggestedUsers.size() - 1);
            }
        }

        isLoading(false);
    };

    @Override
    public void onSentFriendRequest(User suggestedUser) {
        String senderID = mCurrentUser.getID();
        String receiverID = suggestedUser.getID();

        suggestedUser.getReceivedRequests().add(senderID);
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(suggestedUser.getID())
                .update(Constants.KEY_RECEIVED_REQUESTS, suggestedUser.getReceivedRequests());

        User temp = new User();
        temp.Clone(mCurrentUser);
        temp.getSentRequests().add(receiverID);
        mPreferenceManager.putUser(temp);
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mCurrentUser.getID())
                .update(Constants.KEY_SENT_REQUESTS, temp.getSentRequests());
    }
}