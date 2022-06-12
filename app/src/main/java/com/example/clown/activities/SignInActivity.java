package com.example.clown.activities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.example.clown.databinding.ActivitySignInBinding;
import com.example.clown.models.User;
import com.example.clown.services.UserListenerService;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class SignInActivity extends BaseActivity {
    private static final String TAG = SignInActivity.class.getName();

    private ActivitySignInBinding mBinding;
    private String mEmailOrPhoneNumber;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();

        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the app has been signed in
        // then skip the sign in process
        //if (mPreferenceManager.getUser() != null) onAlreadySignedIn();
    }

    private void onAlreadySignedIn() {
        Log.e(TAG, "Already signed in!");

        mCurrentUser.Clone(mPreferenceManager.getUser());

        if (!isJobServiceOn(getApplicationContext())) startAppService();

        startActivity(TAG, MainActivity.class, null);

        finish();
    }

    private void Init() {
        mBinding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mPreferenceManager = new PreferenceManager(getApplicationContext());
        mCurrentUser = new User();
    }

    private void setListeners() {
        mBinding.textCreateNewAccount.setOnClickListener(v -> startActivity(TAG, SignUpActivity.class, null));
        mBinding.forgetPasswordText.setOnClickListener(v -> startActivity(TAG, ResetPasswordActivity.class, null));
        mBinding.buttonSignIn.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        if (isValidSignInDetails()) {
            loading(true);

            // Try to sign in
            FirebaseFirestore
                    .getInstance()
                    .collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_PHONE_NUMBER, mEmailOrPhoneNumber)
                    .whereEqualTo(Constants.KEY_PASSWORD, mPassword)
                    .get()
                    .addOnCompleteListener(this::onSignIn);
        }
    }

    private Boolean isValidSignInDetails() {
        mEmailOrPhoneNumber = mBinding.inputPhoneNumberOrEmail.getText().toString().trim();
        mPassword = mBinding.inputPassword.getText().toString().trim();

        if (mEmailOrPhoneNumber.isEmpty()) {
            showToast(Constants.TOAST_EMPTY_EMAIL_OR_PHONE_NUMBER);
            return false;
        }

        if (!Patterns.PHONE.matcher(mEmailOrPhoneNumber).matches() &&
                !Patterns.EMAIL_ADDRESS.matcher(mEmailOrPhoneNumber).matches()) {
            showToast(Constants.TOAST_INVALID_EMAIL_OR_PHONE_NUMBER);
            return false;
        }

        if (mPassword.isEmpty()) {
            showToast(Constants.TOAST_EMPTY_PASSWORD);
            return false;
        }

        return true;
    }

    private void loading(Boolean isLoading) {
        int nPBVisibility = isLoading ? View.VISIBLE : View.INVISIBLE;
        int nBtnVisibility = isLoading ? View.INVISIBLE : View.VISIBLE;

        // Invoke loading UI
        mBinding.progressBar.setVisibility(nPBVisibility);
        mBinding.buttonSignIn.setVisibility(nBtnVisibility);
    }

    private void onSignIn(Task<QuerySnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() != 0) {
            Log.e(TAG, "Signed in successfully!");

            // Get validated user
            User validatedUser = task.getResult().getDocuments().get(0).toObject(User.class);
            mPreferenceManager.putUser(validatedUser);
            mCurrentUser.Clone(Objects.requireNonNull(validatedUser));

            // Start app's background listener
            startAppService();

            // Go to main activity
            startActivity(TAG, MainActivity.class, null);
            showToast(Constants.TOAST_SIGN_IN_SUCCESSFULLY);

            finish();

            return;
        }

        Log.e(TAG, "Signed in failed!");
        loading(false);
        showToast(Constants.TOAST_SIGN_IN_FAILED);
    }

    private static boolean isJobServiceOn(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE ) ;

        boolean hasBeenScheduled = false ;

        for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
            if ( jobInfo.getId() == Constants.KEY_SERVICE_ID ) {
                hasBeenScheduled = true ;
                break ;
            }
        }

        return hasBeenScheduled ;
    }

    private void startAppService() {
        ComponentName componentName = new ComponentName(getApplicationContext(), UserListenerService.class);
        JobInfo jobInfo = new JobInfo.Builder(Constants.KEY_SERVICE_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true)
                .build();
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }
}