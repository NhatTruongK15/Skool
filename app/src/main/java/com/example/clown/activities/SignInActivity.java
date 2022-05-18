package com.example.clown.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.clown.agora.AgoraService;
import com.example.clown.databinding.ActivitySignInBinding;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import io.agora.rtm.RtmClient;

public class SignInActivity extends AgoraBaseActivity {
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    private ActivityResultLauncher<Intent> launcher;
    private boolean mIsLoggedIn;

    //firebase auth login
    private FirebaseAuth auth ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

            }
        });
        preferenceManager = new PreferenceManager(getApplicationContext());

        //initAgoraService();

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();
        mIsLoggedIn = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mIsLoggedIn)
            destroyAgoraService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindAgoraService();

        // check if we've already logged in
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            mIsLoggedIn = true;
            toAgoraService(Constants.MSG_AGORA_LOG_IN, null);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindAgoraService();
    }

    private void setListener() {
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                SignIn();
            }
        });

        binding.forgetPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class));
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private void SignIn() {
        loading(true);
        //old one-------------------------------------------------------
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        //-----------------------------------------------------
    //region old one login using mail
        /*
        auth.signInWithEmailAndPassword(binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Login state", "signInWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();

                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .whereEqualTo(Constants.KEY_USER_ID, auth.getCurrentUser().getUid())
                                    *//*.whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())*//*
                                    .get()
                                    .addOnCompleteListener(querySnapshotTask -> {
                                        if (querySnapshotTask.isSuccessful() && querySnapshotTask.getResult() != null && querySnapshotTask.getResult().getDocuments().size() > 0) {
                                            DocumentSnapshot documentSnapshot = querySnapshotTask.getResult().getDocuments().get(0);
                                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                            preferenceManager.putString(Constants.KEY_DOCUMENT_REFERENCE_ID, documentSnapshot.getId());
                                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getString(Constants.KEY_USER_ID));
                                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        } else {
                                            loading(false);
                                            showToast("Unable to sign in");
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Login state", "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
                */
        //endregion

        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_PHONE_NUMBER, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(querySnapshotTask -> {
                    if (querySnapshotTask.isSuccessful() && querySnapshotTask.getResult() != null
                            && querySnapshotTask.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = querySnapshotTask.getResult().getDocuments().get(0);

                        mIsLoggedIn = true;
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_DOCUMENT_REFERENCE_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_PHONE_NUMBER, documentSnapshot.getString(Constants.KEY_PHONE_NUMBER));
                        preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getString(Constants.KEY_USER_ID));
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                        String userId = documentSnapshot.getId();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.KEY_DOCUMENT_REFERENCE_ID, userId);
                        toAgoraService(Constants.MSG_AGORA_LOG_IN, bundle);

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        loading(false);
                        showToast("Unable to sign in");
                    }
                });

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    private Boolean isValidSignInDetails() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!(Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()
                || Patterns.PHONE.matcher(binding.inputEmail.getText().toString()).matches()))
        {
            showToast("Enter valid email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else {
            return true;
        }
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Register state", "signInWithCredential:success");

                            FirebaseUser user = auth.getCurrentUser();

                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .whereEqualTo(Constants.KEY_USER_ID, auth.getCurrentUser().getUid())
                                    /*.whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())*/
                                    .get()
                                    .addOnCompleteListener(querySnapshotTask -> {
                                        if (querySnapshotTask.isSuccessful() && querySnapshotTask.getResult() != null && querySnapshotTask.getResult().getDocuments().size() > 0) {
                                            DocumentSnapshot documentSnapshot = querySnapshotTask.getResult().getDocuments().get(0);
                                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                            preferenceManager.putString(Constants.KEY_DOCUMENT_REFERENCE_ID, documentSnapshot.getId());
                                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getString(Constants.KEY_USER_ID));
                                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            loading(false);
                                            showToast("Unable to sign in");
                                        }
                                    });
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("Register state", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }
}

