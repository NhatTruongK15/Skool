package com.example.clown.agora;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.clown.R;
import com.example.clown.activities.CallActivity;
import com.example.clown.activities.CallReceivedActivity;
import com.example.clown.utilities.Constants;

import java.util.ArrayList;
import java.util.Map;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.LocalInvitation;
import io.agora.rtm.RemoteInvitation;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmCallManager;
import io.agora.rtm.RtmClient;

public class AgoraService extends Service implements IEventListener, ResultCallback<Void> {
    private final static String appId = Constants.AGORA_APP_ID;
    private final static String appCertificate = Constants.AGORA_APP_CERTIFICATE;
    private final static int expiredTimeStamp = Constants.EXPIRED_TIME_STAMP;

    private final Messenger mMessenger = new Messenger(new AgoraService.IncomingHandler());
    private ArrayList<Messenger> mClients = new ArrayList<>();
    private NotificationManager mNM;

    private RtmClient mRtmClient;
    private RtmCallManager mRtmCallManager;
    private EngineEventListener mEngineEventListener;

    private LocalInvitation mLocalInvitation;
    private RemoteInvitation mRemoteInvitation;

    private String mUserId;
    private String mRtmToken;
    private boolean mIsLoggedIn;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
         return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        showToast("AgoraService created!");
        defaultSetUp();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rtmRelease();

        this.stopForeground(true);
        mNM.cancelAll();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            mNM.deleteNotificationChannel(Constants.AGORA_APP_ID);

        showToast("AgoraService destroyed!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            mUserId = intent.getStringExtra(Constants.KEY_DOCUMENT_REFERENCE_ID);
        }

        initRtm();

        createNotificationChannel();
        startForegroundNotification();

