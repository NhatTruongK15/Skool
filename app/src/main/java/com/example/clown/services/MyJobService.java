package com.example.clown.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

public class MyJobService extends JobService {
    public static final String TAG = MyJobService.class.getName();

/*    private PreferenceManager mPreferenceManager;
    private User mCurrentUser;*/
    private boolean mIsCanceled;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e(TAG, "JobService started!");
        
        Initialize();
        
        doBackGroundWork(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e(TAG, "JobService stopped!");
        mIsCanceled = true;
        return true;
    }

    private void Initialize() {
        /*mPreferenceManager = new PreferenceManager(getApplicationContext());
        mCurrentUser = new User();
        User source = mPreferenceManager.getUser();
        mCurrentUser.Clone(source);*/
    }

    private void doBackGroundWork(JobParameters params) {
        Runnable task = () -> {
            for (int i = 0; i < 10; i++) {
                if (mIsCanceled) return;

                Log.e(TAG, "JobService's running! " + i);

                /*if (i == 9)
                    ContextCompat.getMainExecutor(getApplicationContext()).execute(() -> {
                        Toast.makeText(getApplicationContext(), mCurrentUser.getUsername(), Toast.LENGTH_LONG).show();
                    });*/

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Log.e(TAG, mCurrentUser.getID() + "***" + mCurrentUser.getPhoneNumber());
            Log.e(TAG, "JobService finished!");
            jobFinished(params, false);
        };

       Thread thread = new Thread((task));
       thread.start();
    }
}
