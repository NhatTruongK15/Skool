package com.example.clown.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.databinding.ItemConteainerAddGroupMemberBinding;
import com.example.clown.listeners.GroupChatListener;
import com.example.clown.models.Conversation;
import com.example.clown.models.User;

import java.util.List;

public class GroupAddMemberAdapter extends RecyclerView.Adapter<GroupAddMemberAdapter.ViewHolder> {
    private final List<User> users;
    private final IGroupAddMemberItemListener listener;

    public GroupAddMemberAdapter(List<User> users, IGroupAddMemberItemListener listener) {
        this.users = users;
        this.listener = listener;
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
            binding.getRoot().setOnClickListener(v -> onItemClicked(user));
        }

        private void onItemClicked(User user) {
            if (listener != null) listener.onGroupAddMemberClicked(user);
        }
    }
    public interface IGroupAddMemberItemListener {
        void onGroupAddMemberClicked(User user);
    }
}