        showToast("AgoraService started!");
        return START_NOT_STICKY;
    }

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
        Log.d("[INFO] ", "Connection state changed!");
        Log.d("[INFO] ", "Status " + status);
        Log.d("[INFO] ", "Reason " + reason);
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
        int i = mClients.size() - 1;
        toClient(i, Constants.MSG_AGORA_LOCAL_INVITATION_REFUSED, null);
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
        int i = mClients.size() - 1;
        toClient(i, Constants.MSG_AGORA_LOCAL_INVITATION_FAILED, null);
        resetInvitations();
    }

    @Override
    public void onRemoteInvitationReceived(RemoteInvitation remoteInvitation) {
        Log.e("[INFO] ", "Remote invitation received!");
        if (mRemoteInvitation == null) {
            mRemoteInvitation = remoteInvitation;

            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_DOCUMENT_REFERENCE_ID, mRemoteInvitation.getCallerId());

            invokeActivity(CallReceivedActivity.class, bundle);
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
            invokeActivity(CallActivity.class, bundle1);
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
        int i = mClients.size() - 1;
        toClient(i, Constants.MSG_AGORA_REMOTE_INVITATION_CANCELED, null);
        resetInvitations();
    }

    @Override
    public void onRemoteInvitationFailure(RemoteInvitation remoteInvitation, int errorCode) {
        Log.e("[INFO] ", "Remote invitation failed!");
        int i = mClients.size() - 1;
        toClient(i, Constants.MSG_AGORA_REMOTE_INVITATION_FAILED, null);
        resetInvitations();
    }

    @Override
    public void onSuccess(Void unused) {

    }

    @Override
    public void onFailure(ErrorInfo errorInfo) {

    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constants.MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    Log.e("[INFO] ", "Client added!");
                    break;

                case Constants.MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    Log.e("[INFO] ", "Client removed!");
                    break;

                case Constants.MSG_AGORA_LOG_IN:
                    if (!mIsLoggedIn) {
                        if (mUserId == null)
                            mUserId = msg.getData().getString(Constants.KEY_DOCUMENT_REFERENCE_ID);
                        logIn();
                        Log.e("[INFO] ", "Agora login!");
                    }
                    break;

                case Constants.MSG_AGORA_LOG_OUT:
                    if (mIsLoggedIn) {
                        logOut();
                        Log.e("[INFO] ", "Agora logout!");
                    }
                    break;

                case Constants.MSG_AGORA_LOCAL_INVITATION_SEND:
                    Bundle bundle = msg.getData();
                    String remoteUserId = bundle.getString(Constants.KEY_REMOTE_ID);
                    String channelId = bundle.getString(Constants.KEY_RTC_CHANNEL_ID);
                    startCall(remoteUserId, channelId);
                    break;

                case Constants.MSG_AGORA_LOCAL_INVITATION_CANCELED:
                    mRtmCallManager.cancelLocalInvitation(mLocalInvitation, AgoraService.this);
                    break;

                case Constants.MSG_AGORA_REMOTE_INVITATION_ACCEPTED:
                    mRtmCallManager.acceptRemoteInvitation(mRemoteInvitation, AgoraService.this);
                    break;

                case Constants.MSG_AGORA_REMOTE_INVITATION_REFUSED:
                    mRtmCallManager.refuseRemoteInvitation(mRemoteInvitation, AgoraService.this);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void defaultSetUp() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mRtmClient = null;
        mRtmCallManager = null;
        mEngineEventListener = null;
        mLocalInvitation = null;
        mRemoteInvitation = null;
        mUserId = null;
        mRtmToken = null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    Constants.AGORA_APP_ID,
                    "Clown",
                    NotificationManager.IMPORTANCE_LOW);
            mNM.createNotificationChannel(notificationChannel);
        }
    }

    private void startForegroundNotification() {
        final Notification.Builder builder = new Notification.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_transparent)
                .setPriority(Notification.PRIORITY_LOW)
                .setVisibility(Notification.VISIBILITY_SECRET);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                builder.setChannelId(Constants.AGORA_APP_ID);

        final Notification notification;
        notification = builder.build();

        this.startForeground(1, notification);
    }

    private void initRtm() {
        try {
            mEngineEventListener = new EngineEventListener();
            mRtmClient = RtmClient.createInstance(AgoraService.this, appId, mEngineEventListener);
            mRtmCallManager = mRtmClient.getRtmCallManager();
            mRtmCallManager.setEventListener(mEngineEventListener);
            mEngineEventListener.registerEventListener(this);
            showToast("AgoraRtm initialized!");
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
        }
    }

    private void logIn() {
        try {
            mRtmToken = rtmTokenGenerator();
            mRtmClient.login(mRtmToken, mUserId, this);
            mIsLoggedIn = true;
            showToast("Agora logged in!");
        } catch (Exception ex) {
                Log.e("[ERROR] ", ex.getMessage());
        }
    }

    private void logOut() {
        try {
            mRtmClient.logout(this);
            mIsLoggedIn = false;
            showToast("Agora logged out!");
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
        }
    }

    private void rtmRelease() {
        try {
            if (mRtmClient != null) {
                mRtmClient.release();
                mRtmClient = null;
            }
            Log.e("[INFO] ", "Agora Rtm released!");
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
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

    private void resetInvitations() {
        mLocalInvitation = null;
        mRemoteInvitation = null;
    }

    private void startCall(String remoteUserId, String channelId) {
        try {
            mLocalInvitation = mRtmCallManager.createLocalInvitation(remoteUserId);
            mLocalInvitation.setChannelId(channelId);
            mRtmCallManager.sendLocalInvitation(mLocalInvitation, AgoraService.this);

            Bundle bundle2 = new Bundle();
            bundle2.putString(Constants.KEY_DOCUMENT_REFERENCE_ID, remoteUserId);
            bundle2.putString(Constants.KEY_RTC_CHANNEL_ID, channelId);
            bundle2.putBoolean(Constants.KEY_IS_CALLER, true);

            invokeActivity(CallActivity.class, bundle2);
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
        }
    }

    private void toClient(int i, int msgNotification, Bundle bundle) {
        try {
            Message msg = Message.obtain(null, msgNotification);
            msg.replyTo = mMessenger;
            msg.setData(bundle);
            mClients.get(i).send(msg);
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
        }
    }

    private void invokeActivity(Class activityClass, Bundle bundle) {
        try {
            Intent intent = new Intent(getApplicationContext(), activityClass);
            intent.putExtra(Constants.KEY_REMOTE_USER_DATA, bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(intent);
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
        }
    }

    public void showToast(String msg) {
        Toast.makeText(AgoraService.this, msg, Toast.LENGTH_SHORT).show();
    }
}
