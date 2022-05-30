package com.example.clown.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

import com.example.clown.activities.SignUpActivity;

public class MyJobService extends JobService {
    public static final String TAG = MyJobService.class.getName();
    private boolean mIsCanceled;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e(TAG, "JobService started!");
        doBackGroundWork(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e(TAG, "JobService stopped!");
        mIsCanceled = true;
        return true;
    }

    private void doBackGroundWork(JobParameters params) {
        Runnable task = () -> {
            for (int i = 0; i < 50; i++) {
                if (mIsCanceled) return;

                Log.e(TAG, "JobService's running! " + i);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);

            Log.e(TAG, "JobService finished!");
            jobFinished(params, false);
        };

       Thread thread = new Thread((task));
       thread.start();
    }
}
