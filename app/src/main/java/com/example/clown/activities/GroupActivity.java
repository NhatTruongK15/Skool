package com.example.clown.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clown.R;
import com.example.clown.databinding.ActivityGroupBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;


public class GroupActivity extends AppCompatActivity {

    private ActivityGroupBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListener();
    }

    private void setListener() {
        binding.btnAddGroupMember.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(),GroupChatActivity.class);
            intent.putExtra(Constants.KEY_USER,preferenceManager.getString(Constants.KEY_USER));
            startActivity(intent);
        });
    }
}