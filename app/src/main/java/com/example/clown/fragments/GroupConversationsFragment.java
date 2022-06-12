package com.example.clown.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.clown.activities.MainActivity;
import com.example.clown.adapter.ConversationAdapter;
import com.example.clown.databinding.FragmentGroupConversationsBinding;

public class GroupConversationsFragment extends Fragment {
    private FragmentGroupConversationsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Initialize(inflater, container);

        return binding.getRoot();
    }

    private void Initialize(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentGroupConversationsBinding.inflate(inflater, container, false);

        ConversationAdapter basicConversationAdapter = ((MainActivity) requireActivity()).getGroupConversationAdapter();
        binding.rcvGroupConversations.setAdapter(basicConversationAdapter);
    }
}