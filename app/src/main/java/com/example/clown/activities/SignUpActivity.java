package com.example.clown.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import android.app.ProgressDialog;
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.clown.R;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.example.clown.databinding.ActivitySignUpBinding;
import com.example.clown.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AgoraBaseActivity {
    private ActivitySignUpBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    //Dialog
    ProgressDialog pd;

    // login with FirebaseAuth
/*    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference userReference;*/

    FirebaseAuth mAuth;
    @NonNull
    String mVerificationId;
    @NonNull
    PhoneAuthProvider.ForceResendingToken mResendToken;
    String smsCode = "";
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        //listener
        setListener();

    }

    private void init() {
        final String TAG = "mCallBack";

        initAgoraService();
        preferenceManager = new PreferenceManager(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();

        //set up mCallback
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

                signInWithPhoneAuthCredential(credential);
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
                binding.userInfoLayout.setVisibility(View.GONE);

                binding.resendContent.setText("didn't see smsCode?");
                binding.verificationCodeEditText.setHint("Please enter your smsCode we sent \n +84"
                        + binding.inputPhoneNumb.getText().subSequence(1, binding.inputPhoneNumb.getText().length()));
            }
        };
        //set up progressdialog
        pd = new ProgressDialog(this);
        pd.setTitle("Please wait...");
        pd.setCanceledOnTouchOutside(false);
        //setup view
        binding.verificationLayout.setVisibility(View.GONE);
        binding.userInfoLayout.setVisibility(View.VISIBLE);

        //set default avatar
        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_avatar);
        encodedImage = encodeImageSuper(icon);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindAgoraService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindAgoraService();
    }

    private void setListener() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        final String TAG = "CheckPhone";

        binding.buttonSignUp.setOnClickListener(v -> {
            loading(binding.buttonSignUp,binding.progressBar,true);
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            database.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_PHONE_NUMBER, binding.inputPhoneNumb.getText().toString())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            Log.d(TAG, "onSuccess on.");
                        }
                    })
                    .addOnCanceledListener(new OnCanceledListener() {
                        @Override
                        public void onCanceled() {
                            Log.d(TAG, "onCanceled on.");
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    loading(binding.buttonSignUp,binding.progressBar,false);
                                    showToast("already existed");
                                    return;
                                }
                                if (isValidSignUpDetails()) {
                                    signUP();
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                                if (isValidSignUpDetails()) {
                                    signUP();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error getting documents2: ", e);
                            if (isValidSignUpDetails()) {
                                signUP();
                            }
                        }
                    })
            ;


        });

        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PickImage.launch(intent);
        });

        binding.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Verifying smsCode");
                pd.show();
                smsCode = binding.verificationCodeEditText.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, smsCode);
                signInWithPhoneAuthCredential(credential);

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


    //region Images handing references
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private String encodeImageSuper(Bitmap bitmap){
        int previewWidth = 720;
        int previewHeight = 720;
        //int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(resizeBitmap(bitmap), previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,80,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private static final float PREFERRED_WIDTH = 720;
    private static final float PREFERRED_HEIGHT = 720;
    public static Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = PREFERRED_WIDTH / width;
        float scaleHeight = PREFERRED_HEIGHT / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, width, height, matrix, false);
      /*  if (resizedBitmap != null && !resizedBitmap.isRecycled()) {
            resizedBitmap.recycle();
            resizedBitmap = null;
        }*/
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
                            binding.imageProfile.setImageBitmap(bitmap);
                            //binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImageSuper(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
    //endregion

    //region Sign up references
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        final String TAG = "OnSignIn";
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        pd.setMessage("Logging in");
        pd.show();
        mAuth.signInWithCredential(credential)
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

                            //create userInput (containing user's infomations)
                            HashMap<String, Object> userInput = new HashMap<>();
                            userInput.put(Constants.KEY_NAME, binding.inputName.getText().toString());
                            userInput.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                            userInput.put(Constants.KEY_PHONE_NUMBER, binding.inputPhoneNumb.getText().toString());
                            userInput.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
                            userInput.put(Constants.KEY_IMAGE, encodedImage);
                            userInput.put(Constants.KEY_USER_ID, currentUser.getUid());
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(currentUser.getUid())
                                    .set(userInput)
                                    .addOnSuccessListener(documentReference -> {
                                        loading(binding.buttonSignUp, binding.progressBar, false);

                                        User user = new User();
                                        user.setId(currentUser.getUid());
                                        user.setName(binding.inputName.getText().toString());
                                        user.setPhoneNumber(binding.inputPhoneNumb.getText().toString());
                                        user.setRawImage(encodedImage);
                                        user.setEmail(binding.inputEmail.getText().toString());


                                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                        preferenceManager.putUser(user);
/*                                      preferenceManager.putString(Constants.KEY_USER_ID, currentUser.getUid());
                                        preferenceManager.putString(Constants.KEY_PHONE_NUMBER, binding.inputPhoneNumb.getText().toString());
                                        preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());

                                        preferenceManager.putString(Constants.KEY_DOCUMENT_REFERENCE_ID, currentUser.getUid());
                                        preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                                        preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);*/

                                        // LOGIN AGORA SERVER
                                        String userId = currentUser.getUid();
                                        Bundle bundle = new Bundle();
                                        bundle.putString(Constants.KEY_DOCUMENT_REFERENCE_ID, userId);
                                        toAgoraService(Constants.MSG_AGORA_LOG_IN, bundle);

                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);

                                    }).addOnFailureListener(exception -> {
                                        loading(binding.buttonSignUp, binding.progressBar, false);
                                        showToast(exception.getMessage());
                                    });
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

    boolean checkForPhoneNumber(String number){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.orderByChild("phone").equalTo(number).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    flag = true;
                else
                    flag = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return flag;

    }
    private void resendCode() {
        pd.setMessage("Resending smsCode");
        pd.show();


        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+84" + binding.inputPhoneNumb.getText().subSequence(1, binding.inputPhoneNumb.getText().length()))    // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(mResendToken)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // [END verify_with_code]
    }
    boolean flag = true;

    private void signUP() {
        loading(binding.buttonSignUp, binding.progressBar, true);
        pd.setMessage("Verifying phone number");
        pd.show();
        //-----------------------------------------------------------

        //--------------------------------------------------------------
        //create account by emails
        /*auth.createUserWithEmailAndPassword(binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // realtime firebase db

                            Log.d("register state", "createUserWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();

                            //create userInput (containing user's infomations)
                            HashMap<String, Object> userInput =  new HashMap<>();
                            userInput.put(Constants.KEY_NAME, binding.inputName.getText().toString());
                            userInput.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                            userInput.put(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString());
                            userInput.put(Constants.KEY_IMAGE, encodedImage);
                            userInput.put(Constants.KEY_USER_ID, currentUser.getUid());

                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .add(userInput)
                                    .addOnSuccessListener(documentReference ->{
                                        loading(false);
                                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                        preferenceManager.putString(Constants.KEY_USER_ID,currentUser.getUid() );
                                        preferenceManager.putString(Constants.KEY_DOCUMENT_REFERENCE_ID,documentReference.getId());
                                        preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                                        preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);

                                    } ).addOnFailureListener(exception -> {
                                loading(false);
                                showToast(exception.getMessage());
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("register state", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });*/

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+84" + binding.inputPhoneNumb.getText().subSequence(1, binding.inputPhoneNumb.getText().length()))    // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)        // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }


//endregion

    private Boolean isValidSignUpDetails() {
        if (encodedImage == null) {
            showToast("Select profile image");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm your password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Password && Confirm password must be same");
            return false;
        } else {
            return true;
        }
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
}