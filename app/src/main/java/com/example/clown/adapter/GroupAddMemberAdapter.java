package com.example.clown.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.databinding.ItemConteainerAddGroupMemberBinding;
import com.example.clown.listeners.GroupChatListener;
import com.example.clown.listeners.UserListener;
import com.example.clown.models.User;

import java.util.List;

public class GroupAddMemberAdapter extends RecyclerView.Adapter<GroupAddMemberAdapter.UserViewHolder> {

    private final List<User> users;
    private final GroupChatListener groupChatListener;

    public GroupAddMemberAdapter(List<User> users, GroupChatListener groupChatListener) {
        this.users = users;
        this.groupChatListener = groupChatListener;
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConteainerAddGroupMemberBinding itemConteainerAddGroupMemberBinding = ItemConteainerAddGroupMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent,
                false
        );
        return new UserViewHolder(itemConteainerAddGroupMemberBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class  UserViewHolder extends RecyclerView.ViewHolder
    {
        ItemConteainerAddGroupMemberBinding binding;
        UserViewHolder(ItemConteainerAddGroupMemberBinding itemConteainerAddGroupMemberBinding)
        {
            super(itemConteainerAddGroupMemberBinding.getRoot());
            binding = itemConteainerAddGroupMemberBinding;
        }

        void setUserData(User user)
        {
            binding.textName.setText(user.getName());

            binding.imageProfile.setImageBitmap(getUserImage(user.getRawImage()));
            binding.getRoot().setOnClickListener(v -> groupChatListener.onGroupChatClicked(user));
        }
    }
    private Bitmap getUserImage(String encodeImage)
    {
        byte [] bytes = Base64.decode(encodeImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);

    }
}
