package com.example.clown.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.clown.adapter.PendingRequestAdapter;
import com.example.clown.databinding.FragmentPendingRequestsBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PendingRequestsFragment extends Fragment {
    public static String TAG = PendingRequestsFragment.class.getName();

    private FragmentPendingRequestsBinding binding;

    private final User mCurrentUser;
    private final List<User> mPendingUsersList;
    private final PendingRequestAdapter mPendingRequestsAdapter;

    public PendingRequestsFragment(User currentUser) {
        mCurrentUser = currentUser;
        mPendingUsersList = new ArrayList<>();
        mPendingRequestsAdapter = new PendingRequestAdapter(getContext(), mPendingUsersList);
        mPendingRequestsAdapter.setCurrentUser(mCurrentUser);
    }

    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
        if (error != null) {
            Log.e(TAG, error.getMessage());
            return;
        }

        if (value != null) {
            for (DocumentChange docChange : value.getDocumentChanges()) {
                switch (docChange.getType()) {
                    case ADDED:
                        mPendingUsersList.add(docChange.getDocument().toObject(User.class));
                        mPendingRequestsAdapter.notifyItemInserted(docChange.getNewIndex());
                        break;

                    case REMOVED:
                        mPendingUsersList.remove(docChange.getDocument().toObject(User.class));
                        mPendingRequestsAdapter.notifyItemRemoved(docChange.getOldIndex());
                        break;

                    case MODIFIED:
                        break;
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPendingRequestsBinding.inflate(inflater, container, false);
        binding.pendingRequestsRecyclerView.setAdapter(mPendingRequestsAdapter);
        setUpFireStoreListener();
        return binding.getRoot();
    }

    private void setUpFireStoreListener() {
        if (mCurrentUser.getReceivedRequests().isEmpty()) return;

        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .whereIn(Constants.KEY_ID, mCurrentUser.getReceivedRequests())
                .addSnapshotListener(this::onEvent);
    }
}