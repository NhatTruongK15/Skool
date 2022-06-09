package com.example.clown.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PreferenceUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Constants.ACT_UPDATE_CURRENT_USER)) {

        }
    }
}
