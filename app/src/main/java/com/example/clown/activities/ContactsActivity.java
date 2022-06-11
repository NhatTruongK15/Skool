package com.example.clown.activities;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.clown.R;
import com.example.clown.adapter.ViewPager2Adapter;
import com.example.clown.databinding.ActivityContactsBinding;
import com.example.clown.fragments.FriendsFragment;
import com.example.clown.fragments.PendingRequestsFragment;
import com.example.clown.fragments.PhoneContactsFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

public class ContactsActivity extends BaseActivity {
    private static final String TAG = ContactsActivity.class.getName();

    private static final int CONTACTS_ACTIVITY_REQ_CODE = 21;
    public static final int FRAGMENT_FRIENDS_POS = 0;
    private static final int FRAGMENT_PENDING_REQUESTS_POS = 1;
    private static final int FRAGMENT_PHONE_CONTACTS_POS = 2;

    private ActivityContactsBinding binding;

    public ViewPager2 getContactViewPager() { return binding.viewPager2Contacts; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();
    }

    private void Init() {
        // Binding
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Activity's permissions
        REQUESTED_PERMISSIONS = new String[] { Manifest.permission.READ_CONTACTS };
        PERMISSION_REQ_ID = CONTACTS_ACTIVITY_REQ_CODE;

        // Config Contacts activity's toolbar
        setSupportActionBar(binding.toolbarContacts);
        Objects.requireNonNull(getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);

        // ViewPager2
        setUpContactsViewPager();
    }

    private void setUpContactsViewPager() {
        Log.e(TAG, "Set up Contacts ViewPager2");

        // Create & set up ViewPager2 Adapter
        ViewPager2Adapter contactsVP2Adapter
                = new ViewPager2Adapter(getSupportFragmentManager(), getLifecycle());
        contactsVP2Adapter.addFragment(new FriendsFragment());
        contactsVP2Adapter.addFragment(new PendingRequestsFragment());
        contactsVP2Adapter.addFragment(new PhoneContactsFragment());

        // Set up ViewPager2
        binding.viewPager2Contacts.setAdapter(contactsVP2Adapter);

        // Connect ViewPager2 with TabLayout
        new TabLayoutMediator(
                binding.tabLayoutContacts,
                binding.viewPager2Contacts,
                this::configContactsViewPager)
                .attach();
    }

    private void configContactsViewPager(TabLayout.Tab tab, int position) {
        switch (position) {
            case FRAGMENT_FRIENDS_POS:
                tab.setIcon(
                        ContextCompat.getDrawable(getBaseContext(),
                        R.drawable.ic_person));
                break;
            case FRAGMENT_PENDING_REQUESTS_POS:
                tab.setIcon(
                        ContextCompat.getDrawable(getBaseContext(),
                        R.drawable.ic_pending));
                break;
            case FRAGMENT_PHONE_CONTACTS_POS:
                tab.setIcon(
                        ContextCompat.getDrawable(getBaseContext(),
                        R.drawable.ic_contact_phone));
                break;
        }
    }
}