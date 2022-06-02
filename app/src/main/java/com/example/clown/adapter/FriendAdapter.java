package com.example.clown.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.R;
import com.example.clown.activities.FriendProfileActivity;
import com.example.clown.databinding.ItemFriendBinding;
import com.example.clown.models.User;
import com.example.clown.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> implements Filterable {
    private final List<User> mFriendsList;
    private final List<User> mFriendsListFull;
    private final Context mContext;

    protected class FriendViewHolder extends RecyclerView.ViewHolder {
        private final ItemFriendBinding binding;

        public FriendViewHolder(@NonNull ItemFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("ResourceAsColor")
        public void setBinding(@NonNull User onFriend) {
            binding.tvUsername.setText(onFriend.getUsername());
            binding.tvPhoneNumber.setText(onFriend.getPhoneNumber());
            binding.rivAvatar.setImageBitmap(onFriend.getBitmapAvatar());

            if (onFriend.getAvailability()) {
                binding.ivStatus.setImageResource(R.drawable.ic_online_circle);
                binding.tvStatus.setText(R.string.status_online);
                binding.tvStatus.setTextColor(R.color.status_online);
            } else {
                binding.ivStatus.setImageResource(R.drawable.ic_status_offline);
                binding.tvStatus.setText(R.string.status_offline);
                binding.tvStatus.setTextColor(R.color.status_offline);
            }

            binding.getRoot().setOnClickListener(v -> checkFriendProfile(onFriend));
        }

        private void checkFriendProfile(User onFriend) {
            Intent intent = new Intent(mContext, UserProfileActivity.class);
            intent.putExtra(Constants.KEY_USER, onFriend);
            mContext.startActivity(intent);
        }
    }

    public FriendsAdapter(Context context, List<User> dataSet) {
        mContext = context;
        mFriendsList = new ArrayList<>(dataSet);
        mFriendsListFull = new ArrayList<>(mFriendsList);
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new FriendViewHolder(ItemFriendBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        final User onFriend = mFriendsList.get(position);
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
}
