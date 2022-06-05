package com.example.clown.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.clown.R;
import com.example.clown.adapter.FriendAdapter;
import com.example.clown.databinding.FragmentPhoneContactsBinding;
import com.example.clown.models.User;

import java.util.ArrayList;
import java.util.List;

public class PhoneContactsFragment extends Fragment {
    private FragmentPhoneContactsBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPhoneContactsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<User> list = new ArrayList<>();
        FriendAdapter friendAdapter = new FriendAdapter(getContext(), list);
        binding.phoneContactsRecyclerView.setAdapter(friendAdapter);
    }
}