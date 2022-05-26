package com.example.clown;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import static com.example.clown.utilities.Constants.HD_RES;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.ActivityMediaAndFile;
import com.example.clown.R;
import com.example.clown.activities.FileDisplayActivitiy;
import com.example.clown.adapter.ChatAdapter;
import com.example.clown.adapter.MediaAndFileAdapter;
import com.example.clown.databinding.ActitvityDisplayFileBinding;
import com.example.clown.databinding.ActivityMediaAndFileBinding;
import com.example.clown.models.ChatMessage;
import com.example.clown.models.MediaAndFile;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ActivityMediaAndFile extends AppCompatActivity {
    public ActivityMediaAndFileBinding binding;
    private FirebaseFirestore database;
    private String imagePath = "";
    private String videoPath = "";
    private String filePath = "";
    private String finame="";
    private String currentUserId;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> chatMessages;
    private String conversationId = null;
    private User receiverUser;

    private ImageView display;

    private RecyclerView recyclerView;

    private List<MediaAndFile> mediaandfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_and_file);
        binding = ActivityMediaAndFileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imageBack.setOnClickListener(v->onBackPressed());

        mediaandfile=new ArrayList<>() ;


        init();

        loadReceiverDetails();
        listenMessages();

        checkConversation();

        recyclerView=(RecyclerView) findViewById(R.id.rcvMediaAndFile);

        GridLayoutManager layoutManager=new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new MediaAndFileAdapter(this,mediaandfile));


    }
    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, currentUserId)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, currentUserId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);


    }
    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
//        showToast("pre is: "+preferenceManager.getString(Constants.KEY_USER_ID));
    }

    private void loadReceiverDetails() {

        Bundle bundle = null;
        bundle = getIntent().getExtras();
        if (bundle != null) {
            currentUserId = bundle.getString("receiverId");
        }
//        showToast("recv is: "+currentUserId);
    }
    private void checkConversation() {
        if (chatMessages.size() != 0) {
            checkConversationRemote(
                    preferenceManager.getString(Constants.KEY_DOCUMENT_REFERENCE_ID),
                    currentUserId
            );

            checkConversationRemote(
                    currentUserId,
                    preferenceManager.getString(Constants.KEY_DOCUMENT_REFERENCE_ID)
            );
        }
    }

    private void checkConversationRemote(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.videoPath=documentChange.getDocument().getString(Constants.KEY_MESSAGE_VIDEO);
                    chatMessage.filePath=documentChange.getDocument().getString(Constants.KEY_MESSAGE_FILE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.message_img_link=documentChange.getDocument().getString(Constants.KEY_MESSAGE_IMAGE_LINK);
                    chatMessage.finame=documentChange.getDocument().getString(Constants.KEY_MESSAGE_FINAME);
                    if(documentChange.getDocument().getString(Constants.KEY_MESSAGE_FINAME)!=null){
                        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
                        mediaandfile.add(new MediaAndFile(chatMessage.videoPath,chatMessage.message_img_link,chatMessage.filePath,chatMessage.finame, sdf.format(chatMessage.dateObject)));
                    }
                    else{
                    }

                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
            } else {
            }
        }
        binding.progressBar.setVisibility(View.INVISIBLE);
        if (conversationId == null) {
            checkConversation();
        }
    });

    private void openVidDisplay(){
        Context context = this;
        Intent intent = new Intent(context, FileDisplayActivitiy.class);
        intent.putExtra("vidPath", videoPath);
        intent.putExtra("finame", finame);
        intent.putExtra("fiPath", "");
        intent.putExtra("imgPath", "");
        context.startActivity(intent);
    }

    private  void openImgDisplay(){
        Context context = this;
        Intent intent = new Intent(context, FileDisplayActivitiy.class);
        intent.putExtra("imgPath", imagePath);
        intent.putExtra("finame", finame);
        intent.putExtra("fiPath", "");
        intent.putExtra("vidPath", "");
        context.startActivity(intent);
    }

    private void openFileDisplay(){
        Context context = this;
        Intent intent = new Intent(context, FileDisplayActivitiy.class);
        intent.putExtra("fiPath", filePath);
        intent.putExtra("finame", finame);
        intent.putExtra("imgPath", "");
        intent.putExtra("vidPath", "");
        context.startActivity(intent);
    }
}