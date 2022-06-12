package com.example.clown.activities;

import static com.example.clown.utilities.Constants.HD_RES;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.example.clown.databinding.ActivityMyProfileBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MyProfileActivity extends BaseActivity {

    private ActivityMyProfileBinding binding;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();

        SetListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LoadUserDetails();
    }

    private void SetListener() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());

        binding.ChangeImage.setOnClickListener(v -> ChangeUserImage());

        binding.ChangeEmail.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditUserProfileActivity.class);
            intent.putExtra(Constants.KEY_EDIT_PROFILETYPE, "email");
            startActivity(intent);
        });

        binding.ChangePhone.setOnClickListener(v -> showToast("you can't change your phone number."));

        binding.ChangeFirstName.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditUserProfileActivity.class);
            intent.putExtra(Constants.KEY_EDIT_PROFILETYPE, "firstName");
            startActivity(intent);
        });

        binding.ChangeLastName.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditUserProfileActivity.class);
            intent.putExtra(Constants.KEY_EDIT_PROFILETYPE, "lastName");
            startActivity(intent);
        });

        binding.ChangeProfileDateOfBirth.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditUserProfileActivity.class);
            intent.putExtra(Constants.KEY_EDIT_PROFILETYPE, "dateOfBirth");
            startActivity(intent);
        });

        binding.ChangeProfileGender.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditUserProfileActivity.class);
            intent.putExtra(Constants.KEY_EDIT_PROFILETYPE, "gender");
            startActivity(intent);
        });

        binding.ChangeProfileBio.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditUserProfileActivity.class);
            intent.putExtra(Constants.KEY_EDIT_PROFILETYPE, "bio");
            startActivity(intent);
        });
    }


    private void Init() {
        binding = ActivityMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();

        loading(false);
        LoadUserDetails();
    }

    private void LoadUserDetails() {
        binding.name.setText(mCurrentUser.getUsername());
        byte[] bytes = Base64.decode(mCurrentUser.getAvatar(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        binding.headerBackground.setImageBitmap(bitmap);
        binding.tvProfilePhoneNumber.setText(mCurrentUser.getPhoneNumber());
        binding.tvProfileEmail.setText(mCurrentUser.getEmail());

        binding.tvProfileFirstName.setText(mCurrentUser.getFirstName());
        binding.tvProfileLastName.setText(mCurrentUser.getLastName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.PATTERN_DATE_ONLY_FORMATTER, Locale.CHINA);
        String temp = simpleDateFormat.format(mCurrentUser.getDateOfBirth());
        binding.tvProfileDateOfBirth.setText(temp);
        binding.tvProfileGender.setText(mCurrentUser.getGender());
        binding.tvProfileBio.setText(mCurrentUser.getBio());
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

    private String encodeImageSuper(Bitmap bitmap) {
        //int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(resizeBitmap(bitmap), HD_RES, HD_RES, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 95, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private static final float PREFERRED_WIDTH = HD_RES;
    private static final float PREFERRED_HEIGHT = HD_RES;

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

    private void ChangeUserImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PickImage.launch(intent);
    }

    protected final ActivityResultLauncher<Intent> PickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {

                    // Broadcast Receiver bind
                    Log.e("cai gi cung dc","ghi mot cai gi do cung dc");
                    IntentFilter intentFilter = new IntentFilter(Constants.ACT_UPDATE_CURRENT_USER);
                    registerReceiver(mBroadcastReceiver, intentFilter);

                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            String encodedImage = encodeImageSuper(bitmap);

                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(mCurrentUser.getID())
                                    .update(
                                            Constants.KEY_AVATAR, encodedImage
                                    );

                            //preference manager
                            User dupUser = new User();
                            dupUser.Clone(mCurrentUser);
                            dupUser.setAvatar(encodedImage);
                            mPreferenceManager.putUser(dupUser);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    unregisterReceiver(mBroadcastReceiver);
                }

            });
    //endregion

    //region Utilities

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    //endregion
}