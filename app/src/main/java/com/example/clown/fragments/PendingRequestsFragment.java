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
import com.example.clown.adapter.ReceivedRequestAdapter;
import com.example.clown.databinding.FragmentPendingRequestsBinding;
import com.example.clown.models.Conversation;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.example.clown.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PendingRequestsFragment extends Fragment implements ReceivedRequestAdapter.IReceivedRequestItemListener {
    private static final String TAG = PendingRequestsFragment.class.getName();

    private FragmentPendingRequestsBinding binding;
    private User mCurrentUser;
    private List<String> mRequesterIDs;
    private List<User> mRequesters;
    private ReceivedRequestAdapter mReceivedRequestAdapter;
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
        loadRequestsDetails();

        broadcastReceiverRegister();

        setUpFireStoreListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterMembers();
    }

    //region IMPLEMENT METHODS
    @Override
    public void onRequestItemClicked(User requester) {

    }
    @Override
    public void onAcceptBtnClicked(User requester) {
        Log.e(TAG, "Request Accepted!");

        // Remove self ID from requester sent request list
        requester.getSentRequests().remove(mCurrentUser.getID());
        requester.getFriends().add(mCurrentUser.getID());

        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(requester.getID())
                .update(Constants.KEY_SENT_REQUESTS, requester.getSentRequests(),
                        Constants.KEY_FRIEND_LIST, requester.getFriends());

        // Update remote self received request list
        mCurrentUser.getReceivedRequests().remove(requester.getID());
        mCurrentUser.getFriends().add(requester.getID());

        removeRequest();

        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mCurrentUser.getID())
                .update(Constants.KEY_RECEIVED_REQUESTS, mCurrentUser.getReceivedRequests(),
                        Constants.KEY_FRIEND_LIST, mCurrentUser.getFriends());

        // Create new conversation
        DocumentReference docRef = FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .document();

        Conversation newConversation = new Conversation();

        newConversation.setId(docRef.getId());

        newConversation.getMembers().add(mCurrentUser.getID());
        newConversation.getMembers().add(requester.getID());

        newConversation.getAdmins().add(mCurrentUser.getID());
        newConversation.getAdmins().add(requester.getID());

        newConversation.setSenderId(mCurrentUser.getID());
        newConversation.setSenderName(mCurrentUser.getUsername());
        newConversation.setSenderAvatar(mCurrentUser.getAvatar());

        newConversation.setReceiverId(requester.getID());
        newConversation.setReceiverName(requester.getUsername());
        newConversation.setReceiverAvatar(requester.getAvatar());

        docRef.set(newConversation);

        // Update local self received request list
        PreferenceManager preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        preferenceManager.putUser(mCurrentUser);
    }

    @Override
    public void onDeclineBtnClicked(User requester) {
        Log.e(TAG, "Request Declined!");

        // Remove self ID from requester sent request list
        requester.getSentRequests().remove(mCurrentUser.getID());
        updateUserProperty(
                requester.getID(),
                Constants.KEY_SENT_REQUESTS,
                requester.getSentRequests());

        // Update remote self received request list
        mRequesterIDs.remove(requester.getID());
        updateUserProperty(
                mCurrentUser.getID(),
                Constants.KEY_RECEIVED_REQUESTS,
                mRequesterIDs);
        removeRequest();

        // Update local self received request list
        PreferenceManager preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        User currentUser = preferenceManager.getUser();
        currentUser.setReceivedRequests(mRequesterIDs);
        preferenceManager.putUser(currentUser);

        // Notify User
        showToast(Constants.TOAST_FRIEND_REQUEST_DECLINED);
    }
    //endregion

    //region FUNCTIONS
    private void Init(LayoutInflater inflater, ViewGroup container) {
        // Binding
        binding = FragmentPendingRequestsBinding.inflate(inflater, container, false);

        // Data Source
        mCurrentUser = (Objects.requireNonNull((ContactsActivity) getActivity())).getCurrentUser();
        List<String> mFriendIDs = mCurrentUser.getFriends();
        mRequesterIDs = mCurrentUser.getReceivedRequests();

        // RecyclerView
        mRequesters = new ArrayList<>();
        mReceivedRequestAdapter = new ReceivedRequestAdapter(mFriendIDs, mRequesters, this);
        binding.pendingRequestsRecyclerView.setAdapter(mReceivedRequestAdapter);
    }

    private void broadcastReceiverRegister() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(Constants.ACT_RECEIVED_REQUEST_ADDED);
        intentFilter.addAction(Constants.ACT_RECEIVED_REQUEST_REMOVED);

        requireContext().registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private synchronized void loadRequestsDetails() {
        if (mRequesterIDs.isEmpty()) return;

        resetRequestAdapter(mRequesters.size());

        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereIn(Constants.KEY_ID, mRequesterIDs)
                .get()
                .addOnCompleteListener(mOnLoadRequestsCompleted);
    }

    private void resetRequestAdapter(int size) {
        mRequesters.clear();
        mReceivedRequestAdapter.notifyItemRangeRemoved(0, size);
    }

    private void setUpFireStoreListener() {
        if (mRequesterIDs.isEmpty()) return;

        mListenerRegister = FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereIn(Constants.KEY_ID, mRequesterIDs)
                .addSnapshotListener(mEventListener);
    }

    private void unRegisterMembers() {
        requireContext().unregisterReceiver(mBroadcastReceiver);
        if (mListenerRegister != null) mListenerRegister.remove();
    }

    private void addRequest() {
        Log.e(TAG, "New request's added!");

        // Load new friend details
        int nNewRequestIndex = mRequesterIDs.size() - 1;
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_ID, mRequesterIDs.get(nNewRequestIndex))
                .get()
                .addOnCompleteListener(mOnLoadRequestsCompleted);
    }

    private void removeRequest() {
        Log.e(TAG, "Requests removed!");

        List<User> oldList = new ArrayList<>(mRequesters);

        for (User friend : oldList)
            if (!mRequesterIDs.contains(friend.getID())) {
                int i = oldList.indexOf(friend);
                mRequesters.remove(i);
                mReceivedRequestAdapter.notifyItemRemoved(i);
            }
    }

    private void updateRequest(DocumentChange docChange, int oldIndex) {
        Log.e(TAG, "Friend's modified!");

        User modifiedRequester = docChange.getDocument().toObject(User.class);
        mRequesters.set(oldIndex, modifiedRequester);
        mReceivedRequestAdapter.notifyItemChanged(oldIndex);
    }

    private void updateUserProperty(String userID, String field, Object value) {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(userID)
                .update(field, value);
    }

    protected void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    //endregion

    //region CALLBACKS
    private final EventListener<QuerySnapshot> mEventListener = (value, error) -> {
        if (error != null) {
            Log.e(TAG, error.getMessage());
            return;
        }

        if (value != null && !value.getMetadata().hasPendingWrites()) {
            for (DocumentChange docChange : value.getDocumentChanges())
                if (docChange.getType() == DocumentChange.Type.MODIFIED)
                    updateRequest(docChange, docChange.getOldIndex());
        }
    };

    private final OnCompleteListener<QuerySnapshot> mOnLoadRequestsCompleted = (task) -> {
        if (task.isSuccessful()) {
            QuerySnapshot querySnapshot = task.getResult();

            for (int i = 0; i < querySnapshot.size(); i++) {
                DocumentSnapshot docSnap = querySnapshot.getDocuments().get(i);
                User requester = docSnap.toObject(User.class);
                mRequesters.add(i, requester);
                mReceivedRequestAdapter.notifyItemInserted(i);
            }
        } else showToast(Objects.requireNonNull(task.getException()).getMessage());
    };

    protected final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACT_RECEIVED_REQUEST_ADDED)) addRequest();
            else removeRequest();
        }
    };
    //endregion
}