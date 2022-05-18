package com.example.clown.listeners;

import com.example.clown.models.GroupUser;
import com.example.clown.models.User;

public interface GroupChatListener {
    void onGroupChatClicked(User user);
    void onGroupChatClicked(GroupUser groupUser);
}
