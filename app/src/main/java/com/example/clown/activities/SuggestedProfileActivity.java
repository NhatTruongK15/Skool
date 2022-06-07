package com.example.clown.activities;

import android.os.Bundle;
import android.util.Log;

import com.example.clown.databinding.ActivitySuggestedProfileBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class SuggestedProfileActivity extends BaseActivity {
    public static final String TAG = SuggestedProfileActivity.class.getName();

    private ActivitySuggestedProfileBinding binding;
    private User mSuggestedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();

        loadSuggestedUserDetails();
    }

    private void Init() {
        binding = ActivitySuggestedProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mSuggestedUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
    }

    private void loadSuggestedUserDetails() {
        binding.rivProfileAvatar.setImageBitmap(mSuggestedUser.getImage());
        binding.tvUsername.setText(mSuggestedUser.getName());
        binding.tvProfileEmail.setText(mSuggestedUser.getEmail());

        binding.optAddFriend.setOnClickListener(v -> addFriend());
    }

    private void addFriend() {
        Log.e(TAG, "Friend request's sent!");

        updateTargetUserPendingRequests();

        updateCurrentUserSentRequests();

        showToast(Constants.TOAST_FRIEND_REQUEST_SENT);
    }

    private void updateTargetUserPendingRequests() {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mSuggestedUser.getUserID())
                .update("pendingRequests", FieldValue.arrayUnion(mCurrentUser.getUserID()));
    }

    private void updateCurrentUserSentRequests() {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mCurrentUser.getUserID())
                .update("sentRequests", FieldValue.arrayUnion(mSuggestedUser.getUserID()));
    }
}