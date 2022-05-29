package com.example.clown.activities;

import static com.example.clown.utilities.Constants.HD_RES;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.clown.databinding.ActivityMyProfileBinding;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MyProfileActivity extends AppCompatActivity {

    ActivityMyProfileBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String encodedImage;

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

        binding.ChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditUserProfileActivity.class);
                intent.putExtra(Constants.KEY_EDIT_PROFILETYPE, "email");
                startActivity(intent);
            }
        });

        binding.ChangePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("you can't change your phone number.");
            }
        });

    }


    private void Init() {
        binding = ActivityMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

        loading(false);
        LoadUserDetails();
    }

    private void LoadUserDetails() {
        binding.name.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        binding.headerBackground.setImageBitmap(bitmap);
        binding.tvProfilePhoneNumber.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
        binding.tvProfileEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));


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
        int previewWidth = HD_RES;
        int previewHeight = HD_RES;
        //int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(resizeBitmap(bitmap), previewWidth, previewHeight, false);
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

    private final ActivityResultLauncher<Intent> PickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            //binding.headerBackground.setImageBitmap(bitmap);
                            encodedImage = encodeImageSuper(bitmap);
                            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
                            Bitmap bitmap2 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                    .update(
                                            Constants.KEY_IMAGE, encodedImage
                                    );

                            //preference manager
                            preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                            LoadUserDetails();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
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

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }
    //endregion
}