package com.example.clown.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.type.DateTime;

import java.util.List;

public class Conversation {
    private String mId;
    private String mName;
    private String mLastMessage;
    private DateTime mTimeStamp;
    private List<String> mAdmins;
    private List<String> mMembers;

    private String mSenderId;
    private String mSenderName;
    private String mSenderImage;

    private String mReceiverId;
    private String mReceiverName;
    private String mReceiverImage;

    //region #Accessors
    public String getId() { return mId; }
    public void setId(String mId) { this.mId = mId; }

    public String getName() { return mName; }
    public void setName(String mName) { this.mName = mName; }

    public String getLastMessage() { return mLastMessage; }
    public void setLastMessage(String mLastMessage) { this.mLastMessage = mLastMessage; }

    public DateTime getTimeStamp() { return mTimeStamp; }
    public void setTimeStamp(DateTime mTimeStamp) { this.mTimeStamp = mTimeStamp; }

    public List<String> getAdmins() { return mAdmins; }
    public void setAdmins(List<String> mAdmins) { this.mAdmins = mAdmins; }

    public List<String> getMembers() { return mMembers; }
    public void setMembers(List<String> mMembers) { this.mMembers = mMembers; }

    public String getSenderId() { return mSenderId; }
    public void setSenderId(String mSenderId) { this.mSenderId = mSenderId; }

    public String getSenderName() { return mSenderName; }
    public void setSenderName(String mSenderName) { this.mSenderName = mSenderName; }

    public String getSenderImage() { return mSenderImage; }
    public Bitmap getSenderBitmapImage() {
        if (mSenderImage == null) return null;
        byte[] bytes = Base64.decode(mSenderImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    public void setSenderImage(String mSenderImage) { this.mSenderImage = mSenderImage; }

    public String getReceiverId() { return mReceiverId; }
    public void setReceiverId(String mReceiverId) { this.mReceiverId = mReceiverId; }

    public String getReceiverName() { return mReceiverName; }
    public void setReceiverName(String mReceiverName) { this.mReceiverName = mReceiverName; }

    public String getReceiverImage() { return mReceiverImage; }
    public Bitmap getReceiverBitmapImage() {
        if (mReceiverImage == null) return null;
        byte[] bytes = Base64.decode(mReceiverImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    public void setReceiverImage(String mReceiverImage) { this.mReceiverImage = mReceiverImage; }
    //endregion
}
