package com.example.clown.activities;

import android.os.Bundle;

import com.example.clown.databinding.ActivityPendingProfileBinding;
import com.example.clown.models.Conversation;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PendingProfileActivity extends BaseActivity {
    private ActivityPendingProfileBinding binding;
    private User mRequester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Initialize();

        setListeners();
    }

    private void Initialize() {
        binding = ActivityPendingProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mRequester = new User();
        mRequester.Clone((User)  getIntent().getSerializableExtra(Constants.KEY_REMOTE_USER_DATA));
    }

    private void setListeners() {
        binding.rivProfileAvatar.setImageBitmap(mRequester.getBitmapAvatar());
        binding.tvUsername.setText(mRequester.getUsername());
        binding.tvProfilePhoneNumber.setText(mRequester.getPhoneNumber());
        binding.tvProfileEmail.setText(mRequester.getEmail());
        binding.tvProfileGender.setText(mRequester.getGender());
        binding.tvFirstName.setText(mRequester.getFirstName());
        binding.tvLastName.setText(mRequester.getLastName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.PATTERN_DATE_ONLY_FORMATTER, Locale.CHINA);
        binding.tvProfileDateOfBirth.setText(simpleDateFormat.format(mRequester.getDateOfBirth()));
        binding.tvProfileBio.setText(mRequester.getBio());
        binding.tvAccept.setOnClickListener(v -> requestAccept());
        binding.tvDecline.setOnClickListener(v -> requestDecline());
    }

    private void requestAccept() {
        // Remove self ID from requester sent request list
        mRequester.getSentRequests().remove(mCurrentUser.getID());
        mRequester.getFriends().add(mCurrentUser.getID());

        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mRequester.getID())
                .update(Constants.KEY_SENT_REQUESTS, mRequester.getSentRequests(),
                        Constants.KEY_FRIEND_LIST, mRequester.getFriends());

        // Update remote self received request list
        mCurrentUser.getReceivedRequests().remove(mRequester.getID());
        mCurrentUser.getFriends().add(mRequester.getID());

        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mCurrentUser.getID())
                .update(Constants.KEY_RECEIVED_REQUESTS, mCurrentUser.getReceivedRequests(),
                        Constants.KEY_FRIEND_LIST, mCurrentUser.getFriends());

        // Create new conversation
        DocumentReference docRef = FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .document();

        Conversation newConversation = new Conversation();

        newConversation.setId(docRef.getId());

        newConversation.getMembers().add(mCurrentUser.getID());
        newConversation.getMembers().add(mRequester.getID());

        newConversation.getAdmins().add(mCurrentUser.getID());
        newConversation.getAdmins().add(mRequester.getID());

        newConversation.setSenderId(mCurrentUser.getID());
        newConversation.setSenderName(mCurrentUser.getUsername());
        newConversation.setSenderAvatar(mCurrentUser.getAvatar());

        newConversation.setReceiverId(mRequester.getID());
        newConversation.setReceiverName(mRequester.getUsername());
        newConversation.setReceiverAvatar(mRequester.getAvatar());

        docRef.set(newConversation);

        // Update local self received request list
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        preferenceManager.putUser(mCurrentUser);

        finish();
    }

    private void requestDecline() {
        // Remove self ID from requester sent request list
        mRequester.getSentRequests().remove(mCurrentUser.getID());
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mRequester.getID())
                .update(Constants.KEY_SENT_REQUESTS, mRequester.getSentRequests());

        // Update local self received request list
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        User currentUser = preferenceManager.getUser();
        currentUser.getReceivedRequests().remove(mRequester.getID());
        preferenceManager.putUser(currentUser);

        // Update remote self received request list
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mCurrentUser.getID())
                .update(Constants.KEY_RECEIVED_REQUESTS, currentUser.getReceivedRequests());

        // Notify User
        showToast(Constants.TOAST_FRIEND_REQUEST_DECLINED);

        finish();
    }
}