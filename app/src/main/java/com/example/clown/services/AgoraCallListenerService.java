package com.example.clown.services;

import static io.agora.rtm.RtmClient.createInstance;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.clown.activities.CallActivity;
import com.example.clown.activities.CallReceivedActivity;
import com.example.clown.agora.EngineEventListener;
import com.example.clown.agora.IEventListener;
import com.example.clown.agora.RtmTokenBuilder;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;

import java.util.Map;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.LocalInvitation;
import io.agora.rtm.RemoteInvitation;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmCallManager;
import io.agora.rtm.RtmClient;

public class AgoraCallListenerService extends JobService implements IEventListener, ResultCallback<Void> {
    private static final String TAG = AgoraCallListenerService.class.getName();

    private final static String appId = Constants.AGORA_APP_ID;
    private final static String appCertificate = Constants.AGORA_APP_CERTIFICATE;
    private final static int expiredTimeStamp = Constants.EXPIRED_TIME_STAMP;

    protected final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.ACT_AGORA_LOCAL_INVITATION_SEND:
                    String remoteUserId = intent.getStringExtra(Constants.KEY_REMOTE_ID);
                    String channelId = intent.getStringExtra(Constants.KEY_RTC_CHANNEL_ID);
                    startCall(remoteUserId, channelId);
                    break;

                case Constants.ACT_AGORA_LOCAL_INVITATION_CANCELED:
                    mRtmCallManager.cancelLocalInvitation(mLocalInvitation, AgoraCallListenerService.this);
                    break;

                case Constants.ACT_AGORA_REMOTE_INVITATION_ACCEPTED:
                    mRtmCallManager.acceptRemoteInvitation(mRemoteInvitation, AgoraCallListenerService.this);
                    break;

