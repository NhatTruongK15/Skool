package com.example.clown.utilities;

import android.app.Application;

import androidx.appcompat.app.AppCompatActivity;

public class BaseApplication extends Application {
    private AppCompatActivity mCurrentActivity = null;

    public AppCompatActivity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(AppCompatActivity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }

    public void onCreate() {
        super.onCreate();
    }
}
