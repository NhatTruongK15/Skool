package com.example.clown.utilities;

import java.security.PublicKey;
import java.util.HashMap;

public class Constants {
    public static final String KEY_GROUP_MEMBERS = "members";
    public static final String KEY_GROUP_ADMIN = "admin";
    public static final String KEY_DOCUMENT_ID = "documentId";
    public static  final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE_NUMBER = "phoneNumber";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_REFERENCE_NAME = "clown";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_DOCUMENT_REFERENCE_ID = "documentReferenceID"; // userID cũ (  thực chất là ID của document )
    public static final String KEY_USER_ID = "userID"; // authencation ID not use anymore
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID ="senderId";
    public static final String KEY_RECEIVER_ID="receiverId";
    public static final String KEY_MESSAGE ="message";
    public static final String KEY_TIMESTAMP="timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS="conversations";
    public static final String KEY_SENDER_NAME="senderName";
    public static final String KEY_RECEIVER_NAME="receiverName";
    public static final String KEY_SENDER_IMAGE="senderImage";
    public static final String KEY_RECEIVER_IMAGE="receiverImage";
    public static final String KEY_LAST_MESSAGE="lastMessage";
    public static final String KEY_AVAILABILITY="availability";

    public static final String REMOTE_MSG_AUTHORIZATION="Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE="Content-Type";
    public static final String REMOTE_MSG_DATA="data";
    public static final String REMOTE_MSG_REGISTRATION_IDS="registration_ids";

    // Agora
    public static final String AGORA_APP_ID="7bf042d1345441fd9da44293ef98cd6d";
    public static final String AGORA_APP_CERTIFICATE="d6a903468c3a4cd590d4a560c2ffb44c";
    public static final String KEY_REMOTE_USER_DATA="remoteUserData";
    public static final String KEY_REMOTE_ID="remoteId";
    public static final String KEY_RTC_CHANNEL_ID="rtcChannelId";
    public static final String KEY_IS_CALLER="isCaller";


    public static final int EXPIRED_TIME_STAMP = 3600;
    public static final int MSG_REGISTER_CLIENT = 0;
    public static final int MSG_UNREGISTER_CLIENT = 1;
    public static final int MSG_AGORA_LOG_IN = 2;
    public static final int MSG_AGORA_LOG_OUT = 3;
    public static final int MSG_AGORA_LOCAL_INVITATION_SEND = 4;
    public static final int MSG_AGORA_LOCAL_INVITATION_REFUSED = 5;
    public static final int MSG_AGORA_LOCAL_INVITATION_CANCELED = 6;
    public static final int MSG_AGORA_LOCAL_INVITATION_FAILED = 7;
    public static final int MSG_AGORA_REMOTE_INVITATION_ACCEPTED = 8;
    public static final int MSG_AGORA_REMOTE_INVITATION_REFUSED = 9;
    public static final int MSG_AGORA_REMOTE_INVITATION_CANCELED = 10;
    public static final int MSG_AGORA_REMOTE_INVITATION_FAILED = 11;

    public static HashMap<String, String> remoteMsgHeader = null;
    public static HashMap<String, String> getRemoteMsgHeader(){
        if(remoteMsgHeader ==  null)
        {
            remoteMsgHeader = new HashMap<>();
            remoteMsgHeader.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAdTiSTwk:APA91bFj35r6k_Dw9FhsLLyidvdxZF5mMH0UY3POC42VaWu4afMiuhgiuHw0O40e6uLxuPns-ZyKBfuEVWw1ML7qPQD7vZppquS2cx8qdSh4LFEy0jesy-FElmvMJR0Lj7fEyoq1yWZQ"
            );
            remoteMsgHeader.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeader;
    }
}
