package com.example.clown.activities;

import android.os.Bundle;

import com.example.clown.databinding.ActivityFriendProfileBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;

public class FriendProfileActivity extends BaseActivity {
    private ActivityFriendProfileBinding binding;
    private User mSelectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        Init();
        
        loadUserProfile();
        
        setListeners();
    }

    private void Init() {
        mSelectedUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
    }

    private void loadUserProfile() {
        binding.rivProfileAvatar.setImageBitmap(mSelectedUser.getBitmapAvatar());
        binding.tvUsername.setText(mSelectedUser.getUsername());
        binding.tvProfilePhoneNumber.setText(mSelectedUser.getPhoneNumber());
        binding.tvProfileEmail.setText(mSelectedUser.getEmail());
    }

    private void setListeners() {
        binding.ivUnfriend.setOnClickListener(v -> removeFriend());
    }

    private void removeFriend() {
        mSelectedUser.getFriends().remove(mCurrentUser.getID());
        mCurrentUser.getFriends().remove(mSelectedUser.getID());
        mPreferenceManager.putUser(mCurrentUser);
    }
}