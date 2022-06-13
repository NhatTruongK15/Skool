package com.example.clown.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.databinding.ItemContainerUserBinding;
import com.example.clown.models.User;

import java.util.List;

public class GroupUserAdapter extends RecyclerView.Adapter<GroupUserAdapter.ViewHolder> {
    private final List<User> users;
    private final IGroupUserListener mGroupUserListener;

    public GroupUserAdapter(List<User> users, IGroupUserListener listener) {
        this.users = users;
        this.mGroupUserListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemContainerUserBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerUserBinding binding;

        ViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user) {
            binding.textName.setText(user.getUsername());
            binding.textEmail.setText(user.getEmail());
            binding.imageProfile.setImageBitmap(user.getBitmapAvatar());
            binding.getRoot().setOnClickListener(v -> onItemClicked(user));
        }

        private void onItemClicked(User user) {
            if (mGroupUserListener != null) mGroupUserListener.onGroupUserClicked(user);
        }
    }

    public interface IGroupUserListener {
        void onGroupUserClicked(User user);
    }
}
