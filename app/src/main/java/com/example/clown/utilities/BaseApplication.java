package com.example.clown.utilities;

import android.app.Activity;
import android.app.Application;

import androidx.appcompat.app.AppCompatActivity;

public class BaseApplication extends Application {
    private Activity mCurrentActivity = null;

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }

    public void onCreate() {
        super.onCreate();
    }
}
