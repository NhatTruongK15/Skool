package com.example.clown.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.clown.R;
import com.example.clown.databinding.ActivityFriendAddingBinding;
import com.example.clown.utilities.PreferenceManager;

public class FriendAddingActivity extends AppCompatActivity {
    private ActivityFriendAddingBinding binding;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_adding);
        preferenceManager = new PreferenceManager(getApplicationContext());

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
    }

    private void findFriendByPhoneContact() {
    }

    private void findFriendByPhoneNumber() {
    }
}