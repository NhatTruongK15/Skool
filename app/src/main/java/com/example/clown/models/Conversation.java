package com.example.clown.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.example.clown.utilities.Constants;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Conversation implements Serializable {
    // General
    private String mConversationId;
    protected String mName;
    private String mLastMessage;
    protected Date mTimeStamp;
    private final List<String> mAdmins;
    private final List<String> mMembers;

    // Sender
    protected String mSenderId;
    protected String mSenderName;
    protected String mSenderAvatar;

    // Receiver
    protected String mReceiverId;
    protected String mReceiverName;
    protected String mReceiverAvatar;

    //region #ACCESSORS
    @PropertyName(Constants.KEY_CONVERSATION_ID) public String getId() { return mConversationId; }
    @PropertyName(Constants.KEY_CONVERSATION_ID) public void setId(String mId) { this.mConversationId = mId; }

    @PropertyName(Constants.KEY_CONVERSATION_NAME) public String getName() { return mName; }
    @PropertyName(Constants.KEY_CONVERSATION_NAME) public void setName(String mName) { this.mName = mName; }

    @PropertyName(Constants.KEY_CONVERSATION_LAST_MESSAGE) public String getLastMessage() { return mLastMessage; }
    @PropertyName(Constants.KEY_CONVERSATION_LAST_MESSAGE) public void setLastMessage(String mLastMessage) { this.mLastMessage = mLastMessage; }

    @PropertyName(Constants.KEY_TIMESTAMP) public Date getTimeStamp() { return mTimeStamp; }
    @PropertyName(Constants.KEY_TIMESTAMP) public void setTimeStamp(Date mTimeStamp) { this.mTimeStamp = mTimeStamp; }

    @PropertyName(Constants.KEY_CONVERSATION_ADMINS) public List<String> getAdmins() { return mAdmins; }
    @PropertyName(Constants.KEY_CONVERSATION_ADMINS) public void setAdmins(List<String> mAdmins) {
        this.mAdmins.clear();
        this.mAdmins.addAll(mAdmins);
    }

    @PropertyName(Constants.KEY_CONVERSATION_MEMBERS) public List<String> getMembers() { return mMembers; }
    @PropertyName(Constants.KEY_CONVERSATION_MEMBERS) public void setMembers(List<String> mMembers) {
        this.mMembers.clear();
        this.mMembers.addAll(mMembers);
    }

    @PropertyName(Constants.KEY_SENDER_ID) public String getSenderId() { return mSenderId; }
    @PropertyName(Constants.KEY_SENDER_ID) public void setSenderId(String mSenderId) { this.mSenderId = mSenderId; }

    @PropertyName(Constants.KEY_SENDER_NAME) public String getSenderName() { return mSenderName; }
    @PropertyName(Constants.KEY_SENDER_NAME) public void setSenderName(String mSenderName) { this.mSenderName = mSenderName; }

    @PropertyName(Constants.KEY_SENDER_AVATAR) public String getSenderAvatar() { return mSenderAvatar; }
    @PropertyName(Constants.KEY_SENDER_AVATAR) public void setSenderAvatar(String mSenderAvatar) { this.mSenderAvatar = mSenderAvatar; }

    @PropertyName(Constants.KEY_RECEIVER_ID) public String getReceiverId() { return mReceiverId; }
    @PropertyName(Constants.KEY_RECEIVER_ID) public void setReceiverId(String mReceiverId) { this.mReceiverId = mReceiverId; }

    @PropertyName(Constants.KEY_RECEIVER_NAME) public String getReceiverName() { return mReceiverName; }
    @PropertyName(Constants.KEY_RECEIVER_NAME) public void setReceiverName(String mReceiverName) { this.mReceiverName = mReceiverName; }

    @PropertyName(Constants.KEY_RECEIVER_AVATAR) public String getReceiverAvatar() { return mReceiverAvatar; }
    @PropertyName(Constants.KEY_RECEIVER_AVATAR) public void setReceiverAvatar(String mReceiverAvatar) { this.mReceiverAvatar = mReceiverAvatar; }

    @Exclude public Bitmap getSenderBitmapAvatar() {
        if (mSenderAvatar == null) return null;
        return getBitmapImage(mSenderAvatar);
    }
    @Exclude public Bitmap getReceiverBitmapAvatar() {
        if (mReceiverAvatar == null) return null;
        return getBitmapImage(mReceiverAvatar);
    }
    //endregion

    public Conversation() {
        setId(Constants.VALUE_UN_INITIALIZED);
        setName(Constants.VALUE_UN_INITIALIZED);
        setLastMessage(Constants.VALUE_UN_INITIALIZED);
        setTimeStamp(new Date());

        setSenderId(Constants.VALUE_UN_INITIALIZED);
        setSenderName(Constants.VALUE_UN_INITIALIZED);
        setSenderAvatar(Constants.VALUE_UN_INITIALIZED);

        setReceiverId(Constants.VALUE_UN_INITIALIZED);
        setReceiverName(Constants.VALUE_UN_INITIALIZED);
        setReceiverAvatar(Constants.VALUE_UN_INITIALIZED);

        mAdmins = new ArrayList<>();
        mMembers = new ArrayList<>();
    }

    public Conversation(Conversation source) {
        setId(source.getId());
        setName(source.getName());
        setLastMessage(source.getLastMessage());
        setTimeStamp(source.getTimeStamp());

        setSenderId(source.getSenderId());
        setSenderName(source.getSenderName());
        setSenderAvatar(source.getSenderAvatar());

        setReceiverId(source.getReceiverId());
        setReceiverName(source.getReceiverName());
        setReceiverAvatar(source.getReceiverAvatar());

        this.mAdmins = new ArrayList<>(source.getAdmins());
        this.mMembers = new ArrayList<>(source.getMembers());
    }

    public void Clone(Conversation source) {
        setId(source.getId());
        setName(source.getName());
        setLastMessage(source.getLastMessage());
        setTimeStamp(source.getTimeStamp());

        setSenderId(source.getSenderId());
        setSenderName(source.getSenderName());
        setSenderAvatar(source.getSenderAvatar());

        setReceiverId(source.getReceiverId());
        setReceiverName(source.getReceiverName());
        setReceiverAvatar(source.getReceiverAvatar());

        setAdmins(source.getAdmins());
        setMembers(source.getMembers());
    }

    private Bitmap getBitmapImage(String src) {
        byte[] bytes = Base64.decode(src, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
