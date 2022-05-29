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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.clown.R;
import com.example.clown.adapter.RecentConversationAdapter;
import com.example.clown.adapter.UsersGCAdapter;
import com.example.clown.agora.AgoraService;
import com.example.clown.databinding.ActivityMainBinding;
import com.example.clown.listeners.ConversationListener;
import com.example.clown.listeners.GroupChatListener;
import com.example.clown.models.ChatMessage;
import com.example.clown.models.GroupUser;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
import com.google.firebase.database.core.Tag;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<String> memberIdList;
    private List<String> adminIdList;
    private RecentConversationAdapter conversationAdapter;
    private FirebaseFirestore database;


    //region Agora
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
//endregion
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

    @Override
    protected void onResume() {
        super.onResume();
        loadUserDetails();
    }

    private void init() {
        conversations = new ArrayList<>();
        conversationAdapter = new RecentConversationAdapter(conversations, this);
        binding.conversationRecyclerView.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();

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
               DrawerLayout drawerLayout = binding.drawerLayout;
                drawerLayout.openDrawer(GravityCompat.START);
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
        binding.llcNewGroup.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),GroupChatActivity.class);
            intent.putExtra(Constants.KEY_DOCUMENT_REFERENCE_ID,getUser());
            startActivity(intent);
        });

        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),MyProfileActivity.class);
            intent.putExtra(Constants.KEY_DOCUMENT_REFERENCE_ID,getUser());
            startActivity(intent);
        });
    }

    private void loadUserDetails() {
        binding.name.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
        binding.Phone.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
        //binding.Email.setText(preferenceManager.getString(Constants.KEY_EMAIL));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }

    private void listenConversation() {
            loadGroupConversation();
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                    .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_DOCUMENT_REFERENCE_ID))
                    .addSnapshotListener(eventListener);
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_DOCUMENT_REFERENCE_ID))
                    .addSnapshotListener(eventListener);

    }

    public static List<String> convertObjectToList(Object obj) {
        List<String> list = new ArrayList<>();
        if (obj.getClass().isArray()) {
            list = Arrays.asList((String[])obj);
        } else if (obj instanceof Collection) {
            list = new ArrayList<>((Collection<String>)obj);
        }
        return list;
    }

    private void loadGroupConversation() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){
                        for(QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                            if(!checkGroupConversation(queryDocumentSnapshot.getId()) && queryDocumentSnapshot.get(Constants.KEY_SENDER_ID) != preferenceManager.getString(Constants.KEY_SENDER_ID))
                                continue;
                            else{
                                memberIdList =  convertObjectToList(queryDocumentSnapshot.get(Constants.KEY_GROUP_MEMBERS));
                                adminIdList =  convertObjectToList(queryDocumentSnapshot.get(Constants.KEY_GROUP_ADMIN));

                                showMessageForGroup(memberIdList,queryDocumentSnapshot);
                                showMessageForGroup(adminIdList,queryDocumentSnapshot);

                            }
                        }
                    }
                });

    }

    private void showMessageForGroup(List<String> list,QueryDocumentSnapshot queryDocumentSnapshot){
        for (String memberId: list)
        {
            if(memberId.equals(preferenceManager.getString(Constants.KEY_DOCUMENT_REFERENCE_ID))){
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                        .document(queryDocumentSnapshot.getId())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                conversations.add(isGroupConversationAdded(documentSnapshot));
                                showMessageList(conversations);
                            }
                        });
            }
        }
    }


    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    if(checkGroupConversation(documentChange.getDocument().getId())) {
                        //conversations.add(isGroupConversationAdded(documentChange));
                    } else{
                        conversations.add(isUserConversationAdded(documentChange));
                    }
                }
                else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    if(checkGroupConversation(documentChange.getDocument().getId())) {
                        conversations = isGroupConversationModified(documentChange);
                    } else{
                        conversations = isUserConversationModified(documentChange);
                    }
                }

            }
            showMessageList(conversations);
        }
    };

    private void showMessageList(List<ChatMessage> conversations){
        Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
        conversationAdapter.notifyDataSetChanged();
        binding.conversationRecyclerView.smoothScrollToPosition(0);
        binding.conversationRecyclerView.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
    }

    private List<ChatMessage> isUserConversationModified(DocumentChange documentChange) {
        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
        for (int i = 0; i < conversations.size(); i++) {
            if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                break;
            }
        }
        return conversations;
    }

    private List<ChatMessage> isGroupConversationModified(DocumentChange documentChange) {
        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
        for (int i = 0; i < conversations.size(); i++) {
            if (conversations.get(i).receiverId.equals(receiverId)) {
                conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                break;
            }
        }
        return conversations;
    }


    private ChatMessage isGroupConversationAdded(DocumentSnapshot documentSnapshot) {

        String senderId = preferenceManager.getString(Constants.KEY_DOCUMENT_REFERENCE_ID);
        String receiverId = documentSnapshot.getId();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.senderId = senderId;
        chatMessage.receiverId = receiverId;
        chatMessage.conversationImage = documentSnapshot.getString(Constants.KEY_RECEIVER_IMAGE);
        chatMessage.conversationName = documentSnapshot.getString(Constants.KEY_GROUP_NAME);
        chatMessage.conversationId = documentSnapshot.getId();
        if (documentSnapshot.getString(Constants.KEY_SENDER_ID).equals(senderId)) {
            chatMessage.message = "You: " + documentSnapshot.getString(Constants.KEY_LAST_MESSAGE);
        } else {
            chatMessage.message = documentSnapshot.getString(Constants.KEY_LAST_MESSAGE);
        }
            chatMessage.dateObject = documentSnapshot.getDate(Constants.KEY_TIMESTAMP);
        return chatMessage;
    }

    private ChatMessage isUserConversationAdded(DocumentChange documentChange) {
        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
        ChatMessage chatMessage = new ChatMessage();
        if (documentChange.getType() == DocumentChange.Type.ADDED) {
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
            chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
            chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

        }
        return chatMessage;
    }

    private boolean checkGroupConversation(String id) {
        try{
            double val = Double.parseDouble(id);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

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
        intent.putExtra(Constants.KEY_LIST_GROUP_ADMIN,(ArrayList<String>)adminIdList);
        intent.putExtra(Constants.KEY_LIST_GROUP_MEMBER,(ArrayList<String>)memberIdList);
        startActivity(intent);
    }


    @Override
    public void onGroupChatClicked(GroupUser groupUser) {

    }

    @Override
    public void onGroupChatClicked(User user) {
    }
}