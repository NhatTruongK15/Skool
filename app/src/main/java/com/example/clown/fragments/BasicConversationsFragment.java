package com.example.clown.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.clown.databinding.FragmentBasicConversationsBinding;
import com.example.clown.models.Conversation;

import java.util.List;

public class BasicConversationsFragment extends Fragment {
    private FragmentBasicConversationsBinding binding;
    private List<Conversation> mConversations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBasicConversationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}