package com.example.clown.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.clown.R;
import com.example.clown.models.User;
import com.example.clown.utilities.BaseApplication;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class UserListenerService extends JobService {
    public static final String TAG = UserListenerService.class.getName();

    private static final boolean FRIEND_ADDED = true;
    private static final boolean FRIEND_REMOVED = false;

    private BaseApplication mBaseApplication;
    private PreferenceManager mPreferenceManager;
    private ListenerRegistration mListenerRegister;
    private volatile boolean mIsCanceled;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e(TAG, "UserListener started!");

        Initialize();

        createNotificationChannel();

        isUserOffline();

        startListener(params);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e(TAG, "UserListener stopped!");

        updateUserAvailability();

        cleanUp();

        return true;
    }

    //region FUNCTIONS
    private void Initialize() {
        Log.e(TAG, "UserListener's initializing!");

        // Base Application
        mBaseApplication = (BaseApplication) getApplicationContext();

        // Preference Manager
        mPreferenceManager = new PreferenceManager(getApplicationContext());
        mPreferenceManager.registerChangesListener(this::onPreferenceManagerChanged);

        // Firestore Listener
        mListenerRegister = FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mPreferenceManager.getUser().getID())
                .addSnapshotListener(this::onUserChanged);

        // Service cancel flag
        mIsCanceled = false;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        // Create channel
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(Constants.KEY_CHANNEL_ID, Constants.KEY_CHANNEL_ID, importance);

        // Register channel
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void isUserOffline() {
        // If no activity is active then the user is offline
        if (mBaseApplication.getCurrentActivity() == null)
            updateUserAvailability();
    }

    private void updateUserAvailability() {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mPreferenceManager.getUser().getID())
                .update(Constants.KEY_AVAILABILITY, false);
    }

    private void startListener(JobParameters params) {
        Log.e(TAG, "UserListener stared!");

        Runnable task = () -> {
            while (!mIsCanceled);

            Log.e(TAG, "CurrentUserListener finished!");

            showToast("Service finished!");

            jobFinished(params, false);
        };

        Thread thread = new Thread(task);
        thread.start();
    }

    private void cleanUp() {
        mPreferenceManager.unRegisterChangesListener(this::onPreferenceManagerChanged);
        mPreferenceManager.clear();

        mListenerRegister.remove();

        mIsCanceled = true;
    }

    private void checkFriendsChanges(List<String> oldFriends, List<String> newFriends) {
        List<String> sourceRecords, checkRecords;
        boolean bType;

        if (oldFriends.size() > newFriends.size()) {
            // Friends removed
            sourceRecords = oldFriends;
            checkRecords = newFriends;
            bType = FRIEND_REMOVED;
        } else {
            // Friends added
            sourceRecords = newFriends;
            checkRecords = oldFriends;
            bType = FRIEND_ADDED;
        }

        for (String friendID : sourceRecords)
            if (!checkRecords.contains(friendID))
                notifyFriendsChanges(friendID, bType);
    }

    private void checkReceivedRequestsChanges(List<String> oldRequests, List<String> newRequests) {
    }

    private void notifyFriendsChanges(String friendID, boolean changeType) {
        Notification notification = new NotificationCompat
                .Builder(getApplicationContext(), Constants.KEY_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_call)
                .setContentText(friendID)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Timestamp.now().getNanoseconds(), notification);
    }

    private void showToast(String msg) {
        ContextCompat
                .getMainExecutor(getApplicationContext())
                .execute(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
    }
    //endregion

    //region EVENT_LISTENERS
    public void onPreferenceManagerChanged(SharedPreferences sharedPreferences, String key) {
        Log.e(TAG, "PreferenceManager changed!");

        if (key.equals(Constants.KEY_CURRENT_USER))
            sendBroadcast(new Intent(Constants.ACT_UPDATE_CURRENT_USER));
    }

    private void onUserChanged(DocumentSnapshot docSnap, FirebaseFirestoreException error) {
        if (error != null) {
            Log.e(TAG, error.getMessage());
            return;
        }

        if (docSnap != null && docSnap.exists()) {
            Log.e(TAG, "Current user's updated!");

            User oldRecord = this.mPreferenceManager.getUser();
            User newRecord = docSnap.toObject(User.class);

            // Update current user changes
            this.mPreferenceManager.putUser(newRecord);

            if (newRecord == null) return;

            checkFriendsChanges(
                    oldRecord.getFriends(),
                    newRecord.getFriends()
            );

            // Check received requests updates
            checkReceivedRequestsChanges(
                    oldRecord.getReceivedRequests(),
                    newRecord.getReceivedRequests()
            );
        }
    }
    //endregion
}
