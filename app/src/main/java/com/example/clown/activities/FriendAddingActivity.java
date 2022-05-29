package com.example.clown.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.clown.databinding.ActivityFriendAddingBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.PreferenceManager;

public class FriendAddingActivity extends AppCompatActivity {
    private ActivityFriendAddingBinding binding;
    private PreferenceManager preferenceManager;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendAddingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding.progressBar.setVisibility(View.GONE);
        setListener();
    }


    private void setListener() {
        binding.buttonFindPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findFriendByPhoneNumber();
            }
        });

        binding.FindContactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findFriendByPhoneContact();
            }
        });
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void findFriendByPhoneContact() {
        Intent intent = new Intent(getApplicationContext(), PhoneContactListActivity.class);
        startActivity(intent);
    }

    private void findFriendByPhoneNumber() {
    }
}