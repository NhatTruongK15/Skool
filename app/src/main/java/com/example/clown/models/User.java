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

public class User implements Serializable {
    // Account
    private String mID;
    private String mUsername;
    private String mPhoneNumber;
    private String mPassword;
    private String mEmail;

    // Personal
    private String mAvatar;
    private String mFirstName;
    private String mLastName;
    private Date mDateOfBirth;
    private String mGender;
    private String mBio;

    // Connection
    private boolean mAvailability;
    private final List<String> mFriends;
    private final List<String> mReceivedRequests;
    private final List<String> mSentRequests;

    //region #Accessors
    @PropertyName(Constants.KEY_ID) public String getID() { return mID; }
    @PropertyName(Constants.KEY_ID) public void setID(String id) { this.mID  = id; }

    @PropertyName(Constants.KEY_USERNAME) public String getUsername() { return mUsername; }
    @PropertyName(Constants.KEY_USERNAME) public void setUsername(String name) { this.mUsername = name; }

    @PropertyName(Constants.KEY_PHONE_NUMBER) public String getPhoneNumber() { return mPhoneNumber; }
    @PropertyName(Constants.KEY_PHONE_NUMBER) public void setPhoneNumber(String phoneNumber) { this.mPhoneNumber = phoneNumber; }

    @PropertyName(Constants.KEY_EMAIL) public String getEmail() { return mEmail; }
    @PropertyName(Constants.KEY_EMAIL) public void setEmail(String email) { this.mEmail = email; }

    @PropertyName(Constants.KEY_PASSWORD) public String getPassword() { return mPassword; }
    @PropertyName(Constants.KEY_PASSWORD) public void setPassword(String mPassword) { this.mPassword = mPassword; }

    @PropertyName(Constants.KEY_AVATAR) public String getAvatar() { return mAvatar; }
    @PropertyName(Constants.KEY_AVATAR) public void setAvatar(String avatar) { this.mAvatar = avatar; }

    @PropertyName(Constants.KEY_FIRST_NAME) public String getFirstName() { return mFirstName; }
    @PropertyName(Constants.KEY_FIRST_NAME) public void setFirstName(String mFirstName) {this.mFirstName = mFirstName; }

    @PropertyName(Constants.KEY_LAST_NAME) public String getLastName() { return mLastName; }
    @PropertyName(Constants.KEY_LAST_NAME) public void setLastName(String mLastName) { this.mLastName = mLastName; }

    @PropertyName(Constants.KEY_DATE_OF_BIRTH) public Date getDateOfBirth() { return mDateOfBirth; }
    @PropertyName(Constants.KEY_DATE_OF_BIRTH) public void setDateOfBirth(Date mDateOfBirth) { this.mDateOfBirth = mDateOfBirth; }

    @PropertyName(Constants.KEY_GENDER) public String getGender() { return mGender; }
    @PropertyName(Constants.KEY_GENDER) public void setGender(String mGender) { this.mGender = mGender; }

    @PropertyName(Constants.KEY_BIO) public String getBio() { return mBio; }
    @PropertyName(Constants.KEY_BIO) public void setBio(String mBio) { this.mBio = mBio; }

    @PropertyName(Constants.KEY_AVAILABILITY) public boolean getAvailability() { return mAvailability; }
    @PropertyName(Constants.KEY_AVAILABILITY) public void setAvailability(boolean availability) { this.mAvailability = availability; }

    @PropertyName(Constants.KEY_FRIEND_LIST) public List<String> getFriends() { return mFriends; }
    @PropertyName(Constants.KEY_FRIEND_LIST) public void setFriends(List<String> friendsList) {
        this.mFriends.clear();
        this.mFriends.addAll(friendsList);
    }

    @PropertyName(Constants.KEY_RECEIVED_REQUESTS) public List<String> getReceivedRequests() { return mReceivedRequests; }
    @PropertyName(Constants.KEY_RECEIVED_REQUESTS) public void setReceivedRequests(List<String> mReceivedRequests) {
        this.mReceivedRequests.clear();
        this.mReceivedRequests.addAll(mReceivedRequests);
    }

    @PropertyName(Constants.KEY_SENT_REQUESTS) public List<String> getSentRequests() { return mSentRequests; }
    @PropertyName(Constants.KEY_SENT_REQUESTS) public void setSentRequests(List<String> mSentRequests) {
        this.mSentRequests.clear();
        this.mSentRequests.addAll(mSentRequests);
    }

    @Exclude public Bitmap getBitmapAvatar() {
        if (mAvatar == null) return null;
        byte[] bytes = Base64.decode(mAvatar, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    //endregion

    public User() {
        setID(Constants.VALUE_UN_INITIALIZED);
        setUsername(Constants.VALUE_UN_INITIALIZED);
        setPhoneNumber(Constants.VALUE_UN_INITIALIZED);
        setEmail(Constants.VALUE_UN_INITIALIZED);
        setPassword(Constants.VALUE_UN_INITIALIZED);

        setAvatar(Constants.VALUE_UN_INITIALIZED);
        setFirstName(Constants.VALUE_UN_INITIALIZED);
        setLastName(Constants.VALUE_UN_INITIALIZED);
        setDateOfBirth(new Date());
        setGender(Constants.VALUE_UN_INITIALIZED);
        setBio(Constants.VALUE_UN_INITIALIZED);

        setAvailability(false);
        mFriends = new ArrayList<>();
        mReceivedRequests = new ArrayList<>();
        mSentRequests = new ArrayList<>();
    }

    public void Clone(User source) {
        setID(source.getID());
        setUsername(source.getUsername());
        setPhoneNumber(source.getPhoneNumber());
        setEmail(source.getEmail());
        setPassword(source.getPassword());

        setAvatar(source.getAvatar());
        setFirstName(source.getFirstName());
        setLastName(source.getLastName());
        setDateOfBirth(source.getDateOfBirth());
        setGender(source.getGender());
        setBio(source.getBio());

        setAvailability(source.getAvailability());
        setFriends(source.getFriends());
        setReceivedRequests(source.getReceivedRequests());
        setSentRequests(source.getSentRequests());
    }
}
