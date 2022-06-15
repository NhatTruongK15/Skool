package com.example.clown.activities;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.clown.R;
import com.example.clown.databinding.ActivitySuggestedProfileBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void Init() {
        binding = ActivitySuggestedProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.tbProfile);
        Objects.requireNonNull(getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);

        mSuggestedUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
    }

    private void loadSuggestedUserDetails() {
        binding.rivProfileAvatar.setImageBitmap(mSuggestedUser.getBitmapAvatar());
        binding.tvUsername.setText(mSuggestedUser.getUsername());
        binding.tvProfileEmail.setText(mSuggestedUser.getEmail());
        binding.optAddFriend.setOnClickListener(v -> addFriend());
        binding.tvProfileBio.setText(mSuggestedUser.getBio());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.PATTERN_DATE_ONLY_FORMATTER, Locale.CHINA);
        binding.tvProfileDateOfBirth.setText(simpleDateFormat.format(mSuggestedUser.getDateOfBirth()));
        binding.tvFirstName.setText(mSuggestedUser.getFirstName());
        binding.tvLastName.setText(mSuggestedUser.getLastName());
        binding.tvProfileGender.setText(mSuggestedUser.getGender());

        if (mCurrentUser.getSentRequests().contains(mSuggestedUser.getID()))
            disableAddFriendOption();
    }

    private void addFriend() {
        Log.e(TAG, "Friend request's sent!");

        updateTargetUserPendingRequests();

        updateCurrentUserSentRequests();

        disableAddFriendOption();

        showToast(Constants.TOAST_FRIEND_REQUEST_SENT);
    }

    private void disableAddFriendOption() {
        int disabledColor = ContextCompat.getColor(binding.tvAddFriend.getContext(), R.color.primary);

        binding.optAddFriend.setEnabled(false);

        binding.tvAddFriend.setText(R.string.request_sent);
        binding.tvAddFriend.setTextColor(disabledColor);
        binding.tvAddFriend.getCompoundDrawablesRelative()[0]
                .setColorFilter(disabledColor, PorterDuff.Mode.SRC_IN);
    }

    private void updateTargetUserPendingRequests() {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mSuggestedUser.getID())
                .update(Constants.KEY_RECEIVED_REQUESTS, FieldValue.arrayUnion(mCurrentUser.getID()));
    }

    private void updateCurrentUserSentRequests() {
        User temp = new User();
        temp.Clone(mCurrentUser);
        temp.getSentRequests().add(mSuggestedUser.getID());
        mPreferenceManager.putUser(temp);
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mCurrentUser.getID())
                .update(Constants.KEY_SENT_REQUESTS, FieldValue.arrayUnion(mSuggestedUser.getID()));
    }
}