package com.example.clown.activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;

import android.widget.Toast;

import com.example.clown.R;
import com.example.clown.agora.RtcTokenBuilder;
import com.example.clown.databinding.ActivityCallBinding;
import com.example.clown.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class CallActivity extends BaseActivity {
    private ActivityCallBinding binding;

    private String channelId;
    private boolean isCaller;
    private boolean isConnected;
    private boolean isCameraEnable;
    private boolean isMute;

    private BroadcastReceiver mCallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startActivity(null, MainActivity.class, null);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        Init();
        joinRtcChannel();
        if (checkPermission(REQUESTED_PERMISSIONS[0]))
            configVideo();
        Log.e("[INFO] ", "CallActivity created!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rtcRelease();
        Log.e("[INFO] ", "CallActivity destroyed!");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.length < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Need permissions " + Manifest.permission.RECORD_AUDIO + "/" + Manifest.permission.CAMERA,
                        Toast.LENGTH_LONG).show();
                finish();
            } else
                configVideo();
        }
    }

    private void Init() {
        initMembers();
        initUI();
        initRtc();
        mRtcEngine.enableAudio();
        mRtcEngine.enableLocalAudio(true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACT_AGORA_LOCAL_INVITATION_FAILED);
        intentFilter.addAction(Constants.ACT_AGORA_LOCAL_INVITATION_REFUSED);
        registerReceiver(mCallReceiver, intentFilter);
    }

    private void initRtc() {
        try {
            mRtcEngine = RtcEngine.create(getApplicationContext(), Constants.AGORA_APP_ID, mRtcEngineEventHandler);
            Toast.makeText(getApplicationContext(),"AgoraRtc initialized!", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
        }
    }

    private void initMembers() {
        Intent intent = getIntent();
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        Bundle bundle = intent.getBundleExtra(Constants.KEY_REMOTE_USER_DATA);
        channelId = bundle.getString(Constants.KEY_RTC_CHANNEL_ID);
        isCaller = bundle.getBoolean(Constants.KEY_IS_CALLER);
        isConnected = !isCaller;
        isCameraEnable = false;
        isMute = false;
    }

    private void initUI() {
        setContentView(binding.getRoot());
        loadRemoteUserDetails();
        if (!isCaller) startTimer();
        setListener();
    }

    private void loadRemoteUserDetails() {
        Bundle bundle = getIntent().getBundleExtra(Constants.KEY_REMOTE_USER_DATA);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(bundle.getString(Constants.KEY_DOCUMENT_REFERENCE_ID))
                .get().addOnCompleteListener(t -> {
            if (t.isSuccessful() && t.getResult() != null && t.getResult().getData() != null) {
                String remoteName = (String) t.getResult().getData().get(Constants.KEY_USERNAME);
                String remoteImage = (String) t.getResult().getData().get(Constants.KEY_AVATAR);
                if (remoteName != null)
                    binding.tvCalleeName.setText(remoteName);

                if (remoteImage != null) {
                    byte[] bytes = Base64.decode(remoteImage, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    binding.ivAvatar.setImageBitmap(bitmap);
                }
            }
        });
    }

    private void setListener() {
        binding.ivEnableCamera.setOnClickListener(v -> configLocalVideo());
        binding.ivSwitchCamera.setOnClickListener(v -> switchCamera());
        binding.ivMute.setOnClickListener(v -> configLocalAudio());
        binding.ivEndCall.setOnClickListener(v -> endCall());
    }

    private void startTimer() {
        int count = binding.frlCallState.getChildCount();
        runOnUiThread(() -> {
            if (count > 1) binding.frlCallState.removeViewAt(count - 1);
            binding.chronoCallTime.setBase(SystemClock.elapsedRealtime() - 1000);
            binding.chronoCallTime.start();
        });
    }

    private boolean checkPermission(String permission) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
            return false;
        }
        return true;
    }

    private void configLocalAudio() {
        isMute = !isMute;

        try {
            mRtcEngine.muteLocalAudioStream(isMute);
            if (isMute)
                binding.ivMute.setImageResource(R.drawable.btn_unmute);
            else
                binding.ivMute.setImageResource(R.drawable.btn_mute);
        } catch (Exception ex) {
            Log.e("[ERROR]", ex.getMessage());
        }
    }

    private void configVideo() {
        try {
            mRtcEngine.enableVideo();
            mRtcEngine.muteLocalVideoStream(true);
            mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
            Log.e("[INFO] ", "RtcEngine video configured!");
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
        }

    }

    private void configLocalVideo() {
        isCameraEnable = !isCameraEnable;

        if (isCameraEnable) {
            runOnUiThread(() -> {
                SurfaceView surfaceView = setupVideo(0, true);
                binding.localVideoContainer.addView(surfaceView);
            });
        }
        else
            runOnUiThread(() -> binding.localVideoContainer.removeAllViews());

        mRtcEngine.muteLocalVideoStream(!isCameraEnable);
        mRtcEngine.enableLocalVideo(isCameraEnable);
    }

    private void configRemoteVideo(int uid) {
        runOnUiThread(() -> {
            SurfaceView surfaceView = setupVideo(uid, false);
            binding.remoteVideoContainer.addView(surfaceView);
        });
    }

    private SurfaceView setupVideo(int uid, boolean isLocal) {
        try {
            SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
            surfaceView.setZOrderMediaOverlay(isLocal);
            VideoCanvas videoCanvas = new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid);
            if (isLocal)
                mRtcEngine.setupLocalVideo(videoCanvas);
            else
                mRtcEngine.setupRemoteVideo(videoCanvas);
            return  surfaceView;
        } catch (Exception ex) {
            Log.e("[ERROR]", ex.getMessage());
        }
        return null;
    }

    private void switchCamera() {
        try {
            mRtcEngine.switchCamera();
        } catch (Exception ex) {
            Log.e("[ERROR]", ex.getMessage());
        }
    }

    private void endCall() {
        if (isConnected)
            leaveChannel();
        else
            cancelLocalInvitation();
        finish();
    }

    private void joinRtcChannel() {
        String mRtcToken = rtcTokenGenerator(channelId, 0);
        try {
            mRtcEngine.joinChannel(mRtcToken, channelId, null, 0);
            Log.e("[INFO] ", "RtcChannel joined!");
        } catch (Exception ex) {
            Log.e("[ERROR]", ex.getMessage());
        }
    }

    private void leaveChannel() {
        try {
            mRtcEngine.leaveChannel();
        } catch (Exception ex) {
            Log.e("[ERROR]", ex.getMessage());
        }
    }

    private void cancelLocalInvitation() {
        sendBroadcast(new Intent(Constants.ACT_AGORA_LOCAL_INVITATION_CANCELED));
    }

    private String rtcTokenGenerator(String channelId, int uid) {
        try {
            RtcTokenBuilder rtcTokenBuilder = new RtcTokenBuilder();
            int timestamp = (int)(System.currentTimeMillis() / 1000 + Constants.EXPIRED_TIME_STAMP);
            String rtcToken = rtcTokenBuilder.buildTokenWithUid(
                    Constants.AGORA_APP_ID,
                    Constants.AGORA_APP_CERTIFICATE,
                    channelId,
                    uid,
                    RtcTokenBuilder.Role.Role_Attendee,
                    timestamp);
            Log.e("[INFO] ", "Rtc Token generated!");
            return rtcToken;
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
        }
        return null;
    }

    public void rtcRelease() {
        try {
            if (mRtcEngine != null) {
                RtcEngine.destroy();
                mRtcEngine = null;
            }
            Log.e("[INFO] ", "Agora Rtc released!");
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
        }
    }

    private static final int PERMISSION_REQ_ID = 22;

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private RtcEngine mRtcEngine;

    private final IRtcEngineEventHandler mRtcEngineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            isConnected = true;
            startTimer();
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);

        }

        @Override
        public void onUserOffline(int uid, int reason) {
            super.onUserOffline(uid, reason);
            runOnUiThread(() -> binding.chronoCallTime.stop());
            leaveChannel();
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
            finish();
        }

        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed);
            switch (state) {
                case 0:
                    int count = binding.remoteVideoContainer.getChildCount();
                    runOnUiThread(() -> {
                        if (count > 1) binding.remoteVideoContainer.removeViewAt(count - 1);});
                    break;
                case 1:
                    configRemoteVideo(uid); break;
                default:
            }
        }
    };
}