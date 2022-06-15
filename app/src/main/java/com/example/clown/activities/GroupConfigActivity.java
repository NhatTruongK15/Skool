package com.example.clown.activities;

import static com.example.clown.activities.NewGroupActivity.NEW_GROUP;
import static com.example.clown.activities.NewGroupActivity.NEW_GROUP_MEMBERS;
import static com.example.clown.utilities.Constants.HD_RES;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.clown.R;
import com.example.clown.adapter.GroupUserAdapter;
import com.example.clown.databinding.ActivityGroupConfigBinding;
import com.example.clown.models.Conversation;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class GroupConfigActivity extends BaseActivity {
    private ActivityGroupConfigBinding binding;

    private Conversation mNewGroupConversation;

    private static final float PREFERRED_WIDTH = HD_RES;
    private static final float PREFERRED_HEIGHT = HD_RES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Init();

        setListener();
    }

    private void Init() {
        binding = ActivityGroupConfigBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle transferData = getIntent().getBundleExtra(Constants.KEY_TRANSFER_DATA);
        mNewGroupConversation = (Conversation) transferData.getSerializable(NEW_GROUP);

        // Config RecyclerView
        @SuppressWarnings("unchecked")
        List<User> mMembers = (List<User>) transferData.getSerializable(NEW_GROUP_MEMBERS);
        GroupUserAdapter userAdapter = new GroupUserAdapter(mMembers, null);
        binding.userRecyclerView.setAdapter(userAdapter);
        binding.userRecyclerView.setVisibility(View.VISIBLE);

        //set default group's name
        StringBuilder defaultName = new StringBuilder().append(mCurrentUser.getUsername()).append(", ");

        for (int i = 0; i < mMembers.size(); i++) {
            User user = mMembers.get(i);

            defaultName.append(user.getUsername());

            if (i < mMembers.size() - 1) defaultName.append(", ");
        }

        binding.etGroupName.setText(defaultName);
    }

    private void setListener() {
        binding.btnAddGroupMember.setOnClickListener(view -> createNewGroupConversation());
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.imageGroup.setOnClickListener(v -> changeGroupImage());
    }

    private void createNewGroupConversation() {
        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_avatar);
        String encodedGroupImage = encodeImageSuper(icon);

        mNewGroupConversation.setImage(encodedGroupImage);
        mNewGroupConversation.setName(binding.etGroupName.getText().toString().trim());

        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .document(mNewGroupConversation.getId())
                .set(mNewGroupConversation);

        methodSwitchToChat();
    }

    private void methodSwitchToChat() {

        Intent intent1 = new Intent(getApplicationContext(), ChatActivity.class);
        intent1.putExtra(Constants.KEY_COLLECTION_CONVERSATIONS,mNewGroupConversation);
        startActivity(intent1);
        finish();
    }


    //region Images handing references
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private String encodeImageSuper(Bitmap bitmap) {
        int previewWidth = HD_RES;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(resizeBitmap(bitmap), previewWidth, previewHeight, false);
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

        Bitmap resizedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        bitmap.recycle();

        return resizedBitmap;
    }

    private void changeGroupImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PickImage.launch(intent);
    }

    private final ActivityResultLauncher<Intent> PickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imageGroup.setImageBitmap(bitmap);
                        //encodedImage = encodeImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
    //endregion

    //region Utilities
    /*private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }*/
    //endregion
}