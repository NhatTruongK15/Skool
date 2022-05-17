package com.example.clown.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.example.clown.R;
import com.example.clown.databinding.ActivityCallReceivedBinding;
import com.example.clown.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

public class CallReceivedActivity extends AgoraBaseActivity {
    private ActivityCallReceivedBinding binding;
    private MediaPlayer mRingTone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_received);
        Init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRinging();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindAgoraService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindAgoraService();
    }

    private void Init() {
        mMessenger = new Messenger(new CallReceivedActivity.IncomingHandler());
        mRingTone = startRinging();
        initUI();
    }

    private void initUI() {
        binding = ActivityCallReceivedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadRemoteUserDetails();
        setListener();
    }

    private void loadRemoteUserDetails() {
        Bundle bundle = getIntent().getBundleExtra(Constants.KEY_REMOTE_USER_DATA);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(bundle.getString(Constants.KEY_DOCUMENT_REFERENCE_ID))
                .get().addOnCompleteListener(t -> {
            if (t.isSuccessful() && t.getResult() != null) {
                String remoteName = (String) t.getResult().getData().get(Constants.KEY_NAME);
                String remoteImage = (String) t.getResult().getData().get(Constants.KEY_IMAGE);
                if (remoteName != null)
                    binding.tvCallerNameCallReceived.setText(remoteName);

                if (remoteImage != null) {
                    byte[] bytes = Base64.decode(remoteImage, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    binding.ivAvatar.setImageBitmap(bitmap);
                }
            }
        });
    }

    private void setListener() {
        binding.btnEndCall.setOnClickListener(v -> refuseRemoteInvitation());
        binding.btnAcceptCall.setOnClickListener(v -> acceptRemoteInvitation());
    }

    private void acceptRemoteInvitation() {
        toAgoraService(Constants.MSG_AGORA_REMOTE_INVITATION_ACCEPTED, null);
        finish();
    }

    private void refuseRemoteInvitation() {
        toAgoraService(Constants.MSG_AGORA_REMOTE_INVITATION_REFUSED, null);
        finish();
    }

    private MediaPlayer startRinging() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.incoming_call_ring);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        return mediaPlayer;
    }

    private void stopRinging() {
        if (mRingTone != null && mRingTone.isPlaying()) {
            mRingTone.stop();
            mRingTone.release();
            mRingTone = null;
        }
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constants.MSG_AGORA_REMOTE_INVITATION_CANCELED:
                case Constants.MSG_AGORA_REMOTE_INVITATION_FAILED:
                    finish();
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }
}