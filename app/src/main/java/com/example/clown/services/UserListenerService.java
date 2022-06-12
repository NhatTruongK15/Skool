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
import android.widget.RemoteViews;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class UserListenerService extends JobService {
    public static final String TAG = UserListenerService.class.getName();

    private static final boolean RECORD_ADDED = true;
    private static final boolean RECORD_REMOVED = false;
    private static final int REC_TYPE_FRIEND = 0;
    private static final int REC_TYPE_RECEIVED_REQUEST = 1;

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
        mPreferenceManager.registerChangesListener(mPreferenceListener);
        String currentUserID = mPreferenceManager.getUser().getID();

        // Firestore Listener
        mListenerRegister = FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(currentUserID)
                .addSnapshotListener(mCurrentUserListener);

        // Service cancel flag
        mIsCanceled = false;
    }

    private void createNotificationChannel()      {
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
        if (mBaseApplication.getCurrentActivity() == null) {
            Log.e(TAG, "User is offline!");
            updateUserAvailability();
        }
    }

    private synchronized void updateUserAvailability() {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mPreferenceManager.getUser().getID())
                .update(Constants.KEY_AVAILABILITY, false);
    }

    private void startListener(JobParameters params) {
        Log.e(TAG, "UserListener stared!");

        Runnable task = () -> {

            while (true) if (mIsCanceled) break;

            Log.e(TAG, "CurrentUserListener finished!");

            showToast("Service finished!");

            jobFinished(params, false);
        };

        Thread thread = new Thread(task);
        thread.start();
    }

    private void cleanUp() {
        mPreferenceManager.unRegisterChangesListener(mPreferenceListener);
        mPreferenceManager.clear();

        mListenerRegister.remove();

        mIsCanceled = true;
    }

    private void checkFriendsChanges(List<String> oldFriends, List<String> newFriends, int nType) {
        // No friends change
        if (oldFriends.equals(newFriends)) return;

        // Friends added
        List<String> sourceRecords = newFriends;
        List<String> checkRecords = oldFriends;
        boolean bType = RECORD_ADDED;

        // Friends removed
        if (oldFriends.size() > newFriends.size()) {
            sourceRecords = oldFriends;
            checkRecords = newFriends;
            bType = RECORD_REMOVED;
        }

        for (String recordID : sourceRecords)
            if (!checkRecords.contains(recordID))
                switch(nType) {
                    case REC_TYPE_FRIEND:
                        notifyFriendsChanges(recordID, bType); break;
                    case REC_TYPE_RECEIVED_REQUEST:
                        notifyReceivedRequestsChanged(recordID, bType); break;
                }
    }

    private void notifyFriendsChanges(String friendID, boolean bIsFriendAdded) {
        Intent intent = bIsFriendAdded ?
                new Intent(Constants.ACT_FRIEND_ADDED) :
                new Intent(Constants.ACT_FRIEND_REMOVED);
        sendBroadcast(intent);

    }

    private void notifyReceivedRequestsChanged(String requesterID, boolean bIsRequestAdded) {
        Intent intent = bIsRequestAdded ?
                new Intent(Constants.ACT_RECEIVED_REQUEST_ADDED) :
                new Intent(Constants.ACT_RECEIVED_REQUEST_REMOVED);
        sendBroadcast(intent);

        if (bIsRequestAdded) showNotification(
                R.drawable.ic_call,
                R.layout.notification_friend_request,
                Constants.NOTIFICATION_FRIEND_REQUEST_TITLE
        );
    }

    private void showNotification(int smallIconID, int customLayout, String title) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), customLayout);

        Notification notification = new NotificationCompat
                .Builder(getApplicationContext(), Constants.KEY_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(title)
                .setSmallIcon(smallIconID)
                .setCustomBigContentView(remoteViews)
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

    //region CALLBACKS
    protected final SharedPreferences.OnSharedPreferenceChangeListener mPreferenceListener = (sharedPreferences, key) -> {
        Log.e(TAG, "PreferenceManager changed!");
        if (key.equals(Constants.KEY_CURRENT_USER))
            sendBroadcast(new Intent(Constants.ACT_UPDATE_CURRENT_USER));
    };

    protected final EventListener<DocumentSnapshot> mCurrentUserListener = (docSnap, error) -> {
        if (error != null) {
            Log.e(TAG, error.getMessage());
            return;
        }

        if (docSnap != null && docSnap.exists() && !docSnap.getMetadata().hasPendingWrites()) {
            Log.e(TAG, "Current user's updated!");

            User oldRecord = this.mPreferenceManager.getUser();
            User newRecord = docSnap.toObject(User.class);

            if (newRecord == null) return;

            // Update current user changes
            this.mPreferenceManager.putUser(newRecord);

            // Check friends changes
            checkFriendsChanges(
                    oldRecord.getFriends(),
                    newRecord.getFriends(),
                    REC_TYPE_FRIEND
            );

            // Check received requests changes
            checkFriendsChanges(
                    oldRecord.getReceivedRequests(),
                    newRecord.getReceivedRequests(),
                    REC_TYPE_RECEIVED_REQUEST
            );
        }
    };
    //endregion
}
