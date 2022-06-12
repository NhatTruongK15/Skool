package com.example.clown.utilities;

import android.app.Application;

import java.util.HashMap;

public class Constants {
    public static final String KEY_LIST_GROUP_ADMIN = "adminList";
    public static final String KEY_LIST_GROUP_MEMBER = "memberList";
    public static final String KEY_GROUP_NAME = "groupname";
    public static final String KEY_HASH_MAP_GROUP_MEMBERS = "hashmap";
    public static final String KEY_GROUP_MEMBERS = "members";
    public static final String KEY_GROUP_ADMIN = "admin";
    public static final String KEY_DOCUMENT_ID = "documentId";
    public static  final String KEY_COLLECTION_USERS = "users";

    public static final String KEY_REFERENCE_NAME = "clown";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_DOCUMENT_REFERENCE_ID = "documentReferenceID"; // userID cũ (  thực chất là ID của document )
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_EDIT_PROFILETYPE = "editProfileType";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_MESSAGE ="message";
    public static final String KEY_MESSAGE_IMAGE ="message_image";
    public static final String KEY_MESSAGE_IMAGE_LINK ="message_image_link";
    public static final String KEY_MESSAGE_IMAGE_FINAME ="message_image_finame";
    public static final String KEY_MESSAGE_FINAME ="message_finame";

    public static final String KEY_MESSAGE_VIDEO ="message_video";
    public static final String KEY_MESSAGE_VIDEO_BASE64 ="message_video_base64";

    public static final String KEY_MESSAGE_FILE ="message_file";



    public static final String KEY_COLLECTION_CONVERSATIONS="conversations";
    public static final String KEY_LAST_MESSAGE="lastMessage";

    public static final String REMOTE_MSG_AUTHORIZATION="Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE="Content-Type";
    public static final String REMOTE_MSG_DATA="data";
    public static final String REMOTE_MSG_REGISTRATION_IDS="registration_ids";

    // Firestore User Model
    public static final String KEY_ID = "id";
    public static final String KEY_USERNAME = "userName";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PHONE_NUMBER = "phoneNumber";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_AVAILABILITY="availability";
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_BIO = "bio";
    public static final String KEY_DATE_OF_BIRTH = "dateOfBirth";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_FRIEND_LIST = "friendList";
    public static final String KEY_RECEIVED_REQUESTS = "receivedRequests";
    public static final String KEY_SENT_REQUESTS = "sentRequests";
    public static final String VALUE_UN_INITIALIZED = null;
    public static final String VALUE_GENDER_MALE = "Male";
    public static final String VALUE_GENDER_FEMALE = "Female";
    public static final String VALUE_GENDER_OTHER = "Other";

    // Firestore Conversation Model
    public static final String KEY_CONVERSATION_ID = "conversationId";
    public static final String KEY_CONVERSATION_IMAGE = "image";
    public static final String KEY_CONVERSATION_NAME = "name";
    public static final String KEY_CONVERSATION_LAST_MESSAGE = "lastMessage";
    public static final String KEY_TIMESTAMP="timeStamp";
    public static final String KEY_CONVERSATION_ADMINS = "admins";
    public static final String KEY_CONVERSATION_MEMBERS = "members";
    public static final String KEY_CONVERSATION_IS_BLOCKED = "isBlocked";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_SENDER_NAME= "senderName";
    public static final String KEY_RECEIVER_NAME="receiverName";
    public static final String KEY_SENDER_AVATAR = "senderImage";
    public static final String KEY_RECEIVER_AVATAR = "receiverImage";
    public static final String PATTERN_DATE_TIME_FORMATTER = "dd/MM/yyyy HH:mm:ss";
    public static final String PATTERN_DATE_ONLY_FORMATTER = "dd/MM/yyyy";
    public static final String PATTERN_TIME_ONLY_FORMATTER = "HH:mm:ss";

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

    // Application
    public static final String KEY_CHANNEL_ID = "applicationChannelID";
    public static final String KEY_CURRENT_USER = "currentUser";
    public static final int KEY_SERVICE_ID = 613;

    // Notifications
    public static final String NOTIFICATION_FRIEND_REQUEST_TITLE = "New friend request!";
    public static final String NOTIFICATION_NEW_FRIEND_ADDED_TITLE = "You've gotten a new friend!";

    // Broadcast actions
    public static final String ACT_UPDATE_CURRENT_USER = "updateCurrentUser";
    public static final String ACT_FRIEND_ADDED = "friendAdd";
    public static final String ACT_FRIEND_REMOVED = "friendRemove";
    public static final String ACT_RECEIVED_REQUEST_ADDED = "receivedRequestAdd";
    public static final String ACT_RECEIVED_REQUEST_REMOVED = "receivedRequestRemove";

    // Base Activity
    public static final String KEY_TRANSFER_DATA = "transferData";

    // SignIn Activity
    public static final String TOAST_EMPTY_EMAIL_OR_PHONE_NUMBER = "Empty email or phone number!";
    public static final String TOAST_INVALID_EMAIL_OR_PHONE_NUMBER = "Invalid email or phone number!";
    public static final String TOAST_EMPTY_PASSWORD = "Empty password!";
    public static final String TOAST_SIGN_IN_FAILED = "Failed to sign in!";
    public static final String TOAST_SIGN_IN_SUCCESSFULLY = "Sign in successfully!";

    // SignUp Activity
    public static final String TOAST_PLEASE_FILL_IN_ALL_INFORMATION = "Please fill in all information!";
    public static final String TOAST_INVALID_EMAIL = "Your email is invalid!";
    public static final String TOAST_UNCONFIRMED_PASSWORD = "Please confirm your password!";
    public static final String TOAST_PASSWORDS_UNMATCHED = "Passwords unmatched!";
    public static final String TOAST_ACCOUNT_ALREADY_SIGNED_UP = "This phone number has already had an account!";
    public static final String TOAST_WEAK_PASSWORD = "Your password must be at least 6 characters length!";
    public static final String TOAST_OVERFLOW_REQUESTS = "This device has sent too many requests!\nPlease try again later!";
    public static final String TOAST_INCORRECT_VERIFY_CODE = "Your verification code is incorrect!";
    public static final String TOAST_SIGN_UP_SUCCESSFULLY = "Sign up successfully!";

    // Main Activity
    public static final String TOAST_ON_SIGN_OUT = "Signing out...";
    public static final String TOAST_UPDATE_FCM_TOKEN_FAILED = "Update FcmToken failed!";

    // Contacts Activity
    public static final String TOAST_PHONE_CONTACT_REQ_FAILED = "The app needs your permission to access phone contacts!";
    public static final String TOAST_FRIEND_REQUEST_SENT = "Friend request's sent!";
    public static final String TOAST_FRIEND_REQUEST_DECLINED = "Friend request's declined!";
    public static final String TOAST_FRIEND_REQUEST_ACCEPTED = "Friend request's accepted!";

    public static final int HD_RES_860=860;
    public static final int HD_RES=860;
    public static final int PIC_HOLDER=500;

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
