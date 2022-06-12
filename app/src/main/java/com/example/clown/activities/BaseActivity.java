package com.example.clown.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getName();

    protected static PreferenceManager mPreferenceManager;
    protected static User mCurrentUser = new User();

    protected BaseApplication mBaseApplication;

    protected String[] REQUESTED_PERMISSIONS;
    protected int PERMISSION_REQ_ID;

    protected BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "Local current user updated");
            mCurrentUser.Clone(mPreferenceManager.getUser());
        }
    };

    public User getCurrentUser() { return mCurrentUser; }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseInit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityBind();
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityUnBind();
    }

    //region PRIVATES
    private void baseInit() {
        mBaseApplication = (BaseApplication) getApplicationContext();

        if (mCurrentUser != null && !mCurrentUser.getAvailability())
            updateUserAvailability();
    }

    private void updateUserAvailability() {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mCurrentUser.getID())
                .update(Constants.KEY_AVAILABILITY, true);
    }

    private void activityBind() {
        Log.e(TAG, "Binding " + this.getLocalClassName());

        // Base Application bind
        mBaseApplication.setCurrentActivity(this);

        // Broadcast Receiver bind
        IntentFilter intentFilter = new IntentFilter(Constants.ACT_UPDATE_CURRENT_USER);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void activityUnBind() {
        Log.e(TAG, "Unbind " + this.getLocalClassName());

        // Base Application unbind
        Activity currentActivity = mBaseApplication.getCurrentActivity();

        if (this.equals(currentActivity))
            mBaseApplication.setCurrentActivity(null);

        // Broadcast Receiver unbind
        unregisterReceiver(mBroadcastReceiver);
    }
    //endregion

    //region INHERITANCES
    protected boolean startActivity(String TAG, Class<?> targetActivity, @Nullable Bundle transferData) {
        try {
            Log.e(TAG, targetActivity.getName() + " started!");
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
    //endregion
}
