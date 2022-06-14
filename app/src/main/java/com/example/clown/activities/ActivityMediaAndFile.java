package com.example.clown.activities;

import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.R;
import com.example.clown.adapter.MediaAndFileAdapter;
import com.example.clown.databinding.ActivityMediaAndFileBinding;
import com.example.clown.models.ChatMessage;
import com.example.clown.models.MediaAndFile;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityMediaAndFile extends BaseActivity {
    public ActivityMediaAndFileBinding binding;
    private FirebaseFirestore database;
    private String imagePath = "";
    private String videoPath = "";
    private String filePath = "";
    private String finame="";
    private String receivedUserId;
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

        binding.imageBack.setOnClickListener(v-> {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
        });

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
        if(checkGroupConversation(receivedUserId)) {
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID,receivedUserId)
                    .addSnapshotListener(eventGroupListener);
        } else {
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_SENDER_ID, mCurrentUser.getID())
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, receivedUserId)
                    .addSnapshotListener(eventListener);
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_SENDER_ID, receivedUserId)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, mCurrentUser.getID())
                    .addSnapshotListener(eventListener);
        }
    }

    private boolean checkGroupConversation(String conversationId) {
        try {
            Double.parseDouble(conversationId);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
    }

    private void loadReceiverDetails() {

        Bundle bundle = null;
        bundle = getIntent().getExtras();
        if (bundle != null) {
            receivedUserId = bundle.getString(Constants.KEY_RECEIVER_ID);
        }
    }
    private void checkConversation() {
        if (chatMessages.size() != 0) {
            checkConversationRemote(
                    preferenceManager.getString(Constants.KEY_DOCUMENT_REFERENCE_ID),
                    receivedUserId
            );

            checkConversationRemote(
                    receivedUserId,
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

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
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
    private final EventListener<QuerySnapshot> eventGroupListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
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
                }
            }
        }
        binding.progressBar.setVisibility(View.INVISIBLE);
        if (conversationId == null) {
            checkConversation();
        }
    });
}