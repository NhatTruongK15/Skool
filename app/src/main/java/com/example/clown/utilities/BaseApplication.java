package com.example.clown.utilities;

import android.app.Activity;
import android.app.Application;

public class BaseApplication extends Application {
    public static final String TAG = BaseApplication.class.getName();

    private Activity mCurrentActivity = null;

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) { this.mCurrentActivity = mCurrentActivity; }

    public void onCreate() {
        super.onCreate();
    }
}
