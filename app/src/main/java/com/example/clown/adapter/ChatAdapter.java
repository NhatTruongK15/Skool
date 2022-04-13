package com.example.clown.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.databinding.ItemContainerReceivedMessageBinding;
import com.example.clown.databinding.ItemContainerSentMessageBinding;
import com.example.clown.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage>  chatMessages;
    private final String senderId;
    private  Bitmap receiverProfileImage;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap)
    {
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, Bitmap receiverProfileImage) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.receiverProfileImage = receiverProfileImage;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT)
        {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),parent,false
                    )
            );
        }else
        {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if(getItemViewType(position) == VIEW_TYPE_SENT)
            {
                ((SentMessageViewHolder)holder).setData(chatMessages.get(position));
            } else
            {
                ((ReceivedMessageViewHolder)holder).setData(chatMessages.get(position),receiverProfileImage);
            }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId))
        {
            return VIEW_TYPE_SENT;
        }else
        {
            return  VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding)
        {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage)
        {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static  class ReceivedMessageViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding)
        {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage)
        {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            if(receiverProfileImage!=null)
            {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }

        }
    }
}
