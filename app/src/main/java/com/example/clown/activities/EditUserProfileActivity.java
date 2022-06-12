package com.example.clown.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.clown.databinding.ActivityEditUserProfileBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Calendar;

public class EditUserProfileActivity extends BaseActivity {
    ActivityEditUserProfileBinding binding;
    private FirebaseFirestore database;
    private String type;
    private int curYear, curMonth, curDay;
    User dupUser = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        curYear = calendar.get(Calendar.YEAR);
        curMonth = calendar.get(Calendar.MONTH);
        curDay = calendar.get(Calendar.DAY_OF_MONTH);
        dupUser.Clone(mCurrentUser);


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
                        if (isValidSignInDetails()) {
                            dupUser.setEmail(binding.newValueForProfile.getText().toString());
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(mCurrentUser.getID())
                                    .update(
                                            Constants.KEY_EMAIL, binding.newValueForProfile.getText().toString()
                                    );

                        }
                        break;
                    case "firstName":
                       dupUser.setFirstName(binding.newValueForProfile.getText().toString());
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(mCurrentUser.getID())
                                .update(
                                        Constants.KEY_FIRST_NAME, binding.newValueForProfile.getText().toString()
                                );
                        break;

                    case "lastName":
                        dupUser.setLastName(binding.newValueForProfile.getText().toString());
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(mCurrentUser.getID())
                                .update(
                                        Constants.KEY_LAST_NAME, binding.newValueForProfile.getText().toString()
                                );


                        break;

                    case "dateOfBirth":
                         //currentUser.setDateOfBirth(new Date(curYear, curMonth -1, curDay));
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(mCurrentUser.getID())
                                .update(
                                        Constants.KEY_DATE_OF_BIRTH, mCurrentUser.getDateOfBirth()
                                );

                        break;
                    case "gender":
                        dupUser.setGender(binding.genderSpinner.getSelectedItem().toString());
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(mCurrentUser.getID())
                                .update(
                                        Constants.KEY_BIO, binding.genderSpinner.getSelectedItem().toString()
                                );
                        break;
                    case "Bio":
                        dupUser.setLastName(binding.newValueForProfile.getText().toString());
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(mCurrentUser.getID())
                                .update(
                                        Constants.KEY_BIO, binding.newValueForProfile.getText().toString()
                                );
                        break;
                }
                mPreferenceManager.putUser(dupUser);
                onBackPressed();
                finish();
            }
        });
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void Init() {
        binding = ActivityEditUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();

        //set all the control GONE
        binding.newValueForProfile.setVisibility(View.GONE);
        binding.genderSpinner.setVisibility(View.GONE);
        binding.bioEditBox.setVisibility(View.GONE);
        binding.datePicker.setVisibility(View.GONE);

        type = getIntent().getExtras().getString(Constants.KEY_EDIT_PROFILETYPE);


        LoadActivityDetails();
    }

    private void LoadActivityDetails() {
        switch (type) {
            case "name":
                binding.newValueForProfile.setVisibility(View.VISIBLE);
                binding.typeLabel.setText("Username");
                binding.guideline.setText("You can choose a username here. People will able to find you by this username and contact with you without needing your phone number \nYou " +
                        "can use a-z, 0- 9 and uderscores. Mininum lenght is 5 character");
                break;
            case "email":
                binding.newValueForProfile.setVisibility(View.VISIBLE);
                binding.typeLabel.setText("Email");
                binding.guideline.setText("You can change your email here.");
                break;
            case "firstName":
                binding.newValueForProfile.setVisibility(View.VISIBLE);
                binding.typeLabel.setText("First Name");
                binding.guideline.setText("You can change your first name here. Mininum lenght is 5 character");
                break;
            case "lastName":
                binding.newValueForProfile.setVisibility(View.VISIBLE);
                binding.typeLabel.setText("Last Name");
                binding.guideline.setText("You can change your last name here. Mininum lenght is 5 character");
                break;
            case "dateOfBirth":
                binding.datePicker.setVisibility(View.VISIBLE);
                binding.typeLabel.setText("Date Of Birth");
                binding.guideline.setText("Pick your date of birth.");
                binding.datePicker.init(curYear, curMonth, curDay, new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        //Date temp =  new Date(year -1900, monthOfYear, dayOfMonth);
                        dupUser.setDateOfBirth(new Date(year -1900, monthOfYear, dayOfMonth));
                    }

                });
                break;
            case "gender":
                binding.genderSpinner.setVisibility(View.VISIBLE);
                String[] arraySpinner = new String[]{
                        "Male", "Female", "Other"
                };
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, arraySpinner);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.genderSpinner.setAdapter(adapter);
                binding.typeLabel.setText("Gender");
                binding.guideline.setText("Select your gender here.");
                break;
            case "Bio":
                binding.bioEditBox.setVisibility(View.VISIBLE);
                binding.typeLabel.setText("Bio");
                binding.guideline.setText("Let us and everyone something about yourself here.");

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

    protected void showToast(String message) {
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