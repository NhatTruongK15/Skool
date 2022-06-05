package com.example.clown.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.clown.databinding.ActivityFriendProfileBinding;

public class FriendProfileActivity extends AppCompatActivity {
    private ActivityFriendProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}