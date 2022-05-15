package com.example.clown.models;

import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.Date;

public class ChatMessage {
    public String senderId, receiverId, message, dateTime;
    public Date dateObject;
    public String conversationId, conversationName, conversationImage;
    public Bitmap message_img=null;

    public String videoPath = null;
    public String filePath = null;

    public Uri uriVideo=null;
    public String message_img_link;

    public String finame;

    public MediaController mediaController;


}
