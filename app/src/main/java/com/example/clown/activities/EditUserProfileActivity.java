package com.example.clown.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.clown.R;
import com.example.clown.databinding.ActivityEditUserProfileBinding;
import com.example.clown.databinding.ActivityMyProfileBinding;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditUserProfileActivity extends AppCompatActivity {
    ActivityEditUserProfileBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();

        SetListener();
    }

    private void SetListener() {
        binding.imageConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (type) {
                    case "name":
                        break;
                    case "email":
                        if(isValidSignInDetails()){
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                    .update(
                                            Constants.KEY_EMAIL, binding.newValueForProfile.getText().toString()
                                    );

                            onBackPressed();
                            preferenceManager.putString(Constants.KEY_EMAIL, binding.newValueForProfile.getText().toString());
                        }
                        break;
                }

            }
        });
    }

    private void Init() {
        binding = ActivityEditUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

        type = getIntent().getExtras().getString(Constants.KEY_EDIT_PROFILETYPE);

        LoadActivityDetails();
    }

    private void LoadActivityDetails() {
        switch (type) {
            case "name":
                binding.typeLabel.setText("Username");
                binding.guideline.setText("You can chose a username here. People will able to find you by this username and contact with you without needing your phone number \nYou " +
                        "can use a-z, 0- 9 and uderscores. Mininum lenght is 5 character");
                break;
            case "email":
                binding.typeLabel.setText("Email");
                binding.guideline.setText("You can change your email here.");
                break;
        }

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    //region Utilities

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }

    private Boolean isValidSignInDetails() {
        boolean flag = false;
        switch (type) {
            case "name":
                flag = false;
                break;
            case "email":
                if (binding.newValueForProfile.getText().toString().trim().isEmpty()) {
                    showToast("Enter email");
                    flag = false;
                } else if (!(Patterns.EMAIL_ADDRESS.matcher(binding.newValueForProfile.getText().toString()).matches())) {
                    showToast("Enter valid email");
                    flag = false;
                } else {
                    flag = true;
                }
                break;
        }
        return flag;

    }
}