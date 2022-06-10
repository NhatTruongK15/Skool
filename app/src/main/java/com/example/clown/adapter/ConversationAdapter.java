package com.example.clown.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.activities.ChatActivity;
import com.example.clown.databinding.ItemConversationBinding;
import com.example.clown.models.Conversation;
import com.example.clown.utilities.Constants;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private final List<Conversation> mConversationIDs;
    private final Context mContext;

    public ConversationAdapter(Context context, List<Conversation> dataSet) {
        this.mConversationIDs = dataSet;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ConversationViewHolder(ItemConversationBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setBinding(mConversationIDs.get(position));
    }

    @Override
    public int getItemCount() {
        return mConversationIDs.size();
    }

    public class ConversationViewHolder extends RecyclerView.ViewHolder {
        ItemConversationBinding binding;

        ConversationViewHolder(ItemConversationBinding itemConversationBinding) {
            super(itemConversationBinding.getRoot());
            binding = itemConversationBinding;
        }

        void setBinding(Conversation onConversation) {
            binding.imageProfile.setImageBitmap(onConversation.getReceiverBitmapAvatar());
            binding.textName.setText(onConversation.getReceiverName());
            binding.textRecentMessage.setText(onConversation.getLastMessage());
            binding.getRoot().setOnClickListener(v -> beginChat(onConversation));
        }

        private void beginChat(Conversation conversation) {
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra(Constants.KEY_CONVERSATION_ID, conversation.getId());
            intent.putExtra(Constants.KEY_CONVERSATION_NAME ,conversation.getName());
            intent.putExtra(Constants.KEY_CONVERSATION_ADMINS, (Parcelable) conversation.getAdmins());
            intent.putExtra(Constants.KEY_CONVERSATION_MEMBERS, (Parcelable) conversation.getMembers());
            mContext.startActivity(intent);
        }
    }
}
