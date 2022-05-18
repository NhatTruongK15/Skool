package com.example.clown.activities;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.example.clown.adapter.RecentConversationAdapter;
import com.example.clown.agora.AgoraService;
import com.example.clown.databinding.ActivityMainBinding;
import com.example.clown.listeners.ConversationListener;
import com.example.clown.listeners.GroupChatListener;
import com.example.clown.models.ChatMessage;
import com.example.clown.models.GroupUser;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends FirestoreBaseActivity implements ConversationListener, GroupChatListener {

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private User user;
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationAdapter conversationAdapter;
    private FirebaseFirestore database;

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Agora service manager
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private final Messenger mMessenger = new Messenger(new MainActivity.IncomingHandler());
    private Messenger mService;
    private boolean mIsBound;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            toAgoraService(Constants.MSG_REGISTER_CLIENT, null);
            Log.e("[INFO] ", "AgoraService to MainActivity connected!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            Log.e("[INFO] ", "AgoraService disconnected!");
        }
    };


    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    }

    private void toAgoraService(int msgNotification, Bundle bundle) {
        try {
            Message msg = Message.obtain(null, msgNotification, 0, 0);
            msg.replyTo = mMessenger;
            msg.setData(bundle);
            mService.send(msg);
        } catch (Exception ex) {
            Log.e("[ERROR] ", ex.getMessage());
        }
    }

    private void bindAgoraService() {
        if (isServiceRunning(AgoraService.class)) {
            Intent intent = new Intent(getApplicationContext(), AgoraService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
            Log.e("[INFO] ", "AgoraService bound!");
        }
    }

    private void unbindAgoraService() {
        if (mIsBound) {
            toAgoraService(Constants.MSG_UNREGISTER_CLIENT, null);
            unbindService(mConnection);
            mIsBound = false;
            Log.e("[INFO] ", "AgoraService unbound!");
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (serviceClass.getName().equals(service.service.getClassName()))
                return true;
        return false;
    }
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Agora service manager
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserDetails();
        getToken();
        setListener();
        listenConversation();
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

    private void init() {
        conversations = new ArrayList<>();
        conversationAdapter = new RecentConversationAdapter(conversations, this);
        binding.conversationRecyclerView.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();
        binding.NavMenubarLayout.setVisibility(View.GONE);
    }

    private void setListener() {
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.NewChat.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), UsersActivity.class);
            intent.putExtra(Constants.KEY_USER, getUser());
            startActivity(intent);
        });
        binding.imageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.NavMenubarLayout.getVisibility() == View.GONE) {
                    binding.NavMenubarLayout.setVisibility(View.VISIBLE);

                } else
                    binding.NavMenubarLayout.setVisibility(View.GONE);
            }
        });
        binding.NavMenubarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    binding.NavMenubarLayout.setVisibility(View.GONE);
            }
        });

        binding.buttonContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FriendAddingActivity.class);
                intent.putExtra(Constants.KEY_USER, getUser());
                startActivity(intent);
            }
        });
    }

    private void loadUserDetails() {
        binding.name.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
        binding.Phone.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
        binding.Email.setText(preferenceManager.getString(Constants.KEY_EMAIL));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }

    private void listenConversation() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_DOCUMENT_REFERENCE_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_DOCUMENT_REFERENCE_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferenceManager.getString(Constants.KEY_DOCUMENT_REFERENCE_ID).equals(senderId)) {
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_DOCUMENT_REFERENCE_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Failed"));
    }

    private void signOut() {
        showToast("Signing Out...");

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_DOCUMENT_REFERENCE_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    FirebaseAuth.getInstance().signOut();
                    toAgoraService(Constants.MSG_AGORA_LOG_OUT, null);
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Cant sign out"));
    }

    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        setUser(user);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }


    @Override
    public void onGroupChatClicked(GroupUser groupUser) {
        Intent intent = new Intent(getApplicationContext(), GChatActivity.class);
        intent.putExtra(Constants.KEY_USER, groupUser);
        startActivity(intent);
    }

    @Override
    public void onGroupChatClicked(User user) {
    }
}