package com.example.clown.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.activities.ChatActivity;
import com.example.clown.databinding.ItemConversationBinding;
import com.example.clown.models.Conversation;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private static final String TAG = ConversationAdapter.class.getName();

    private final List<Conversation> mConversationIDs;
    private final String mUserID;
    private final Context mContext;

    private final IConversationItemListener iConversationItemListener;
    private static final int BASIC_CONVERSATION = 0;
    private static final int GROUP_CONVERSATION = 1;

    public ConversationAdapter(Context context, List<Conversation> dataSet, String userID, IConversationItemListener iConversationItemListener) {
        Log.e(TAG, "Initialized!");
        this.mConversationIDs = dataSet;
        this.mContext = context;
        this.mUserID = userID;
        this.iConversationItemListener = iConversationItemListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemConversationBinding.inflate(inflater, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setBinding(mConversationIDs.get(position));
    }

    @Override
    public int getItemCount() {
        return mConversationIDs.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getType(mConversationIDs.get(position));
    }

    private int getType(Conversation newConversation) {
        try {
            Double.parseDouble(newConversation.getId());
            return GROUP_CONVERSATION;
        } catch (Exception ex) {
            return BASIC_CONVERSATION;
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemConversationBinding binding;
        private final int mType;

        ViewHolder(ItemConversationBinding itemConversationBinding, int type) {
            super(itemConversationBinding.getRoot());
            binding = itemConversationBinding;
            mType = type;
        }

        void setBinding(Conversation onConversation) {
            String name;
            Bitmap image;

            if (mType == BASIC_CONVERSATION) {
                name = mUserID.equals(onConversation.getSenderId()) ?
                        onConversation.getReceiverName() :
                        onConversation.getSenderName();

                image = mUserID.equals(onConversation.getSenderId()) ?
                        onConversation.getReceiverBitmapAvatar() :
                        onConversation.getSenderBitmapAvatar();
            } else {
                name = onConversation.getName();
                image = onConversation.getBitmapImage();
            }

            binding.textName.setText(name);
            binding.imageProfile.setImageBitmap(image);
            binding.textRecentMessage.setText(onConversation.getLastMessage());
            binding.getRoot().setOnClickListener(v -> iConversationItemListener.onConversationClicked(onConversation));
        }

        private void beginChat(Conversation conversation) {
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra(Constants.KEY_COLLECTION_CONVERSATIONS, conversation);
            mContext.startActivity(intent);
        }
    }

    public interface IConversationItemListener {
        void onConversationClicked(Conversation conversation);
    }


}
