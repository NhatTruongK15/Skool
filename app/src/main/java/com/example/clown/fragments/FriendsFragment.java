package com.example.clown.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.clown.activities.ContactsActivity;
import com.example.clown.adapter.FriendAdapter;
import com.example.clown.databinding.FragmentFriendsBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendsFragment extends Fragment implements EventListener<QuerySnapshot> {
    public static final String TAG = FriendsFragment.class.getName();

    private FragmentFriendsBinding binding;
    private User mCurrentUser;
    private List<User> mFriendsList;
    private FriendAdapter mFriendAdapter;
    private boolean mIsBound;

    private final EventListener<QuerySnapshot> mFriendEventsListener = (value, error) -> {
        if (error != null) {
            Log.e(TAG, error.getMessage());
            return;
        }

        if (value != null) {
            for (DocumentChange docChange : value.getDocumentChanges()) {
                switch (docChange.getType()) {
                    case ADDED:
                        addFriend(docChange, docChange.getNewIndex()); break;

                    case REMOVED:
                        removeFriend(docChange.getOldIndex()); break;

                    case MODIFIED:
                        updateFriend(docChange, docChange.getOldIndex()); break;
                }
            }

            if (!mIsBound) {
                mFriendAdapter = new FriendAdapter(getContext(), mFriendsList);
                binding.friendsRecyclerView.setAdapter(mFriendAdapter);
                mIsBound = true;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "Friends fragment's creating view!");
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "Friends fragment's view's created!");
        friendsRecyclerViewInit();
        setUpFireStoreListener();
    }

    @Override
    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

    }

    private void Init() {
        mCurrentUser = (Objects.requireNonNull((ContactsActivity) getActivity())).getCurrentUser();
        mFriendsList = new ArrayList<>();
        mIsBound = false;
    }

    private void setUpFireStoreListener() {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereIn(Constants.KEY_USER_ID, mCurrentUser.getFriendsList())
                .addSnapshotListener(mFriendEventsListener);
    }

    private void addFriend(DocumentChange docChange, int pos) {
        Log.e(TAG, "Friend's added!");
        User addedFriend = docChange.getDocument().toObject(User.class);
        mFriendsList.add(pos, addedFriend);
    }

    private void removeFriend(int pos) {
        Log.e(TAG, "Friend's removed!");
        mFriendsList.remove(pos);
        mFriendAdapter.notifyItemRemoved(pos);
    }

    private void updateFriend(DocumentChange docChange, int pos) {
        Log.e(TAG, "Friend's modified!");
        User modifiedFriend = docChange.getDocument().toObject(User.class);
        mFriendsList.set(pos, modifiedFriend);
        mFriendAdapter.notifyItemChanged(pos);
    }

    private void friendsRecyclerViewInit() {
        Log.e(TAG, "Friends RecyclerView initialized!");
        mFriendAdapter = new FriendAdapter(getContext(), mFriendsList);
        binding.friendsRecyclerView.setAdapter(mFriendAdapter);
    }
}