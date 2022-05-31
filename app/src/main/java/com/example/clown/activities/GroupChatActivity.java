package com.example.clown.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.clown.adapter.GroupAddMemberAdapter;
import com.example.clown.adapter.UsersGCAdapter;
import com.example.clown.databinding.ActivityGroupChatBinding;
import com.example.clown.listeners.GroupChatListener;
import com.example.clown.listeners.UserGCListener;
import com.example.clown.models.GroupUser;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class GroupChatActivity extends FirestoreBaseActivity implements GroupChatListener, UserGCListener {

    private ActivityGroupChatBinding binding;
    private PreferenceManager preferenceManager;
    HashMap<String, Object> createGroupChat;
    List<User> listMember = new ArrayList<>();
    List<User> listUser = new ArrayList<>();
    private String groupId;
    private int VIEW_OF_USERS = 1;
    private int VIEW_OF_GROUP_USERS = 0;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();
        getUsers();
        setListener();
    }

    private void Init() {
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        currentUser = preferenceManager.getUser();

    }

    private void methodSetAdapterForList(List<User> user, int viewType) {
        if (viewType == 0) {
            GroupAddMemberAdapter groupChatAdapter = new GroupAddMemberAdapter(user, GroupChatActivity.this);
            binding.listUserAdded.setAdapter(groupChatAdapter);
        } else if (viewType == 1) {
            UsersGCAdapter usersGCAdapter = new UsersGCAdapter(listUser, GroupChatActivity.this);
            binding.listFriend.setAdapter(usersGCAdapter);
        }
    }


    private void setListener() {
        binding.imageBack.setOnClickListener(view -> {
            onBackPressed();
        });
        binding.btnAdd.setOnClickListener(view -> {
            List<String> listMemberId = new ArrayList<>();
            List<String> arrTempForListAdmin = new ArrayList<>();
            arrTempForListAdmin.add(currentUser.getUserID());

            CreateGroup(listMemberId, arrTempForListAdmin);

            Intent intent = new Intent(getApplicationContext(), GroupActivity.class);
            intent.putExtra(Constants.KEY_DOCUMENT_ID, groupId);
            intent.putExtra(Constants.KEY_HASH_MAP_GROUP_MEMBERS, createGroupChat);
            intent.putExtra(Constants.KEY_LIST_GROUP_ADMIN, (ArrayList<String>) arrTempForListAdmin);
            intent.putExtra(Constants.KEY_LIST_GROUP_MEMBER, (ArrayList<String>) listMemberId);

            Bundle args = new Bundle();
            args.putSerializable("ARRAYLIST",(Serializable)listMember);
            intent.putExtra("BUNDLE",args);


            startActivity(intent);
        });
    }

    private void CreateGroup(List<String> listMemberId, List<String> arrTempForListAdmin) {
        createGroupChat = new HashMap<>();
        groupId = "" + System.currentTimeMillis();
        methodGetIdFromUser(listMemberId, listMember);
        createGroupChat.put(Constants.KEY_LAST_MESSAGE, "");
        createGroupChat.put(Constants.KEY_GROUP_ADMIN, arrTempForListAdmin);
        createGroupChat.put(Constants.KEY_GROUP_MEMBERS, listMemberId);
        createGroupChat.put(Constants.KEY_SENDER_ID, currentUser.getUserID());
        createGroupChat.put(Constants.KEY_RECEIVER_ID, groupId);
        createGroupChat.put(Constants.KEY_TIMESTAMP, new Date());
    }

    private void methodGetIdFromUser(List<String> listMemberId, List<User> usersGroupChat) {
        for (User user : usersGroupChat
        )
        {
            listMemberId.add(user.getUserID());

        }
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = currentUser.getUserID();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user = queryDocumentSnapshot.toObject(User.class);
                            user.setUserID(queryDocumentSnapshot.getString(Constants.KEY_USER_ID));
                            listUser.add(user);
                        }
                        if (listUser.size() > 0) {
                            UsersGCAdapter usersGCAdapter = new UsersGCAdapter(listUser, GroupChatActivity.this);
                            binding.listFriend.setAdapter(usersGCAdapter);
                            binding.listFriend.setVisibility(View.VISIBLE);

                        } else {
                            showError();
                        }
                    } else {
                        showError();
                    }
                });
    }

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


    @Override
    public void onGroupChatClicked(User user) {
        listMember.remove(user);
        listUser.add(user);
        methodSetAdapterForList(listUser, VIEW_OF_USERS);
        methodSetAdapterForList(listMember, VIEW_OF_GROUP_USERS);
    }

    @Override
    public void onGroupChatClicked(GroupUser groupUser) {

    }

    @Override
    public void onUserGCClicked(User user) {
        listUser.remove(user);
        methodSetAdapterForList(listUser, VIEW_OF_USERS);
        listMember.add(user);
        methodSetAdapterForList(listMember, VIEW_OF_GROUP_USERS);

    }
}