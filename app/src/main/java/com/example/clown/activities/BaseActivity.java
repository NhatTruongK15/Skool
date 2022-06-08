package com.example.clown.activities;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.clown.R;
import com.example.clown.models.User;
import com.example.clown.utilities.BaseApplication;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class BaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected static PreferenceManager mPreferenceManager;
    protected static User mCurrentUser = new User();

    protected BaseApplication mBaseApplication;
    protected String[] REQUESTED_PERMISSIONS;

    protected static boolean mIsSignedIn;
    protected int PERMISSION_REQ_ID;

    //region BACKGROUND POSSIBILITY
    private static final String CHANNEL_ID = "TEST_CHANNEL";

    private ListenerRegistration mRegistration;
    private static boolean mIsChannelCreated = false;

    private final EventListener<DocumentSnapshot> mCurrentUserListener = (docSnap, error) -> {
        if (error != null) {
            Log.e("BaseActivity", error.getMessage());
            return;
        }

        if (docSnap != null && docSnap.exists()) {
            Log.e("BaseActivity", "Current user's updated!");
            User updatedUser = docSnap.toObject(User.class);
            if (updatedUser == null) return;

            // Check friends list updates
            int oldSize = mCurrentUser.getFriends().size();
            int newSize = updatedUser.getFriends().size();

            if (oldSize > newSize)
                for (int i = 0; i < oldSize; i++)
                    if (!updatedUser.getFriends().contains(mCurrentUser.getFriends().get(i)))
                        notifyFriendsRemoved(mCurrentUser.getFriends().get(i));

            // Check pending requests updates

            mPreferenceManager.putUser(updatedUser);
        }
    };

    private void notifyFriendsRemoved(String removedFriendID) {
        if (!mIsChannelCreated) createNotificationChannel();

        Log.e("BaseActivity", removedFriendID + "notification");
        Notification notification = new NotificationCompat
                .Builder(getApplicationContext(), CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_call)
                .setContentText(removedFriendID)
                .setChannelId(CHANNEL_ID)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Timestamp.now().getNanoseconds(), notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        // Create channel
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);

        // Register channel
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        mIsChannelCreated = true;
    }
    //endregion

    public User getCurrentUser() { return mCurrentUser; }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseInit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBaseApplication.setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearBaseAppRefs();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPreferenceManager.unRegisterChangesListener(this);
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

    //region PRIVATES
    private void baseInit() {
        mBaseApplication = (BaseApplication) getApplicationContext();

        if (mPreferenceManager != null) {
            mPreferenceManager.registerChangesListener(this);
            mIsSignedIn = mPreferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN);
        }

        /*if (mIsSignedIn) mRegistration = FirebaseFirestore
                    .getInstance()
                    .collection(Constants.KEY_COLLECTION_USERS)
                    .document(mCurrentUser.getID())
                    .addSnapshotListener(mCurrentUserListener);*/
    }

    private void clearBaseAppRefs() {
        Activity currentActivity = mBaseApplication.getCurrentActivity();
        if (this.equals(currentActivity))
            mBaseApplication.setCurrentActivity(null);
    }
    //endregion

    //region INHERITANCES
    protected void updateUserAvailability() {
        if (mCurrentUser == null) return;

        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mCurrentUser.getID())
                .update(Constants.KEY_AVAILABILITY, mIsSignedIn);
    }

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

    protected boolean checkPermission(String permission) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
            return false;
        }
        return true;
    }

    protected void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    //endregion
}
