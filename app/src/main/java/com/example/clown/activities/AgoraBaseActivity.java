package com.example.clown.activities;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clown.agora.AgoraService;
import com.example.clown.utilities.Constants;

public class AgoraBaseActivity extends AppCompatActivity {
    // Agora Service
    protected Messenger mMessenger = new Messenger(new AgoraBaseActivity.IncomingHandler());
    protected Messenger mService;
    protected boolean mIsBound;

    protected class IncomingHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    }

    protected ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            toAgoraService(Constants.MSG_REGISTER_CLIENT, null);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onBindingDied(ComponentName name) {
            ServiceConnection.super.onBindingDied(name);
        }
    };

    protected void toAgoraService(int msgNotification, Bundle bundle) {
        try {
            Message msg = Message.obtain(null, msgNotification, 0, 0);
            msg.replyTo = mMessenger;
            msg.setData(bundle);
            mService.send(msg);
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
        }
    }

    protected boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void initAgoraService() {
        Intent intent = new Intent(getApplicationContext(), AgoraService.class);
        if (!isServiceRunning(AgoraService.class))
            startService(intent);
    }

    protected void destroyAgoraService() {
        unbindAgoraService();
        Intent intent = new Intent(getApplicationContext(), AgoraService.class);
        stopService(intent);
    }

    protected void bindAgoraService() {
        if (!mIsBound) {
            Intent intent = new Intent(getApplicationContext(), AgoraService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
            Log.e("[INFO] ", "AgoraService bound!");
        }
    }

    protected void unbindAgoraService() {
        if (mIsBound) {
            toAgoraService(Constants.MSG_UNREGISTER_CLIENT, null);
            unbindService(mConnection);
            mIsBound = false;
            Log.e("[INFO] ", "AgoraService unbound!");
        }
    }
}