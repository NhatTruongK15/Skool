package com.example.clown.activities;

import android.os.Bundle;
import android.view.View;

import com.example.clown.adapter.GroupAddMemberAdapter;
import com.example.clown.adapter.GroupUserAdapter;
import com.example.clown.databinding.ActivityNewGroupBinding;
import com.example.clown.listeners.GroupChatListener;
import com.example.clown.models.Conversation;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NewGroupActivity extends BaseActivity implements GroupChatListener, GroupUserAdapter.IGroupUserListener {
    private static final String TAG = NewGroupActivity.class.getName();

    private ActivityNewGroupBinding binding;
    private GroupUserAdapter groupUserAdapter;

    private final List<User> listMembers = new ArrayList<>();
    private final List<User> listFriends = new ArrayList<>();

    public static final String NEW_GROUP = "newGroup";
    public static final String NEW_GROUP_MEMBERS = "newGroupMember";

    private final int VIEW_OF_USERS = 1;
    private final int VIEW_OF_GROUP_USERS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();

        loadUserFriends();

        setListener();
    }

    private void Init() {
        binding = ActivityNewGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupUserAdapter = new GroupUserAdapter(listFriends, this);
        binding.listFriend.setAdapter(groupUserAdapter);
    }

    private void loadUserFriends() {
        loading(true);

        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereIn(Constants.KEY_ID, mCurrentUser.getFriends())
                .get()
                .addOnCompleteListener(mOnLoadFriendsCompleted);
    }

    private void setListener() {
        binding.imageBack.setOnClickListener(view -> onBackPressed());
        binding.btnAdd.setOnClickListener(view -> createNewGroupConversation());
    }

    private void createNewGroupConversation() {
        // Create new group_conversation
        Conversation newGroupConversation = new Conversation();
        List<String> listMemberId = new ArrayList<>();

        newGroupConversation.setId(Long.toString(System.currentTimeMillis()));

        newGroupConversation.getAdmins().add(mCurrentUser.getID());
        newGroupConversation.getMembers().add(mCurrentUser.getID());

        getGroupMemberIDs(listMemberId, listMembers);
        newGroupConversation.getMembers().addAll(listMemberId);

        if (!isValidated(newGroupConversation)) return;

        // Prepare data transfer
        Bundle args = new Bundle();
        args.putSerializable(NEW_GROUP ,newGroupConversation);
        args.putSerializable(NEW_GROUP_MEMBERS, (Serializable) listMembers);

        // Configure new group conversation
        startActivity(TAG ,GroupConfigActivity.class, args);
    }

    private boolean isValidated(Conversation newGroupConversation) {
        if (newGroupConversation.getMembers().size() < 3) {
            showToast("Must have at least 3 people to form a group!");
            return false;
        }
        return true;
    }

    private void methodSetAdapterForList(List<User> user, int viewType) {
        if (viewType == 0) {
            GroupAddMemberAdapter groupChatAdapter = new GroupAddMemberAdapter(user, NewGroupActivity.this);
            binding.listUserAdded.setAdapter(groupChatAdapter);
        } else if (viewType == 1) {
            GroupUserAdapter groupUserAdapter = new GroupUserAdapter(listFriends, NewGroupActivity.this);
            binding.listFriend.setAdapter(groupUserAdapter);
        }
    }

    private void getGroupMemberIDs(List<String> listMemberId, List<User> usersGroupChat) {
        for (User user : usersGroupChat)
            listMemberId.add(user.getID());
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

    private final OnCompleteListener<QuerySnapshot> mOnLoadFriendsCompleted = task -> {
        loading(false);

        if (task.isSuccessful() && task.getResult() != null) {

            for (QueryDocumentSnapshot docSnap : task.getResult()) {
                User user = docSnap.toObject(User.class);
                listFriends.add(user);
                groupUserAdapter.notifyItemInserted(listFriends.size() - 1);
            }

            if (listFriends.size() > 0)
                binding.listFriend.setVisibility(View.VISIBLE);
            else
                showError();

        } else showError();
    };

    @Override
    public void onGroupChatClicked(User user) {
        listMembers.remove(user);
        listFriends.add(user);
        methodSetAdapterForList(listFriends, VIEW_OF_USERS);
        methodSetAdapterForList(listMembers, VIEW_OF_GROUP_USERS);
    }

    @Override
    public void onGroupUserClicked(User user) {
        listFriends.remove(user);
        methodSetAdapterForList(listFriends, VIEW_OF_USERS);
        listMembers.add(user);
        methodSetAdapterForList(listMembers, VIEW_OF_GROUP_USERS);
    }
}