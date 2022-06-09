package com.example.clown.activities;

import android.app.job.JobScheduler;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.example.clown.R;
import com.example.clown.adapter.ViewPager2Adapter;
import com.example.clown.databinding.ActivityMainBinding;
import com.example.clown.fragments.BasicConversationsFragment;
import com.example.clown.fragments.GroupConversationsFragment;
import com.example.clown.utilities.Constants;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends BaseActivity {
    public static final String TAG = MainActivity.class.getName();
    public static final int JOB_SERVICE_ID = 613;

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();

        loadCurrentUserDetails();

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

    private void setListeners() {
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.imageMenu.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));
        binding.buttonContact.setOnClickListener(v -> startActivity(TAG, ContactsActivity.class, null));
        binding.llcNewGroup.setOnClickListener(v -> startActivity(TAG, GroupChatActivity.class, null));
        binding.imageProfile.setOnClickListener(v -> startActivity(TAG, MyProfileActivity.class, null));
    }

    private void signOut() {
        showToast(Constants.TOAST_ON_SIGN_OUT);

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(Constants.KEY_SERVICE_ID);

        startActivity(new Intent(getApplicationContext(), SignInActivity.class));

        finish();
    }
}