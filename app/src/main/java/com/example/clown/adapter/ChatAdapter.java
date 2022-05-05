package com.example.clown.adapter;

import static com.example.clown.utilities.Constants.HD_RES;
import static com.example.clown.utilities.Constants.HD_RES_860;
import static com.example.clown.utilities.Constants.PIC_HOLDER;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.R;
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
            if(chatMessage==null){
                binding.textMessage.setVisibility(View.INVISIBLE);
            }
            else{
                binding.textMessage.setVisibility(View.VISIBLE);
            }

            if(chatMessage.message_img!=null){
                binding.textMessage.setVisibility(View.INVISIBLE);
                binding.messContainer.setMinimumHeight(PIC_HOLDER+10);
                binding.imgMessage.setMinimumHeight(PIC_HOLDER);
                binding.imgMessage.setMinimumWidth(PIC_HOLDER);
                binding.imgMessage.setImageBitmap(chatMessage.message_img);
                binding.textMessage.setText(null);
                chatMessage.message_img=null;
            }
            else{
                binding.textMessage.setVisibility(View.VISIBLE);
            }
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
            if(chatMessage.message_img!=null){
                binding.textMessage.setVisibility(View.INVISIBLE);
                binding.messContainer.setMinimumHeight(810);
                binding.imgMessage.setMinimumHeight(800);
                binding.imgMessage.setMinimumWidth(800);
                binding.imgMessage.setImageBitmap(chatMessage.message_img);
                binding.textMessage.setText(null);
                chatMessage.message_img=null;
            }
            else{
                binding.textMessage.setVisibility(View.VISIBLE);
            }
            binding.textDateTime.setText(chatMessage.dateTime);
            if(receiverProfileImage!=null)
            {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }

        }
    }
}
