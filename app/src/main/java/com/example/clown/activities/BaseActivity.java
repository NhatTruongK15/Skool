package com.example.clown.activities;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clown.models.User;
import com.example.clown.utilities.BaseApplication;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected static BaseApplication mBaseApplication;
    protected static PreferenceManager mPreferenceManager;
    protected static User mCurrentUser;
    protected static boolean mIsInitialized = false;
    protected static boolean mIsSignedIn = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseInit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearReferences();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserAvailability();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBaseApplication.setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearReferences();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.KEY_IS_SIGNED_IN:
                mIsSignedIn = mPreferenceManager.getBoolean(key);
                break;

            case Constants.KEY_USER:
                mCurrentUser = mPreferenceManager.getUser();
                break;
        }
    }

    private void baseInit() {
        if (!mIsInitialized) {
            mBaseApplication = (BaseApplication) getApplicationContext();
            mPreferenceManager = new PreferenceManager(getApplicationContext());
            mIsInitialized = true;
        }
    }

    private void clearReferences() {
        AppCompatActivity currentActivity = mBaseApplication.getCurrentActivity();
        if (this.equals(currentActivity))
            mBaseApplication.setCurrentActivity(null);
    }

    private void updateUserAvailability() {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mCurrentUser.getId())
                .update(Constants.KEY_AVAILABILITY, true);
    }
}
