package com.example.clown.activities;

import android.os.Bundle;

import androidx.core.content.ContextCompat;

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
    public static final String TAG = ContactsActivity.class.getName();

    private ActivityContactsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();
    }

    private void Init() {
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Config Contacts activity's toolbar
        setSupportActionBar(binding.toolbarContacts);
        Objects.requireNonNull(getSupportActionBar())
                .setDisplayHomeAsUpEnabled(true);

        setUpContactsViewPager();
    }

    private void setUpContactsViewPager() {
        // Create & set up ViewPager2 Adapter
        ViewPager2Adapter contactsVP2Adapter = new ViewPager2Adapter(getSupportFragmentManager(), getLifecycle());

        contactsVP2Adapter.addFragment(new FriendsFragment());
        contactsVP2Adapter.addFragment(new PendingRequestsFragment());
        contactsVP2Adapter.addFragment(new PhoneContactsFragment());

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
            case 0:
                tab.setIcon(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_person));
                break;
            case 1:
                tab.setIcon(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_pending));
                break;
            case 2:
                tab.setIcon(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_contact_phone));
                break;
        }
    }
}