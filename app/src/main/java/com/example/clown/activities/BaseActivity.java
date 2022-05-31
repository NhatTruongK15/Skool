package com.example.clown.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clown.models.User;
import com.example.clown.utilities.BaseApplication;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected static PreferenceManager mPreferenceManager;
    protected BaseApplication mBaseApplication;
    protected static User mCurrentUser;

    protected static boolean mIsInitialized = false;
    protected static boolean mIsSignedIn;

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
            mPreferenceManager = new PreferenceManager(getApplicationContext());
            mBaseApplication = (BaseApplication) getApplicationContext();

            if (mPreferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN) != null)
                mIsSignedIn = mPreferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN);

            mIsInitialized = true;
        }
    }

    private void clearReferences() {
        Activity currentActivity = mBaseApplication.getCurrentActivity();
        if (this.equals(currentActivity))
            mBaseApplication.setCurrentActivity(null);
    }

    protected void updateUserAvailability() {
        if (mCurrentUser == null) return;

        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mCurrentUser.getUserID())
                .update(Constants.KEY_AVAILABILITY, mIsSignedIn);
    }

    protected boolean startActivity(Class<?> targetActivity, @Nullable Bundle transferData) {
        try {
            Intent intent = new Intent(getApplicationContext(), targetActivity);
            intent.putExtra(Constants.KEY_TRANSFER_DATA, transferData);
            startActivity(intent);
            return true;
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
            return false;
        }
    }

    protected void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
