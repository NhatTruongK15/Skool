package com.example.clown.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.databinding.ItemFriendBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FriendAdapter2 extends RecyclerView.Adapter<FriendAdapter2.ViewHolder> {
    public static final String TAG = FriendAdapter2.class.getName();

    private final List<String> mFriends;
    private final Context mContext;

    public FriendAdapter2(Context mContext, List<String> mFriends) {
        this.mFriends = mFriends;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemFriendBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        /*User friend = getFriendInfo(mFriends.get(position));
        holder.setBinding(position);*/
    }

    /*private User getFriendInfo(String friendId) {
        FirebaseFirestore
                .getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(friendId)
                .addSnapshotListener(mFriendListener);
    }*/

    @Override
    public int getItemCount() {
        return mFriends.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFriendBinding binding;

        public ViewHolder(@NonNull ItemFriendBinding inflate) {
            super(inflate.getRoot());
            binding = inflate;
        }
    }
}
