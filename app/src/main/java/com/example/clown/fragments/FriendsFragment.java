package com.example.clown.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.clown.activities.ContactsActivity;
import com.example.clown.adapter.FriendAdapter;
import com.example.clown.databinding.FragmentFriendsBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendsFragment extends Fragment {
    private static final String TAG = FriendsFragment.class.getName();

    private FragmentFriendsBinding binding;
    private List<String> mFriendIDs;
    private List<User> mFriends;
    private FriendAdapter mFriendAdapter;
    private ListenerRegistration mListenerRegister;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView!");

        Init(inflater, container);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFriendsDetails();

        broadcastReceiverRegister();

        setFireStoreListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterMembers();
    }

    //region FUNCTIONS
    private void Init(LayoutInflater inflater, ViewGroup container) {
        // Binding
        binding = FragmentFriendsBinding.inflate(inflater, container, false);

        // Data source
        User currentUser = (Objects.requireNonNull((ContactsActivity) getActivity())).getCurrentUser();
        mFriendIDs = currentUser.getFriends();

        // RecyclerView
        mFriends = new ArrayList<>();
        mFriendAdapter = new FriendAdapter(getContext(), mFriends);
        binding.friendsRecyclerView.setAdapter(mFriendAdapter);
    }

    private void broadcastReceiverRegister() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(Constants.ACT_FRIEND_ADDED);
        intentFilter.addAction(Constants.ACT_FRIEND_REMOVED);

        requireContext().registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void loadFriendsDetails() {
        if (mFriendIDs.isEmpty()) return;

        resetFriendAdapter(mFriends.size());

        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereIn(Constants.KEY_ID, mFriendIDs)
                .get()
                .addOnCompleteListener(mOnLoadFriendsCompleted);
    }

    private void resetFriendAdapter(int size) {
        mFriends.clear();
        mFriendAdapter.notifyItemRangeRemoved(0, size);
    }

    private void setFireStoreListener() {
        if (mFriendIDs.isEmpty()) return;

        mListenerRegister = FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereIn(Constants.KEY_ID, mFriendIDs)
                .addSnapshotListener(mFriendEventsListener);
    }

    private void unRegisterMembers() {
        requireContext().unregisterReceiver(mBroadcastReceiver);
        if (mListenerRegister != null) mListenerRegister.remove();
    }

    private void updateFriend(DocumentChange docChange, int pos) {
        Log.e(TAG, "Friend's modified!");
        User modifiedFriend = docChange.getDocument().toObject(User.class);
        mFriends.set(pos, modifiedFriend);
        mFriendAdapter.notifyItemChanged(pos);
    }

    private void removeFriend() {
        Log.e(TAG, "Friend's removed!");

        List<User> oldList = new ArrayList<>(mFriends);

        for (User friend : oldList)
            if (!mFriendIDs.contains(friend.getID())) {
                int i = oldList.indexOf(friend);
                mFriends.remove(i);
                mFriendAdapter.notifyItemRemoved(i);
            }
    }

    private void addFriend() {
        Log.e(TAG, "Friend's added!");

        // Load new friend details
        int nNewFriendIndex = mFriendIDs.size() - 1;
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_ID, mFriendIDs.get(nNewFriendIndex))
                .get()
                .addOnCompleteListener(mOnLoadFriendsCompleted);
    }

    protected void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    //endregion

    //region FIRE STORE CALLBACKS
    private final EventListener<QuerySnapshot> mFriendEventsListener = (value, error) -> {
        if (error != null) {
            Log.e(TAG, error.getMessage());
            return;
        }

        if (value != null) {
            for (DocumentChange docChange : value.getDocumentChanges())
                if (docChange.getType() == DocumentChange.Type.MODIFIED)
                    updateFriend(docChange, docChange.getOldIndex());
        }
    };

    private final OnCompleteListener<QuerySnapshot> mOnLoadFriendsCompleted = (task) -> {
        if (task.isSuccessful()) {
            QuerySnapshot querySnapshot = task.getResult();

            for (int i = 0; i < querySnapshot.size(); i++) {
                DocumentSnapshot docSnap = querySnapshot.getDocuments().get(i);
                User friend = docSnap.toObject(User.class);
                mFriends.add(i, friend);
                mFriendAdapter.notifyItemInserted(i);
            }
        } else
            showToast(Objects.requireNonNull(task.getException()).getMessage());

        Log.e(TAG, "Load friends completed!");
    };

    protected final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACT_FRIEND_ADDED)) addFriend();
            else removeFriend();
        }
    };
    //endregion
}