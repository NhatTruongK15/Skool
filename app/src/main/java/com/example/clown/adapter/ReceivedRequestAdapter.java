package com.example.clown.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.databinding.ItemPendingRequestBinding;
import com.example.clown.models.User;

import java.util.ArrayList;
import java.util.List;

public class ReceivedRequestAdapter extends RecyclerView.Adapter<ReceivedRequestAdapter.PendingRequestViewHolder> {
    private final List<User> mPendingUsersList;
    private final List<String> mFriendIDs;
    private final Context mContext;
    private final String mUserID;

    public ReceivedRequestAdapter(Context context, List<User> dataSet, String currentUserID, List<String> friendIDs) {
        mContext = context;
        mPendingUsersList = dataSet;
        mUserID = currentUserID;
        mFriendIDs = friendIDs;
    }

    @NonNull
    @Override
    public ReceivedRequestAdapter.PendingRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PendingRequestViewHolder(ItemPendingRequestBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReceivedRequestAdapter.PendingRequestViewHolder holder, int position) {
        User onUser = mPendingUsersList.get(position);
        holder.setBinding(onUser);
    }

    @Override
    public int getItemCount() {
        return mPendingUsersList.size();
    }

    public class PendingRequestViewHolder extends RecyclerView.ViewHolder {
        private final ItemPendingRequestBinding binding;

        public PendingRequestViewHolder(@NonNull ItemPendingRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setBinding(User onUser) {
            binding.rivAvatar.setImageBitmap(onUser.getBitmapAvatar());
            binding.tvUsername.setText(onUser.getUsername());
            binding.tvMutualFriendsCount.setText(mutualFriendsCount(onUser.getFriends()));

            binding.getRoot().setOnClickListener(v -> checkProfile(onUser));
            binding.btnAccept.setOnClickListener(v -> friendAccept(onUser));
            binding.btnDecline.setOnClickListener(v -> friendDecline(onUser));
        }

        private String mutualFriendsCount(List<String> friendsList) {
            if (friendsList == null || friendsList.isEmpty()) return "0 mutual friend";
            List<String> mutualFriends = new ArrayList<>(mFriendIDs);
            mutualFriends.retainAll(friendsList);

            return mutualFriends.size() > 1 ?
                    mutualFriends.size() + " mutual friends" :
                    mutualFriends.size() + " mutual friend";
        }

        private void checkProfile(User onUser) {

        }

        private void friendDecline(User onUser) {
            // Remove pending requests from local

            // Remove pending requests from fire store
        }

        private void friendAccept(User onUser) {
            // Remove pending requests from local

            // Remove pending requests from fire store
        }
    }
}
