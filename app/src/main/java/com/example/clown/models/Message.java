package com.example.clown.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.example.clown.utilities.Constants;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class Message extends Conversation implements Serializable {
    private String mMessageID;
    private String mFileLink;
    private String mFileName;
    private String mContentText;

    //region ACCESSORS
    @PropertyName(Constants.KEY_MESSAGE_ID) public String getMessageID() { return mMessageID; }
    @PropertyName(Constants.KEY_MESSAGE_ID) public void setMessageID(String mMessageID) { this.mMessageID = mMessageID; }

    @PropertyName(Constants.KEY_MESSAGE_FILE_URI) public String getFileLink() { return mFileLink; }
    @PropertyName(Constants.KEY_MESSAGE_FILE_URI) public void setFileLink(String mFileUri) { this.mFileLink = mFileUri; }

    @PropertyName(Constants.KEY_MESSAGE_FILE_NAME) public String getFileName() { return mFileName; }
    @PropertyName(Constants.KEY_MESSAGE_FILE_NAME) public void setFileName(String mFileName) { this.mFileName = mFileName; }

    @PropertyName(Constants.KEY_MESSAGE_CONTENT) public String getContent() { return mContentText; }
    @PropertyName(Constants.KEY_MESSAGE_CONTENT) public void setContent(String mContentText) { this.mContentText = mContentText; }

    @Exclude public Bitmap getBitmapContent() {
        byte[] bytes = Base64.decode(mContentText, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    @Exclude public String getFileExtension() {
        String fileName = getFileName();
        if (fileName != null && !fileName.isEmpty())
            return fileName.substring(fileName.lastIndexOf("."));
        return null;
    }
    //endregion

    public Message() {
        setMessageID(Constants.VALUE_UN_INITIALIZED);
        setFileLink(Constants.VALUE_UN_INITIALIZED);
        setContent(Constants.VALUE_UN_INITIALIZED);
    }

    public Message(Message source) {
        setMessageID(source.getMessageID());
        setFileLink(source.getFileLink());
        setContent(source.getContent());
    }

    public void Reset() {
        setMessageID(Constants.VALUE_UN_INITIALIZED);
        setFileLink(Constants.VALUE_UN_INITIALIZED);
        setContent(Constants.VALUE_UN_INITIALIZED);
    }
}