                case Constants.ACT_AGORA_REMOTE_INVITATION_REFUSED:
                    mRtmCallManager.refuseRemoteInvitation(mRemoteInvitation, AgoraCallListenerService.this);
                    break;
            }
        }
    };

    private RtmClient mRtmClient;
    private RtmCallManager mRtmCallManager;

    private LocalInvitation mLocalInvitation;
    private RemoteInvitation mRemoteInvitation;

    private String mUserId;
    private boolean mIsLoggedIn;

    private volatile boolean mIsCanceled;

    @Override
    public boolean onStartJob(JobParameters params) {
        showToast("Agora job service started");
        
        Initialize();
        
        logIn();

        startListener(params);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        showToast("AgoraService stopped!");

        cleanUp();

        return true;
    }

    //region FUNCTIONS
    private void Initialize() {
        Log.e(TAG, "AgoraService Initialize!");

        // Get current user ID
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        mUserId = preferenceManager.getUser().getID();

        // AgoraRtm Init
        try {
            EngineEventListener mEngineEventListener = new EngineEventListener();
            mRtmClient = createInstance(this, appId, mEngineEventListener);
            mRtmCallManager = mRtmClient.getRtmCallManager();
            mRtmCallManager.setEventListener(mEngineEventListener);
            mEngineEventListener.registerEventListener(this);
            Log.e(TAG, "AgoraRtm initialized!");
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

        // Invitations Init
        resetInvitations();

        // Broadcast receiver register
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACT_AGORA_LOCAL_INVITATION_SEND);
        intentFilter.addAction(Constants.ACT_AGORA_LOCAL_INVITATION_CANCELED);
        intentFilter.addAction(Constants.ACT_AGORA_REMOTE_INVITATION_ACCEPTED);
        intentFilter.addAction(Constants.ACT_AGORA_REMOTE_INVITATION_REFUSED);
        registerReceiver(mBroadcastReceiver, intentFilter);

        mIsCanceled = false;
    }

    private void startListener(JobParameters params) {
        Log.e(TAG, "Listener stared!");

        Runnable task = () -> {

            //noinspection StatementWithEmptyBody
            while (!mIsCanceled);

            showToast ("Agora job service finished!");

            logOut();

            jobFinished(params, false);
        };

        Thread thread = new Thread(task);
        thread.start();
    }

    private void cleanUp() {
        // release AgoraRtm
        try {
            if (mRtmClient != null) {
                mRtmClient.release();
                mRtmClient = null;
            }

            Log.e(TAG, "Agora Rtm released!");
        } catch (Exception ex) { Log.e(TAG, ex.getMessage()); }

        // Broadcast receiver unregister
        unregisterReceiver(mBroadcastReceiver);

        // Stop service
        mIsCanceled = true;
    }

    private void resetInvitations() {
        mLocalInvitation = null;
        mRemoteInvitation = null;
    }

    private void startCall(String remoteUserId, String channelId) {
        try {
            mLocalInvitation = mRtmCallManager.createLocalInvitation(remoteUserId);
            mLocalInvitation.setChannelId(channelId);
            mRtmCallManager.sendLocalInvitation(mLocalInvitation,this);

            Bundle bundle2 = new Bundle();
            bundle2.putString(Constants.KEY_DOCUMENT_REFERENCE_ID, remoteUserId);
            bundle2.putString(Constants.KEY_RTC_CHANNEL_ID, channelId);
            bundle2.putBoolean(Constants.KEY_IS_CALLER, true);

            startActivity(CallActivity.class, bundle2);
        } catch (Exception ex) { Log.e(TAG, ex.getMessage()); }
    }

    private void logIn() {
        try {
            showToast("Agora logged in!");
            String mRtmToken = rtmTokenGenerator();
            mRtmClient.login(mRtmToken, mUserId, this);
            mIsLoggedIn = true;
            Log.e(TAG, "Agora logged in!");
        } catch (Exception ex) { Log.e(TAG, ex.getMessage()); }
    }

    private void logOut() {
        try {
            showToast("Agora logged out!");
            mRtmClient.logout(this);
            mIsLoggedIn = false;
            Log.e(TAG, "Agora logged out!");
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private String rtmTokenGenerator() {
        try {
            RtmTokenBuilder rtmTokenBuilder = new RtmTokenBuilder();
            int timestamp = (int)(System.currentTimeMillis() / 1000 + expiredTimeStamp);
            String rtmToken = rtmTokenBuilder.buildToken(
                    appId,
                    appCertificate,
                    mUserId,
                    RtmTokenBuilder.Role.Rtm_User,
                    timestamp);
            Log.e("[INFO] ", "Rtm Token generated!");
            return rtmToken;
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
        }
        return null;
    }

    private void startActivity(Class<?> activityClass, Bundle bundle) {
        try {
            Intent intent = new Intent(getApplicationContext(), activityClass);
            intent.putExtra(Constants.KEY_REMOTE_USER_DATA, bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
        }
    }

    private void showToast(String msg) {
        ContextCompat
                .getMainExecutor(getApplicationContext())
                .execute(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
    }
    //endregion

    //region AGORA_EVENT_LISTENERS
    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {

    }

    @Override
    public void onUserJoined(int uid, int elapsed) {

    }

    @Override
    public void onUserOffline(int uid, int reason) {

    }

    @Override
    public void onConnectionStateChanged(int status, int reason) {
        Log.d(TAG, "Connection state changed!");
        Log.d(TAG, "Status " + status);
        Log.d(TAG, "Reason " + reason);
    }

    @Override
    public void onPeersOnlineStatusChanged(Map<String, Integer> map) {

    }

    @Override
    public void onLocalInvitationReceived(LocalInvitation localInvitation) {
        Log.e("[INFO] ", "Local invitation received!");
    }

    @Override
    public void onLocalInvitationAccepted(LocalInvitation localInvitation, String response) {
        Log.e("[INFO] ", "Local invitation accepted!");
        resetInvitations();
    }

    @Override
    public void onLocalInvitationRefused(LocalInvitation localInvitation, String response) {
        Log.e("[INFO] ", "Local invitation refused!");
        sendBroadcast(new Intent(Constants.ACT_AGORA_LOCAL_INVITATION_REFUSED)) ;
        resetInvitations();
    }

    @Override
    public void onLocalInvitationCanceled(LocalInvitation localInvitation) {
        Log.e("[INFO] ", "Local invitation canceled!");
        resetInvitations();
    }

    @Override
    public void onLocalInvitationFailure(LocalInvitation localInvitation, int errorCode) {
        Log.e("[INFO] ", "Local invitation failed!");
        sendBroadcast(new Intent(Constants.ACT_AGORA_LOCAL_INVITATION_FAILED));
        resetInvitations();
    }

    @Override
    public void onRemoteInvitationReceived(RemoteInvitation remoteInvitation) {
        Log.e("[INFO] ", "Remote invitation received!");
        if (mRemoteInvitation == null) {
            mRemoteInvitation = remoteInvitation;

            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_DOCUMENT_REFERENCE_ID, mRemoteInvitation.getCallerId());

            startActivity(CallReceivedActivity.class, bundle);
        }
    }

    @Override
    public void onRemoteInvitationAccepted(RemoteInvitation remoteInvitation) {
        Log.e("[INFO] ", "Remote invitation accepted!");
        if (mRemoteInvitation != null) {
            Bundle bundle1 = new Bundle();
            bundle1.putString(Constants.KEY_DOCUMENT_REFERENCE_ID, mRemoteInvitation.getCallerId());
            bundle1.putString(Constants.KEY_RTC_CHANNEL_ID, mRemoteInvitation.getChannelId());
            bundle1.putBoolean(Constants.KEY_IS_CALLER, false);
            startActivity(CallActivity.class, bundle1);
            resetInvitations();
        }
    }

    @Override
    public void onRemoteInvitationRefused(RemoteInvitation remoteInvitation) {
        Log.e("[INFO] ", "Remote invitation refused!");
        resetInvitations();
    }

    @Override
    public void onRemoteInvitationCanceled(RemoteInvitation remoteInvitation) {
        Log.e("[INFO] ", "Remote invitation canceled!");
        sendBroadcast(new Intent(Constants.ACT_AGORA_REMOTE_INVITATION_CANCELED));
        resetInvitations();
    }

    @Override
    public void onRemoteInvitationFailure(RemoteInvitation remoteInvitation, int errorCode) {
        Log.e("[INFO] ", "Remote invitation failed!");
        sendBroadcast(new Intent(Constants.ACT_AGORA_REMOTE_INVITATION_FAILED));
        resetInvitations();
    }

    @Override
    public void onSuccess(Void unused) { }

    @Override
    public void onFailure(ErrorInfo errorInfo) { }
    //endregion
}
