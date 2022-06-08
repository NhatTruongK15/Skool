package com.example.clown.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.firebase.firestore.Exclude;

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
    private List<String> mFriends;
    private List<String> mReceivedRequests;
    private List<String> mSentRequests;

    //region #Accessors
    public String getID() { return mID; }
    public void setID(String id) { this.mID  = id; }

    public String getUsername() { return mUsername; }
    public void setUsername(String name) { this.mUsername = name; }

    public String getPhoneNumber() { return mPhoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.mPhoneNumber = phoneNumber; }

    public String getEmail() { return mEmail; }
    public void setEmail(String email) { this.mEmail = email; }

    public String getPassword() { return mPassword; }
    public void setPassword(String mPassword) { this.mPassword = mPassword; }

    public String getAvatar() { return mAvatar; }
    public void setAvatar(String avatar) { this.mAvatar = avatar; }

    public String getFirstName() { return mFirstName; }
    public void setFirstName(String mFirstName) {this.mFirstName = mFirstName; }

    public String getLastName() { return mLastName; }
    public void setLastName(String mLastName) { this.mLastName = mLastName; }

    public Date getDateOfBirth() { return mDateOfBirth; }
    public void setDateOfBirth(Date mDateOfBirth) { this.mDateOfBirth = mDateOfBirth; }

    public String getGender() { return mGender; }
    public void setGender(String mGender) { this.mGender = mGender; }

    public String getBio() { return mBio; }
    public void setBio(String mBio) { this.mBio = mBio; }

    public boolean getAvailability() { return mAvailability; }
    public void setAvailability(boolean availability) { this.mAvailability = availability; }

    public List<String> getFriends() { return mFriends; }
    public void setFriendsList(List<String> friendsList) { this.mFriends = new ArrayList<>(friendsList); }

    public List<String> getReceivedRequests() { return mReceivedRequests; }
    public void setReceivedRequests(List<String> pendingRequests) { this.mReceivedRequests = new ArrayList<>(pendingRequests); }

    public List<String> getSentRequests() { return mSentRequests; }
    public void setSentRequests(List<String> sentRequests) { this.mSentRequests = new ArrayList<>(sentRequests); }

    @Exclude public Bitmap getBitmapAvatar() {
        if (mAvatar == null) return null;
        byte[] bytes = Base64.decode(mAvatar, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    @Exclude public String getFullName() { return mFirstName + " " + mLastName; }
    //endregion

    public User() {
        setID("");
        setUsername("Unnamed");
        setPhoneNumber("");
        setEmail("");
        setPassword("");

        setAvatar("");
        setFirstName("");
        setLastName("");
        setDateOfBirth(new Date());
        setGender("Male");
        setBio("None");

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
        setFriendsList(source.getFriends());
        setReceivedRequests(source.getReceivedRequests());
        setSentRequests(source.getSentRequests());
    }
}
