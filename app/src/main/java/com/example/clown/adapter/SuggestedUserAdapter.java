package com.example.clown.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.R;
import com.example.clown.activities.SuggestedProfileActivity;
import com.example.clown.databinding.ItemSuggestedUserBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;

import java.util.List;

public class SuggestedUserAdapter extends RecyclerView.Adapter<SuggestedUserAdapter.SuggestedUserViewHolder> {
    private final List<User> mSuggestedUserList;
    private User mCurrentUser;
    private final Context mContext;

    public SuggestedUserAdapter(Context context, List<User> suggestedList) {
        mContext = context;
        mSuggestedUserList = suggestedList;
    }

    @NonNull
    @Override
    public SuggestedUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new SuggestedUserViewHolder(ItemSuggestedUserBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestedUserViewHolder holder, int position) {
        holder.setBinding(mSuggestedUserList.get(position));
    }

    @Override
    public int getItemCount() {
        return mSuggestedUserList.size();
    }

    public void setCurrentUser(User currentUser) { mCurrentUser = currentUser; }

    public class SuggestedUserViewHolder extends RecyclerView.ViewHolder {
        private final ItemSuggestedUserBinding binding;

        public SuggestedUserViewHolder(@NonNull ItemSuggestedUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setBinding(User onUser) {
            binding.rivAvatar.setImageBitmap(onUser.getImage());
            binding.tvUsername.setText(onUser.getName());

            if (mCurrentUser.getFriendsList().contains(onUser.getUserID())) {
                binding.tvRequestSent.setText(R.string.already_friend);
                binding.tvRequestSent.setVisibility(View.VISIBLE);
                binding.btnAddFriend.setVisibility(View.GONE);
            } else {
                binding.tvRequestSent.setVisibility(View.GONE);
                binding.btnAddFriend.setVisibility(View.VISIBLE);
            }

            binding.getRoot().setOnClickListener(v -> checkSuggestedProfile(onUser));
        }

        private void checkSuggestedProfile(User onUser) {
            Intent intent = new Intent(mContext, SuggestedProfileActivity.class);
            intent.putExtra(Constants.KEY_USER, onUser);
            mContext.startActivity(intent);
        }
    }
}
