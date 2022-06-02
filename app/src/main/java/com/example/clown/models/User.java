package com.example.clown.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private String name;
    private String image;
    private String email;
    private String token;
    private String userID;
    private boolean availability;
    private String phoneNumber;
    private ArrayList<String> friendsList = new ArrayList<>();
    private ArrayList<String> pendingRequests = new ArrayList<>();

    //region #Accessors
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRawImage() { return image; }
    public Bitmap getImage() {
        if (image == null) return null;
        byte[] bytes = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    public void setRawImage(String image) { this.image = image; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean getAvailability() { return availability; }
    public void setAvailability(boolean availability) { this.availability = availability; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public ArrayList<String> getFriendsList() { return friendsList; }
    public void setFriendsList(ArrayList<String> friendsList) { this.friendsList = friendsList; }

    public ArrayList<String> getPendingRequests() { return pendingRequests; }
    public void setPendingRequests(ArrayList<String> pendingRequests) { this.pendingRequests = pendingRequests; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }
    //endregion
}
