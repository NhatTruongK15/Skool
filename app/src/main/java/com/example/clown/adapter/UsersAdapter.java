package com.example.clown.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.databinding.ItemContainerUserBinding;
import com.example.clown.listeners.UserListener;
import com.example.clown.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private final List<User> users;
    private final UserListener userListener;

    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent,
                false
        );
        return new ViewHolder(itemContainerUserBinding);
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
             binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }
}
