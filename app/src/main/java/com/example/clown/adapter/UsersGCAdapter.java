package com.example.clown.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.databinding.ItemContainerUserBinding;
import com.example.clown.listeners.UserGCListener;
import com.example.clown.listeners.UserListener;
import com.example.clown.models.User;

import java.util.List;


public class UsersGCAdapter extends RecyclerView.Adapter<UsersGCAdapter.UserViewHolder> {
    private final List<User> users;
    private final UserGCListener userGCListener;


    public UsersGCAdapter(List<User> users, UserGCListener userGCListener) {
        this.users = users;
        this.userGCListener = userGCListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent,
                false
        );
        return new UserViewHolder(itemContainerUserBinding);
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
        ItemContainerUserBinding binding;
        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding)
        {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user)
        {
            binding.textName.setText(user.getName());
            binding.textEmail.setText(user.getEmail());
            binding.imageProfile.setImageBitmap(getUserImage(user.getRawImage()));
            binding.getRoot().setOnClickListener(v -> {
                userGCListener.onUserGCClicked(user);
            });
        }
    }

    private Bitmap getUserImage(String encodeImage)
    {
        byte [] bytes = Base64.decode(encodeImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);

    }
}
