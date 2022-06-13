package com.example.clown.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.R;
import com.example.clown.activities.FriendProfileActivity;
import com.example.clown.databinding.ItemFriendBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> implements Filterable {
    private final List<User> mFriendsList;
    private final List<User> mFriendsListFull;
    private final Context mContext;

    public FriendAdapter(Context context, List<User> dataSet) {
        mContext = context;
        mFriendsList = dataSet;
        mFriendsListFull = new ArrayList<>(mFriendsList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ItemFriendBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User onFriend = mFriendsList.get(position);
        holder.setBinding(onFriend);
    }

    @Override
    public int getItemCount() {
        return mFriendsList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<User> filteredFriendList = new ArrayList<>();
                String filteredPattern = constraint.toString().toLowerCase().trim();

                if (filteredPattern.isEmpty())
                    filteredFriendList = mFriendsListFull;
                else for (User onFriend : mFriendsListFull)
                    if (onFriend.getUsername().toLowerCase().contains(filteredPattern)
                            || onFriend.getEmail().toLowerCase().contains(filteredPattern)
                            || onFriend.getPhoneNumber().contains(filteredPattern))
                        filteredFriendList.add(onFriend);

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredFriendList;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFriendsList.clear();
                //noinspection unchecked
                mFriendsList.addAll((ArrayList<User>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFriendBinding binding;

        public ViewHolder(@NonNull ItemFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setBinding(@NonNull User onFriend) {
            binding.tvUsername.setText(onFriend.getUsername());
            binding.tvPhoneNumber.setText(onFriend.getPhoneNumber());
            binding.rivAvatar.setImageBitmap(onFriend.getBitmapAvatar());

            if (onFriend.getAvailability()) {
                int onlineColor = ContextCompat.getColor(mContext, R.color.status_online);
                binding.ivStatus.setImageResource(R.drawable.ic_online_circle);
                binding.tvStatus.setText(R.string.status_online);
                binding.tvStatus.setTextColor(onlineColor);
            } else {
                int offlineColor = ContextCompat.getColor(mContext, R.color.status_offline);
                binding.ivStatus.setImageResource(R.drawable.ic_offline_circle);
                binding.tvStatus.setText(R.string.status_offline);
                binding.tvStatus.setTextColor(offlineColor);
            }

            binding.getRoot().setOnClickListener(v -> checkFriendProfile(onFriend));
        }

        private void checkFriendProfile(User onFriend) {
            Intent intent = new Intent(mContext, FriendProfileActivity.class);
            intent.putExtra(Constants.KEY_USER, onFriend);
            mContext.startActivity(intent);
        }
    }
}
