package com.example.clown.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.databinding.ItemPendingRequestBinding;
import com.example.clown.models.User;

import java.util.ArrayList;
import java.util.List;

public class ReceivedRequestAdapter extends RecyclerView.Adapter<ReceivedRequestAdapter.ViewHolder> {
    private final List<User> mPendingUsersList;
    private final List<String> mFriendIDs;
    private final IReceivedRequestItemListener requestItemListener;

    public ReceivedRequestAdapter(List<String> friendIDs, List<User> dataSet, IReceivedRequestItemListener listener) {
        mFriendIDs = friendIDs;
        mPendingUsersList = dataSet;
        requestItemListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemPendingRequestBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User onUser = mPendingUsersList.get(position);
        holder.setBinding(onUser);
    }

    @Override
    public int getItemCount() {
        return mPendingUsersList.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPendingRequestBinding binding;

        public ViewHolder(@NonNull ItemPendingRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setBinding(User onUser) {
            binding.rivAvatar.setImageBitmap(onUser.getBitmapAvatar());
            binding.tvUsername.setText(onUser.getUsername());
            binding.tvMutualFriendsCount.setText(mutualFriendsCount(onUser.getFriends()));

            binding.getRoot().setOnClickListener(v -> requestItemListener.onRequestItemClicked(onUser));
            binding.btnAccept.setOnClickListener(v -> requestItemListener.onAcceptBtnClicked(onUser));
            binding.btnDecline.setOnClickListener(v -> requestItemListener.onDeclineBtnClicked(onUser));
        }

        private String mutualFriendsCount(List<String> friendsList) {
            if (friendsList == null || friendsList.isEmpty()) return "0 mutual friend";
            List<String> mutualFriends = new ArrayList<>(mFriendIDs);
            mutualFriends.retainAll(friendsList);

            return mutualFriends.size() > 1 ?
                    mutualFriends.size() + " mutual friends" :
                    mutualFriends.size() + " mutual friend";
        }
    }
//034
    public interface IReceivedRequestItemListener {
        void onRequestItemClicked(User requester);
        void onAcceptBtnClicked(User requester);
        void onDeclineBtnClicked(User requester);
    }
}
