package com.example.clown.models;

import android.graphics.Bitmap;

import java.util.Date;

public class ChatMessage {
    public String senderId, receiverId, message, dateTime;
    public Date dateObject;
    public String conversationId, conversationName, conversationImage;
    public Bitmap message_img=null;
}
