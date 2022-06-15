package com.example.clown.activities;

import android.os.Bundle;

import com.example.clown.databinding.ActivityFriendProfileBinding;
import com.example.clown.models.Conversation;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FriendProfileActivity extends BaseActivity {
    private ActivityFriendProfileBinding binding;
    private User mSelectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();
        
        loadUserProfile();
        
        setListeners();
    }

    private void Init() {
        binding = ActivityFriendProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mSelectedUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
    }

    private void loadUserProfile() {
        binding.rivProfileAvatar.setImageBitmap(mSelectedUser.getBitmapAvatar());
        binding.tvUsername.setText(mSelectedUser.getUsername());
        binding.tvProfilePhoneNumber.setText(mSelectedUser.getPhoneNumber());
        binding.tvProfileEmail.setText(mSelectedUser.getEmail());
        binding.tvProfileGender.setText(mSelectedUser.getGender());
        binding.tvFirstName.setText(mSelectedUser.getFirstName());
        binding.tvLastName.setText(mSelectedUser.getLastName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.PATTERN_DATE_ONLY_FORMATTER, Locale.CHINA);
        binding.tvProfileDateOfBirth.setText(simpleDateFormat.format(mSelectedUser.getDateOfBirth()));
        binding.tvProfileBio.setText(mSelectedUser.getBio());
    }

    private void setListeners() {
        binding.optUnfriend.setOnClickListener(v -> removeFriend());
    }

    private void removeFriend() {
        mSelectedUser.getFriends().remove(mCurrentUser.getID());
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mSelectedUser.getID())
                .update(Constants.KEY_FRIEND_LIST, mSelectedUser.getFriends());

        User temp = new User();
        temp.Clone(mCurrentUser);
        temp.getFriends().remove(mSelectedUser.getID());
        mPreferenceManager.putUser(temp);
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mCurrentUser.getID())
                .update(Constants.KEY_FRIEND_LIST,  temp.getFriends());


        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereArrayContains(Constants.KEY_CONVERSATION_MEMBERS, mCurrentUser.getID())
                .get()
                .addOnCompleteListener(mConversationResults);
    }

    private final OnCompleteListener<QuerySnapshot> mConversationResults = task -> {
        if (task.isSuccessful() && task.getResult() != null) {
            for (DocumentSnapshot docSnap : task.getResult().getDocuments()) {
                Conversation conversation = docSnap.toObject(Conversation.class);
                if (conversation != null && !isGroup(conversation) && conversation.getMembers().contains(mSelectedUser.getID())) {
                    FirebaseFirestore
                            .getInstance()
                            .collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                            .document(conversation.getId())
                            .delete();
                    break;
                }
            }
            showToast("Removed friend completed!");
            finish();
        }
    };

    private boolean isGroup(Conversation conversation) {
        try { Double.parseDouble(conversation.getId());return true;
        } catch (Exception ex) { return false; }
    }
}