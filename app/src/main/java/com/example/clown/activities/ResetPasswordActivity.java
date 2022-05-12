package com.example.clown.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clown.databinding.ActivityResetPasswordBinding;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class ResetPasswordActivity extends AppCompatActivity {
    private ActivityResetPasswordBinding binding;
    private PreferenceManager preferenceManager;
    private ActivityResultLauncher<Intent> launcher;

    //firebase auth login
    private FirebaseAuth auth;
    @NonNull
    String mVerificationId;
    @NonNull
    PhoneAuthProvider.ForceResendingToken mResendToken;
    String smsCode = "";
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());

        preferenceManager = new PreferenceManager(getApplicationContext());

        //set up progressdialog
        pd = new ProgressDialog(this);
        pd.setTitle("Please wait...");
        pd.setCanceledOnTouchOutside(false);
        final String TAG = "onCodeSent";

        //set up ui
        binding.waitForEnteringPhoneNumberLayout.setVisibility(View.VISIBLE);
        binding.verificationLayout.setVisibility(View.GONE);
        setContentView(binding.getRoot());


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                ResetPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                pd.dismiss();
                binding.verificationLayout.setVisibility(View.VISIBLE);
                binding.waitForEnteringPhoneNumberLayout.setVisibility(View.GONE);


            }
        };
        setListener();
    }



    private void setListener() {
        binding.backToSignIn.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignInActivity.class)));

        binding.buttonNumberConfirm.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                sendCode();
            }
        });

        binding.buttonSubmitForgetPassword.setOnClickListener(v -> {
            smsCode = binding.verificationCodeEditText.getText().toString();
            if (isValidSignInDetails()) {
                if (smsCode == "") {
                    showToast("Enter verification code");

                } else {
                    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationId, smsCode);
                    ResetPhoneAuthCredential(phoneAuthCredential);
                }

            }
        });

        binding.resendContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendCode();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void resendCode() {
        pd.setMessage("Resending smsCode");
        pd.show();


        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber("+84" + binding.inputEmailOrPhoneNumber.getText().subSequence(1, binding.inputEmailOrPhoneNumber.getText().length()))    // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(mResendToken)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void sendCode() {
        pd.setMessage("Sending smsCode");
        pd.show();
auth = FirebaseAuth.getInstance();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber("+84" + binding.inputEmailOrPhoneNumber.getText().subSequence(1, binding.inputEmailOrPhoneNumber.getText().length()))    // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void ResetPhoneAuthCredential(PhoneAuthCredential credential) {
        final String TAG = "OnSignIn";
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        pd.setMessage("Logging in");
        pd.show();
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            //FirebaseUser user = task.getResult().getUser();
                            // Update UI

                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // realtime firebase db

                            Log.d("register state", "createUserWithEmail:success");
                            pd.dismiss();

                            ResetPassword();
                            showToast("Your new password has been change to your phone number! ");
                        } else {
                            // Sign in failed, display a message and update the UI
                            pd.dismiss();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });

    }

    private void ResetPassword() {
        loading(binding.buttonSubmitForgetPassword, binding.progressBar, true);
        //old one-------------------------------------------------------
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        //-----------------------------------------------------


        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_PHONE_NUMBER, binding.inputEmailOrPhoneNumber.getText().toString())
                .get()
                .addOnCompleteListener(querySnapshotTask -> {
                    if (querySnapshotTask.isSuccessful() && querySnapshotTask.getResult() != null
                            && querySnapshotTask.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = querySnapshotTask.getResult().getDocuments().get(0);
                        documentSnapshot.getReference().update(Constants.KEY_PASSWORD, binding.inputEmailOrPhoneNumber.getText().toString());
                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(binding.buttonSubmitForgetPassword, binding.progressBar, false);
                        showToast("Unable to sign in");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loading(binding.buttonSubmitForgetPassword, binding.progressBar, false);
                showToast("No username matches.");
            }
        })

        ;

    }

    private void loading(Button button, ProgressBar progressBar, Boolean isLoading) {
        if (isLoading) {
            button.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            button.setVisibility(View.VISIBLE);
        }
    }

    private Boolean isValidSignInDetails() {
        if (binding.inputEmailOrPhoneNumber.getText().toString().trim().isEmpty()) {
            showToast("Enter email or phone number");
            return false;
        } else return true;
    }


}

