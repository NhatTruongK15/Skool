package com.example.clown.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.R;
import com.example.clown.activities.ChatActivity;
import com.example.clown.activities.FriendProfileActivity;
import com.example.clown.activities.SuggestedProfileActivity;
import com.example.clown.databinding.ItemSuggestedUserBinding;
import com.example.clown.models.Conversation;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;

import java.util.List;

public class SuggestedUserAdapter extends RecyclerView.Adapter<SuggestedUserAdapter.ViewHolder> {
    private final List<User> mSuggestedUserList;
    private User mCurrentUser;
    private final Context mContext;
    private final ISuggestedUserListener requestItemListener;


    public SuggestedUserAdapter(Context context, List<User> suggestedList, ISuggestedUserListener listener) {
        mContext = context;
        mSuggestedUserList = suggestedList;
        requestItemListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemSuggestedUserBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setBinding(mSuggestedUserList.get(position));
    }

    @Override
    public int getItemCount() {
        return mSuggestedUserList.size();
    }

    public void setCurrentUser(User currentUser) {
        mCurrentUser = currentUser;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSuggestedUserBinding binding;

        public ViewHolder(@NonNull ItemSuggestedUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setBinding(User onUser) {
            binding.rivAvatar.setImageBitmap(onUser.getBitmapAvatar());
            binding.tvUsername.setText(onUser.getUsername());
            binding.getRoot().setOnClickListener(v -> checkSuggestedProfile(onUser));

            //region Get appropriate UI for the relationship
            if (mCurrentUser.getFriends().contains(onUser.getID())) {
                binding.tvRequestSent.setText(R.string.already_friend);
                binding.tvRequestSent.setVisibility(View.VISIBLE);
                binding.btnAddFriend.setVisibility(View.GONE);
                return;
            }

            if (mCurrentUser.getSentRequests().contains(onUser.getID())) {
                binding.tvRequestSent.setText(R.string.request_sent);
                binding.tvRequestSent.setVisibility(View.VISIBLE);
                binding.btnAddFriend.setVisibility(View.GONE);
                return;
            }

            binding.tvRequestSent.setVisibility(View.GONE);
            binding.btnAddFriend.setVisibility(View.VISIBLE);

            binding.btnAddFriend.setOnClickListener(v -> requestItemListener.onSentFriendRequest(onUser));
            //endregion
        }

        private void checkSuggestedProfile(User onUser) {
            // Checking whether this user is already friend
            Class<?> targetActivity;
            if (mCurrentUser.getFriends().contains(onUser.getID())) {
                targetActivity = ChatActivity.class;
            } else
                targetActivity = SuggestedProfileActivity.class;

            // Start appropriate profile UI
            Intent intent = new Intent(mContext, targetActivity);
            if (mCurrentUser.getFriends().contains(onUser.getID())) {

                String name;
                Bitmap image;
                Conversation onConversation = new Conversation();
                name = mCurrentUser.equals(onConversation.getSenderId()) ?
                        onConversation.getReceiverName() :
                        onConversation.getSenderName();

                image = mCurrentUser.equals(onConversation.getSenderId()) ?
                        onConversation.getReceiverBitmapAvatar() :
                        onConversation.getSenderBitmapAvatar();
                onConversation.setReceiverAvatar(mCurrentUser.getAvatar());
                onConversation.setSenderName(mCurrentUser.getUsername());
                onConversation.setReceiverId(onUser.getID());
                intent.putExtra(Constants.KEY_COLLECTION_CONVERSATIONS, onConversation);

            } else
                intent.putExtra(Constants.KEY_USER, onUser);

            mContext.startActivity(intent);
        }


    }

    public interface ISuggestedUserListener {
        void onSentFriendRequest(User suggestedUser);
    }
}
