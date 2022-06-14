package com.example.clown.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.databinding.ItemConteainerAddGroupMemberBinding;
import com.example.clown.listeners.GroupChatListener;
import com.example.clown.models.User;

import java.util.List;

public class GroupAddMemberAdapter extends RecyclerView.Adapter<GroupAddMemberAdapter.ViewHolder> {
    private final List<User> users;
    private final GroupChatListener groupChatListener;

    public GroupAddMemberAdapter(List<User> users, GroupChatListener groupChatListener) {
        this.users = users;
        this.groupChatListener = groupChatListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemConteainerAddGroupMemberBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() { return users.size(); }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        ItemConteainerAddGroupMemberBinding binding;

        ViewHolder(ItemConteainerAddGroupMemberBinding itemConteainerAddGroupMemberBinding) {
            super(itemConteainerAddGroupMemberBinding.getRoot());
            binding = itemConteainerAddGroupMemberBinding;
        }

        void setUserData(User user) {
            binding.textName.setText(user.getUsername());

            binding.imageProfile.setImageBitmap(user.getBitmapAvatar());
            binding.getRoot().setOnClickListener(v -> groupChatListener.onGroupChatClicked(user));
        }
    }
}
