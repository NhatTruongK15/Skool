package com.example.clown.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.clown.activities.MainActivity;
import com.example.clown.adapter.ConversationAdapter;
import com.example.clown.databinding.FragmentBasicConversationsBinding;

public class BasicConversationsFragment extends Fragment {
    private FragmentBasicConversationsBinding binding;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Initialize(inflater, container);

        return binding.getRoot();
    }

    private void Initialize(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentBasicConversationsBinding.inflate(inflater, container, false);

        ConversationAdapter basicConversationAdapter = ((MainActivity) requireActivity()).getBasicConversationAdapter();
        binding.rcvBasicConversations.setAdapter(basicConversationAdapter);
    }
}