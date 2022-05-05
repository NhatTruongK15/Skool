package com.example.clown.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clown.R;
import com.example.clown.adapter.GroupChatAdapter;
import com.example.clown.adapter.RecentConversationAdapter;
import com.example.clown.adapter.UsersAdapter;
import com.example.clown.adapter.UsersGCAdapter;
import com.example.clown.databinding.ActivityGroupChatBinding;
import com.example.clown.databinding.ActivityMainBinding;
import com.example.clown.listeners.ConversationListener;
import com.example.clown.listeners.GroupChatListener;
import com.example.clown.listeners.UserGCListener;
import com.example.clown.listeners.UserListener;
import com.example.clown.models.ChatMessage;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class GroupChatActivity extends BaseActivity implements GroupChatListener, UserGCListener {

    private ActivityGroupChatBinding binding;
    private PreferenceManager preferenceManager;
    List<User> usersGroupChat = new ArrayList<>();
    List<User> users = new ArrayList<>();
    private int VIEW_OF_USERS = 1;
    private int VIEW_OF_GROUP_USERS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        getUsers();
        setListener();

    }



    private void getUserForGroupList(List<User> user,int viewType){


        if (viewType == 0){
            GroupChatAdapter groupChatAdapter = new GroupChatAdapter(user, GroupChatActivity.this);
            binding.listUserAdded.setAdapter(groupChatAdapter);}
        else if (viewType == 1){
            UsersGCAdapter usersGCAdapter = new UsersGCAdapter(users, GroupChatActivity.this);
            binding.listFriend.setAdapter(usersGCAdapter);}
    }


    private void setListener() {
        binding.imageBack.setOnClickListener(view -> { onBackPressed(); });
        binding.btnOk.setOnClickListener(view ->{});
    }

    private void getUsers()
    {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null)
                    {
                        for(QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if(users.size() > 0)
                        {
                            UsersGCAdapter usersGCAdapter = new UsersGCAdapter(users, GroupChatActivity.this);
                            binding.listFriend.setAdapter(usersGCAdapter);
                            binding.listFriend.setVisibility(View.VISIBLE);

                        }else{
                            showError();
                        }
                    } else
                    {
                        showError();
                    }
                });
    }

    private  void showError()
    {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }



    @Override
    public void onGroupChatClicked(User user) {
        usersGroupChat.remove(user);
        users.add(user);
        getUserForGroupList(users,VIEW_OF_USERS);
        getUserForGroupList(usersGroupChat,VIEW_OF_GROUP_USERS);
    }

    @Override
    public void onUserGCClicked(User user) {
        users.remove(user);


        getUserForGroupList(users,VIEW_OF_USERS);
        usersGroupChat.add(user);
        getUserForGroupList(usersGroupChat,VIEW_OF_GROUP_USERS);

    }
}