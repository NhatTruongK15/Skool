package com.example.clown.activities;

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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.clown.R;
import com.example.clown.adapter.UsersAdapter;
import com.example.clown.databinding.ActivityGroupBinding;
import com.example.clown.listeners.UserListener;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GroupActivity extends FirestoreBaseActivity implements UserListener {

    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private ActivityGroupBinding binding;
    private String encodedImage;
    private String documentId;
    private User currentGroup;
    private PreferenceManager preferenceManager;
    User currentUser;


    List<User> listMember = new ArrayList<>();
    List<User> listUser = new ArrayList<>();
    ArrayList<String> adminList;
    ArrayList<String> memberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();

        setListener();
    }

    private void Init() {
        binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        currentUser = preferenceManager.getUser();
        //set default avatar
        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_avatar);
        //binding.imageGroup.setImageBitmap(icon); có ai biết sao dòng này bị lỗi không :> chứ t làm bên SignUpAc dòng 159 thì đâu có bị lỗi đâu ta :> ảo thật đếi
        encodedImage = encodeImageSuper(icon);
        RecyclerViewSetUp();
    }

    private void RecyclerViewSetUp() {
        adminList = (ArrayList<String>) getIntent().getSerializableExtra(Constants.KEY_LIST_GROUP_ADMIN);
        memberList = (ArrayList<String>) getIntent().getSerializableExtra(Constants.KEY_LIST_GROUP_MEMBER);

        Bundle args = getIntent().getBundleExtra("BUNDLE");
        listMember = (ArrayList<User>) args.getSerializable("ARRAYLIST");

        if (listMember.size() > 0) {
            UsersAdapter userAdapter = new UsersAdapter(listMember, this);
            binding.userRecyclerView.setAdapter(userAdapter);
            binding.userRecyclerView.setVisibility(View.VISIBLE);
        } else {
            showError();
        }
    }

    private void setListener() {
        binding.btnAddGroupMember.setOnClickListener(view -> {
            if (binding.etGroupName.getText().toString().trim() == "") {
                Toast.makeText(GroupActivity.this, "Vui lòng nhập tên nhóm!", Toast.LENGTH_SHORT).show();
            }

            currentGroup = new User();

            //Them thong tin vao database
            Intent intent = getIntent();
            HashMap<String, Object> createGroupChat = (HashMap<String, Object>) intent.getSerializableExtra(Constants.KEY_HASH_MAP_GROUP_MEMBERS);
            documentId = (String) intent.getSerializableExtra(Constants.KEY_DOCUMENT_ID);
            createGroupChat.put(Constants.KEY_RECEIVER_AVATAR, encodedImage);
            createGroupChat.put(Constants.KEY_GROUP_NAME, binding.etGroupName.getText().toString().trim());
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(documentId).set(createGroupChat);


            //Information for receiver
            currentGroup.setID(documentId);
            currentGroup.setAvatar(encodedImage);
            currentGroup.setUsername(binding.etGroupName.getText().toString().trim());

            methodSwitchToChat();

        });
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.imageGroup.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PickImage.launch(intent);
        });
    }

    private void methodSwitchToChat() {

        Intent intent1 = new Intent(getApplicationContext(), ChatActivity.class);
        intent1.putExtra(Constants.KEY_USER, currentGroup);
        intent1.putExtra(Constants.KEY_LIST_GROUP_ADMIN, adminList);
        intent1.putExtra(Constants.KEY_LIST_GROUP_MEMBER, memberList);
        startActivity(intent1);
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
        //int previewHeight = HD_RES;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
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
                            binding.imageGroup.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
    //endregion

    //region Utilities
    private void showError() {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

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


    public void onUserClicked(User user) {

    }
    //endregion
}