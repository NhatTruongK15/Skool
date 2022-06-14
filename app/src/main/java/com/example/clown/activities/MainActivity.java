package com.example.clown.activities;

import android.app.job.JobScheduler;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.example.clown.R;
import com.example.clown.adapter.ConversationAdapter;
import com.example.clown.adapter.ViewPager2Adapter;
import com.example.clown.databinding.ActivityMainBinding;
import com.example.clown.fragments.BasicConversationsFragment;
import com.example.clown.fragments.GroupConversationsFragment;
import com.example.clown.models.Conversation;
import com.example.clown.utilities.Constants;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    public static final String TAG = MainActivity.class.getName();

    private ActivityMainBinding binding;

    private List<Conversation> mBasicConversations;
    private List<Conversation> mGroupConversations;

    private ConversationAdapter mBasicConversationAdapter;
    private ConversationAdapter mGroupConversationAdapter;

    public ConversationAdapter getBasicConversationAdapter() { return mBasicConversationAdapter; }
    public ConversationAdapter getGroupConversationAdapter() { return mGroupConversationAdapter; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();

        loadConversations();

        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentUserDetails();
    }

    private void Init() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mBasicConversations = new ArrayList<>();
        mGroupConversations = new ArrayList<>();

        mBasicConversationAdapter = new ConversationAdapter(this, mBasicConversations, mCurrentUser.getID());
        mGroupConversationAdapter = new ConversationAdapter(this, mGroupConversations, mCurrentUser.getID());

        setUpConversationsViewPager();

    }

    private void setUpConversationsViewPager() {
        // Create & set up ViewPager2 Adapter
        ViewPager2Adapter conversationVP2Adapter =
                new ViewPager2Adapter(getSupportFragmentManager(), getLifecycle());

        conversationVP2Adapter.addFragment(new BasicConversationsFragment());
        conversationVP2Adapter.addFragment(new GroupConversationsFragment());

        binding.vp2Conversations.setAdapter(conversationVP2Adapter);

        // Connect ViewPager2 with TabLayout
        new TabLayoutMediator(
                binding.tlMainActivity, 
                binding.vp2Conversations,
                this::configConversationViewPager)
                .attach();
    }

    private void configConversationViewPager(TabLayout.Tab tab, int position) {
        if (position == 0)
            tab.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_chat_bubble));
        else
            tab.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_forum));
    }

    private void loadCurrentUserDetails() {
        binding.name.setText(mCurrentUser.getUsername());
        binding.imageProfile.setImageBitmap(mCurrentUser.getBitmapAvatar());
        binding.Phone.setText(mCurrentUser.getPhoneNumber());
    }

    private void loadConversations() {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereArrayContains(Constants.KEY_CONVERSATION_MEMBERS, mCurrentUser.getID())
                .addSnapshotListener(mConversationListener);
    }

    private void setListeners() {
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.imageMenu.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));
        binding.buttonContact.setOnClickListener(v -> startActivity(TAG, ContactsActivity.class, null));
        binding.llcNewGroup.setOnClickListener(v -> startActivity(TAG, NewGroupActivity.class, null));
        binding.imageProfile.setOnClickListener(v -> startActivity(TAG, MyProfileActivity.class, null));
        binding.btnFindFriend.setOnClickListener(v -> onFindFriendBtnClicked());
    }

    private void onFindFriendBtnClicked() {
        if (binding.tlMainActivity.getSelectedTabPosition() == 1)
            startActivity(TAG, NewGroupActivity.class, null);
        else
            startActivity(TAG, CommunityActivity.class, null);
    }

    private void signOut() {
        showToast(Constants.TOAST_ON_SIGN_OUT);

        mPreferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false);

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();

        startActivity(new Intent(getApplicationContext(), SignInActivity.class));

        finish();
    }

    private void addConversation(DocumentChange docChange, int newIndex) {
        Conversation newConversation = docChange.getDocument().toObject(Conversation.class);

        if (isBasicConversation(newConversation)) {
            // Got a new friend! - New basic conversation
            mBasicConversations.add(newConversation);
            mBasicConversationAdapter.notifyItemInserted(newIndex);
        } else {
            // Join a group! - New group conversation
            mGroupConversations.add(newConversation);
            mGroupConversationAdapter.notifyItemInserted(newIndex);
        }
    }

    private boolean isBasicConversation(Conversation newConversation) {
        try {
            Double.parseDouble(newConversation.getId());
            return false;
        } catch (Exception ex) {
            return true;
        }
    }

    private void removeConversation(int oldIndex) {
        mBasicConversations.remove(oldIndex);
    }

    private void updateConversation(DocumentChange docChange, int oldIndex) {
        Conversation modifiedConversation = docChange.getDocument().toObject(Conversation.class);
        mBasicConversations.set(oldIndex, modifiedConversation);
    }

    //region CALLBACKS
    private final EventListener<QuerySnapshot> mConversationListener = (value, error) -> {
        if (error != null) {
            showToast(error.getMessage());
            return;
        }

        if (value != null && !value.isEmpty())
            for (DocumentChange docChange : value.getDocumentChanges()) {
                switch (docChange.getType()) {
                    case ADDED:
                        addConversation(docChange, docChange.getNewIndex()); break;

                    case REMOVED:
                        removeConversation(docChange.getOldIndex()); break;

                    case MODIFIED:
                        updateConversation(docChange, docChange.getOldIndex()); break;
                }
            }
    };
    //endregion
}