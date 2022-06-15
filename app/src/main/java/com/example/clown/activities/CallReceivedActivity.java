package com.example.clown.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.example.clown.R;
import com.example.clown.databinding.ActivityCallReceivedBinding;
import com.example.clown.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

public class CallReceivedActivity extends BaseActivity {
    private ActivityCallReceivedBinding binding;
    private MediaPlayer mRingTone;

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
        setContentView(R.layout.activity_call_received);
        Init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRinging();
        unregisterReceiver(mCallReceiver);
    }

    private void Init() {
        mRingTone = startRinging();
        initUI();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACT_AGORA_REMOTE_INVITATION_FAILED);
        intentFilter.addAction(Constants.ACT_AGORA_REMOTE_INVITATION_CANCELED);
        registerReceiver(mCallReceiver, intentFilter);
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
            if (t.isSuccessful() && t.getResult() != null && t.getResult().getData() != null) {
                String remoteName = (String) t.getResult().getData().get(Constants.KEY_USERNAME);
                String remoteImage = (String) t.getResult().getData().get(Constants.KEY_AVATAR);
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
        sendBroadcast(new Intent(Constants.ACT_AGORA_REMOTE_INVITATION_ACCEPTED));
        finish();
    }

    private void refuseRemoteInvitation() {
        sendBroadcast(new Intent(Constants.ACT_AGORA_REMOTE_INVITATION_REFUSED));
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
}