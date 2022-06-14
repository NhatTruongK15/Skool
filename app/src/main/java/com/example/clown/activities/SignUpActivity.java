package com.example.clown.activities;

import static com.example.clown.utilities.Constants.HD_RES;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.example.clown.R;
import com.example.clown.models.User;
import com.example.clown.services.UserListenerService;
import com.example.clown.utilities.Constants;
import com.example.clown.databinding.ActivitySignUpBinding;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SignUpActivity extends BaseActivity {
    private static final String TAG = SignUpActivity.class.getName();

    private static final String PHONE_NUMBER_PREFIX = "+84";
    private static final float PREFERRED_WIDTH = HD_RES;
    private static final float PREFERRED_HEIGHT = HD_RES;

    private ActivitySignUpBinding binding;
    private String mEncodedAvatar;
    private String mVerificationId;
    private ProgressDialog pd;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Init();

        setListeners();
    }

    private void Init() {
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //set up progressDialog
        pd = new ProgressDialog(this);
        pd.setTitle("Please wait...");
        pd.setCanceledOnTouchOutside(false);

        //setup views
        binding.verificationLayout.setVisibility(View.GONE);
        binding.userInfoLayout.setVisibility(View.VISIBLE);

        //set default avatar
        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_avatar);
        mEncodedAvatar = encodeImageSuper(icon);
    }

    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.layoutImage.setOnClickListener(v -> pickImage());
        binding.buttonSignUp.setOnClickListener(v -> signUp());
        binding.confirmButton.setOnClickListener(v -> onSubmitVerificationCode());
        binding.resendContent.setOnClickListener(v -> resendCode());
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PickImage.launch(intent);
    }

    private void signUp() {
        loading(true);

        if (!isValidSignUpDetails()) {
            loading(false);
            return;
        }

        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_PHONE_NUMBER, binding.inputPhoneNumb.getText().toString())
                .get()
                .addOnCompleteListener(this::onExistedPhoneNumberCheckComplete);
    }

    private void onSubmitVerificationCode() {
        pd.setMessage("Verifying smsCode");
        pd.show();

        String smsCode = binding.verificationCodeEditText.getText().toString();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, smsCode);
        signInWithPhoneAuthCredential(credential);
    }

    private Boolean isValidSignUpDetails() {
        if (mEncodedAvatar == null) {
            showToast("Select profile image");
            return false;
        }

        if (binding.inputName.getText().toString().trim().isEmpty() ||
                binding.inputEmail.getText().toString().trim().isEmpty() ||
                binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast(Constants.TOAST_PLEASE_FILL_IN_ALL_INFORMATION);
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast(Constants.TOAST_INVALID_EMAIL);
            return false;
        }

        if (binding.inputPassword.getText().toString().trim().length() < 6) {
            showToast(Constants.TOAST_WEAK_PASSWORD);
            return false;
        }

        if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast(Constants.TOAST_UNCONFIRMED_PASSWORD);
            return false;
        }

        if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast(Constants.TOAST_PASSWORDS_UNMATCHED);
            return false;
        }

        return true;
    }

    private void loading(Boolean isLoading) {
        int nPBVisibility = isLoading ? View.VISIBLE : View.INVISIBLE;
        int nBtnVisibility = isLoading ? View.INVISIBLE : View.VISIBLE;

        // Invoke loading UI
        binding.progressBar.setVisibility(nPBVisibility);
        binding.buttonSignUp.setVisibility(nBtnVisibility);
    }

    private void signUpFirebaseAuth() {
        loading(true);

        pd.setMessage("Verifying phone number");
        pd.show();

        String phoneNumber = PHONE_NUMBER_PREFIX +
                binding.inputPhoneNumb.getText().subSequence(1, binding.inputPhoneNumb.getText().length());

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneNumber)                        // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS)          // Timeout and unit
                .setActivity(this)                                  // Activity (for callback binding)
                .setCallbacks(mStateChangedVerifyCallback)         // OnVerificationStateChangedCallbacks
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        pd.setMessage("Logging in");
        pd.show();

        FirebaseAuth
                .getInstance()
                .signInWithCredential(credential)
                .addOnCompleteListener(this, this::onPhoneAuthSignInCompleted);
    }

    private void resendCode() {
        pd.setMessage("Resending smsCode");
        pd.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setPhoneNumber("+84" + binding.inputPhoneNumb.getText().subSequence(1, binding.inputPhoneNumb.getText().length()))    // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mStateChangedVerifyCallback)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(mResendToken)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
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

    //region IMAGE FORMATTING SUPPORT FUNCTIONS
    private String encodeImageSuper(Bitmap bitmap) {
        //int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(resizeBitmap(bitmap), HD_RES, HD_RES, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 95, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = PREFERRED_WIDTH / width;
        float scaleHeight = PREFERRED_HEIGHT / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, width, height, matrix, false);
        bitmap.recycle();
        return resizedBitmap;
    }

    private final ActivityResultLauncher<Intent> PickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            mEncodedAvatar = encodeImageSuper(bitmap);

                            byte [] encodeByte = Base64.decode(mEncodedAvatar,Base64.DEFAULT);
                            bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

                            binding.imageProfile.setImageBitmap(bitmap);

                            //binding.textAddImage.setVisibility(View.GONE);
                        }
                        catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
    //endregion

    //region CALLBACKS FUNCTIONS
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mStateChangedVerifyCallback
            = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            Log.e(TAG, "Verification Completed!" + phoneAuthCredential);

            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.

            pd.dismiss();
            loading(false);

            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Log.e(TAG, "onVerificationFailed", e);

            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid

            // Show a message and update the UI
            pd.dismiss();
            loading(false);

            if (e instanceof FirebaseTooManyRequestsException)
                showToast(Constants.TOAST_OVERFLOW_REQUESTS);
            else
                showToast(e.getMessage());
        }

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(verificationId, forceResendingToken);
            Log.e(TAG, "onCodeSent: " + verificationId);

            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.

            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = forceResendingToken;

            pd.dismiss();
            loading(false);

            binding.verificationLayout.setVisibility(View.VISIBLE);
            binding.userInfoLayout.setVisibility(View.GONE);
            binding.resendContent.setText(R.string.missing_sms_code);
            binding.verificationCodeEditText.setHint(R.string.verification_code_hint);
        }
    };

    public void onExistedPhoneNumberCheckComplete(@NonNull Task<QuerySnapshot> task) {
        loading(false);

        if (task.isSuccessful()) {
            if (!task.getResult().isEmpty())
                showToast(Constants.TOAST_ACCOUNT_ALREADY_SIGNED_UP);
            else
                signUpFirebaseAuth();
        } else
            showToast(Objects.requireNonNull(task.getException()).getMessage());
    }

    private void onPhoneAuthSignInCompleted(@NonNull Task<AuthResult> task) {
        if (!task.isSuccessful()) {
            Log.e(TAG, "signInWithCredential:failure", task.getException());

            // Sign in failed, display a message and update the UI
            pd.dismiss();

            // The verification code entered was invalid
            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                showToast(Constants.TOAST_INCORRECT_VERIFY_CODE);
        } else {
            // Sign in success, update UI with the signed-in user's information
            Log.e(TAG, "signInWithCredential successfully!");

            //FirebaseUser user = task.getResult().getUser();
            // Update UI

            // Sign in success, update UI with the signed-in user's information
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // realtime firebase db

            Log.e("register state", "createUserWithEmail:success");
            pd.dismiss();

            //create newUser (containing user's information)
            User newUser = new User();
            newUser.setID(Objects.requireNonNull(currentUser).getUid());
            newUser.setUsername(binding.inputName.getText().toString());
            newUser.setPhoneNumber(binding.inputPhoneNumb.getText().toString());
            newUser.setPassword(binding.inputPassword.getText().toString());
            newUser.setAvatar(mEncodedAvatar);
            newUser.setEmail(binding.inputEmail.getText().toString());

            FirebaseFirestore
                    .getInstance()
                    .collection(Constants.KEY_COLLECTION_USERS)
                    .document(currentUser.getUid())
                    .set(newUser)
                    .addOnSuccessListener(documentReference -> {
                        loading(false);

                        // Set app's user
                        mPreferenceManager.putUser(newUser);
                        mCurrentUser.Clone(newUser);

                        // Start app's background listener
                        startAppService();

                        // Go to main activity
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);

                        showToast(Constants.TOAST_SIGN_UP_SUCCESSFULLY);
                        finish();
                    })
                    .addOnFailureListener(exception -> {
                        loading(false);
                        showToast(exception.getMessage());
                    });
        }
    }
    //endregion
}