package com.example.clown.activities;

import static com.example.clown.utilities.Constants.HD_RES;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.example.clown.adapter.ChatAdapter;
import com.example.clown.adapter.GroupChatAdapter;
import com.example.clown.databinding.ActivityChatBinding;
import com.example.clown.models.ChatMessage;
import com.example.clown.models.Conversation;
import com.example.clown.network.APIClient;
import com.example.clown.network.APIService;
import com.example.clown.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {
    private Conversation mConversation;

    private String mReceiverId;
    private String mReceiverName;
    private String mReceiverAvatar;
    private Bitmap mReceiverBitmapAvatar;

    private ActivityChatBinding binding;
    private List<ChatMessage> chatMessages;
    private ArrayList<String> adminList;
    private ArrayList<String> memberList;
    private ChatAdapter chatAdapter;
    private GroupChatAdapter groupChatAdapter;
    private FirebaseFirestore database;
    private String conversationId = null;
    private Boolean checkGroupConversation = false;
    private boolean isReceiverAvailable = false;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListener();
        loadReceiverDetails();
        if(checkGroupConversation(mConversation.getId())){
            conversationId = mConversation.getId();
            checkGroupConversation = true;
        }
        init();


        listenMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //listenAvailabilityOfReceiver();
    }

    private void init() {

        database = FirebaseFirestore.getInstance();
        chatMessages = new ArrayList<>();

        if(!checkGroupConversation){
            chatAdapter = new ChatAdapter(chatMessages, mCurrentUser.getID(), mReceiverBitmapAvatar);
            binding.chatRecyclerView.setAdapter(chatAdapter);
        }
        else{
            groupChatAdapter = new GroupChatAdapter(chatMessages,
                    mCurrentUser.getID(),
                    database);
            binding.chatRecyclerView.setAdapter(groupChatAdapter);
        }
    }

    private boolean checkGroupConversation(String id) {
        try{
            double val = Double.parseDouble(id);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }


    public String filetype(String file){
        return file.substring(file.lastIndexOf("."));
    }

    public String checkFileType(String file) {
        String result = "";
        switch (filetype(file)) {
            case ".mp4":
                result = "vid";
                break;
            case ".png":
            case ".jpg":
            case ".jpeg":
            case ".gif":
                result = "img";
                break;
            case ".pdf":
            case ".docx":
            case ".pptx":
            case ".doc":
            case ".xlsx":
            case ".mp3":
            case ".flac":
            case ".mkv":
            case ".webm":

                result = "etc";
                break;
            default:
                break;
        }
        return result;
    }

    private String encodeImageFromUri(Uri fileuri){
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileuri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return encodeImage(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getBitmapFromEncodeString(String encodedImage) {
        if(encodedImage != null)
        {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        else
        {
            return null;
        }
    }
    private String encodeImage(Bitmap bitmap){
        int previewWidth = HD_RES;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(resizeBitmap(bitmap), previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,95,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private static final float PREFERRED_WIDTH = HD_RES;
    private static final float PREFERRED_HEIGHT = HD_RES;
    public static Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = PREFERRED_WIDTH / width;
        float scaleHeight = PREFERRED_HEIGHT / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, width, height, matrix, false);
        bitmap.recycle();
        return resizedBitmap;
    }

    public String filelink="";
    String finame;
    Uri fileuri;
    String imglink="";
    ActivityResultLauncher<Intent> activityResultLauncher;

    public void pickFile(){
        int i=0;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        activityResultLauncher.launch(intent);
        //startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }
    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    Boolean isfailupload=false;

    private void SendFileToDatabase(Uri  fileuri,String finame) {
        isfailupload=false;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(finame);
        storageReference.putFile(fileuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                isfailupload=false;
                loading(true);
                getLinkDownload(finame);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                isfailupload=true;
                showToast("failed  ");
                loading(false);
                isUploadingFile=false;
            }
        });


    }

    private void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {
        DownloadManager downloadManager=(DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri=Uri.parse(url);
        DownloadManager.Request request=new DownloadManager.Request(uri);
        request.setDestinationInExternalFilesDir(context,destinationDirectory,fileName+fileExtension);
        downloadManager.enqueue(request);
    }

    public Boolean isUploadingFile=false;

    private void loading(Boolean isLoading)
    {
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else
        {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void getLinkDownload(String finame){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(finame);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                filelink = uri.toString();
                loading(false);
                isUploadingFile=false;
                showToast("get link success");

            }
        });
    }

    public String VideoToBase64(String finame){
        File tempFile = new File(finame);
        String encodedString = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(tempFile);
        } catch (Exception e) {
            // TODO: handle exception
        }
        byte[] bytes;
        byte[] buffer = new byte[10000];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bytes = output.toByteArray();
        encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
        Log.i("Strng", encodedString);
        return encodedString;
    }

    public String videolocation=null;
    public void Base64ToVideo(String encodedString){
        byte[] decodedBytes = Base64.decode(encodedString.getBytes(),Base64.DEFAULT);
        try {
            FileOutputStream out = new FileOutputStream(
                    Environment.getExternalStorageDirectory()
                            + "/my/Convert.mp4");
            out.write(decodedBytes);
            out.close();
            videolocation=Environment.getExternalStorageDirectory()+ "/my/Convert.mp4";
//            binding.vidMessage.setVideoPath(videolocation);
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("Error", e.toString());
        }
    }

    Boolean isnotVid=false;
    private void sendMessage() {
        if( (binding.inputMessage.getText().toString().isEmpty()&&finame==null)||isUploadingFile==true){
            return;
        }
        if(isfailupload){
            encodedImage=null;
            finame=null;
            fileuri=null;
            filelink=null;
            imglink=null;
            videolocation=null;
            isfailupload=false;
            isnotVid=false;
            return;
        }
        if(fileuri!=null) {
            if (checkFileType(finame).compareTo("vid") == 0) {
//                loading(true);
//                getLinkDownload(finame);
                binding.inputMessage.setText("");
            }
            if (checkFileType(finame).compareTo("img") == 0) {
                imglink=filelink;
                filelink=null;
                encodedImage = encodeImageFromUri(fileuri);
                binding.inputMessage.setText("");
            }
            if(checkFileType(finame).compareTo("etc")==0){
                isnotVid=true;
                binding.inputMessage.setText(finame);
            }

        }
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, mCurrentUser.getID());
        if(checkGroupConversation)
            message.put(Constants.KEY_RECEIVER_ID,conversationId);
        else
            message.put(Constants.KEY_RECEIVER_ID, mReceiverId);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());

        if(filelink!=null) {
            if(isnotVid==false){
                message.put(Constants.KEY_MESSAGE_VIDEO, filelink);
            }
            message.put(Constants.KEY_MESSAGE_FILE,filelink);
            message.put(Constants.KEY_MESSAGE_IMAGE, "");
        }
        else{
            message.put(Constants.KEY_MESSAGE_VIDEO,"");

        }
        if(encodedImage!=null){
            message.put(Constants.KEY_MESSAGE_IMAGE,encodedImage);
            message.put(Constants.KEY_MESSAGE_IMAGE_LINK,imglink);
        }
        else{
            message.put(Constants.KEY_MESSAGE_IMAGE,"");
            message.put(Constants.KEY_MESSAGE_IMAGE_LINK,"");

        }

        message.put(Constants.KEY_MESSAGE_FINAME,finame);



        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversationId != null) {
            updateConversation(binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID, mCurrentUser.getID());
            conversation.put(Constants.KEY_SENDER_NAME, mCurrentUser.getUsername());
            conversation.put(Constants.KEY_SENDER_AVATAR, mCurrentUser.getAvatar());
            conversation.put(Constants.KEY_RECEIVER_ID, mReceiverId);
            conversation.put(Constants.KEY_RECEIVER_NAME, mReceiverName);
            conversation.put(Constants.KEY_RECEIVER_AVATAR, mReceiverAvatar);
            conversation.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversation(conversation);

        }
        /*if (!isReceiverAvailable) {
            try {
                *//*JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.getToken());*//*

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_DOCUMENT_REFERENCE_ID, mCurrentUser.getID());
                data.put(Constants.KEY_USERNAME, mCurrentUser.getUsername());
                //data.put(Constants.KEY_FCM_TOKEN, mCurrentUser.getToken());
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
                data.put(Constants.KEY_MESSAGE_IMAGE, encodedImage);
                data.put(Constants.KEY_MESSAGE_IMAGE_LINK,imglink);
                data.put(Constants.KEY_MESSAGE_IMAGE_FINAME,finame);
                data.put(Constants.KEY_MESSAGE_FINAME,finame);

                data.put(Constants.KEY_MESSAGE_VIDEO,filelink);
                data.put(Constants.KEY_MESSAGE_FILE,filelink);


                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                //body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (Exception exception) {
                showToast("how about this one" + exception.getMessage());
            }
        }*/
        binding.inputMessage.setText(null);

        encodedImage=null;
        finame=null;
        fileuri=null;
        filelink=null;
        imglink=null;
        videolocation=null;
        isnotVid=false;
    }

    private void sendNotification(String messageBody) {
        APIClient.getClient().create(APIService.class).sendMessage(
                Constants.getRemoteMsgHeader(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJSON = new JSONObject(response.body());
                            JSONArray results = responseJSON.getJSONArray("results");
                            if (responseJSON.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast("this one ??? " + error.getString("error"));
                                return;
                            }
                            showToast("success");
                            Log.d("test","success");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("test","success maybe");

                    }
                }
                else
                    showToast("Error: " + response.code());
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast("testing this" + (t.getMessage()));
            }
        });
    }

    private void listenAvailabilityOfReceiver() {
        database
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(mReceiverId)
                .addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                //receiverUser.setToken(value.getString(Constants.KEY_FCM_TOKEN));
                if (mReceiverAvatar == null) {
                    //receiverUser.setAvatar(value.getString(Constants.KEY_AVATAR));
                    chatAdapter.setReceiverProfileImage(mReceiverBitmapAvatar);
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                }
            }
            if (isReceiverAvailable) {
                binding.textAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailability.setVisibility(View.GONE);
            }
        });

    }

    private void listenMessages() {
        if(checkGroupConversation(conversationId)) {
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID,mReceiverId)
                    .addSnapshotListener(eventGroupListener);
        }
        else{
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_SENDER_ID, mCurrentUser.getID())
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, mReceiverId)
                    .addSnapshotListener(eventUserListener);
            database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_SENDER_ID, mReceiverId)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, mCurrentUser.getID())
                    .addSnapshotListener(eventUserListener);}
    }

    private final EventListener<QuerySnapshot> eventGroupListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getId();
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.message_img=getBitmapFromEncodeString( documentChange.getDocument().getString(Constants.KEY_MESSAGE_IMAGE));
                    chatMessage.videoPath=documentChange.getDocument().getString(Constants.KEY_MESSAGE_VIDEO);
                    chatMessage.filePath=documentChange.getDocument().getString(Constants.KEY_MESSAGE_FILE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.message_img_link=documentChange.getDocument().getString(Constants.KEY_MESSAGE_IMAGE_LINK);
                    chatMessage.finame=documentChange.getDocument().getString(Constants.KEY_MESSAGE_FINAME);
                    chatMessages.add(chatMessage);
                }
            }
            showMessage(chatMessages,count);
        }
        binding.progressBar.setVisibility(View.GONE);

    });

    private final EventListener<QuerySnapshot> eventUserListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.message_img=getBitmapFromEncodeString( documentChange.getDocument().getString(Constants.KEY_MESSAGE_IMAGE));
                    chatMessage.videoPath=documentChange.getDocument().getString(Constants.KEY_MESSAGE_VIDEO);
                    chatMessage.filePath=documentChange.getDocument().getString(Constants.KEY_MESSAGE_FILE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.message_img_link=documentChange.getDocument().getString(Constants.KEY_MESSAGE_IMAGE_LINK);
                    chatMessage.finame=documentChange.getDocument().getString(Constants.KEY_MESSAGE_FINAME);
                    chatMessages.add(chatMessage);
                }
            }
            showMessage(chatMessages,count);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversationId == null)
            checkConversation();
    });

    private void showMessage(List<ChatMessage> chatMessages,int count) {
        Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
        if(!checkGroupConversation)
        {
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
        }
        else{
            if (count == 0) {
                groupChatAdapter.notifyDataSetChanged();
            } else {
                groupChatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
        }
        binding.chatRecyclerView.setVisibility(View.VISIBLE);
    }


    private void loadReceiverDetails() {
        //receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);

        mConversation = (Conversation) getIntent().getSerializableExtra(Constants.KEY_COLLECTION_CONVERSATIONS);

        // Load receiver details
        mReceiverId = mConversation.getReceiverId() == null ? mConversation.getId() :
                mConversation.getReceiverId().equals(mCurrentUser.getID()) ?
                        mConversation.getSenderId() : mConversation.getReceiverId();

        mReceiverName = mConversation.getName() != null ? mConversation.getName() :
                mConversation.getReceiverId().equals(mCurrentUser.getID()) ?
                        mConversation.getName() : mConversation.getReceiverName();

        mReceiverAvatar = mConversation.getImage() != null ? mConversation.getImage() :
                mConversation.getReceiverId().equals(mCurrentUser.getID()) ?
                        mConversation.getSenderAvatar() : mConversation.getReceiverAvatar();

        mReceiverBitmapAvatar = getBitmapFromEncodeString(mReceiverAvatar);
        adminList  = (ArrayList<String>) getIntent().getSerializableExtra(Constants.KEY_LIST_GROUP_ADMIN);
        memberList  = (ArrayList<String>) getIntent().getSerializableExtra(Constants.KEY_LIST_GROUP_MEMBER);
        binding.textName.setText(mReceiverName);
    }

    private void setListener() {
        binding.imageBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
        });
        binding.layoutSend.setOnClickListener(v -> sendMessage());

        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                fileuri= result.getData().getData();
                finame=getFileName(fileuri);
                loading(true);
                isUploadingFile=true;
                SendFileToDatabase(fileuri,finame);

//                binding.inputMessage.setText(finame);
            }
        });
        binding.layoutFile.setOnClickListener(v -> pickFile());

        binding.imageCall.setOnClickListener(v -> startCall());
    }

    private void startCall() {
        Intent intent = new Intent(Constants.ACT_AGORA_LOCAL_INVITATION_SEND);
        intent.putExtra(Constants.KEY_REMOTE_ID, mReceiverId);
        intent.putExtra(Constants.KEY_RTC_CHANNEL_ID, mConversation.getId());
        sendBroadcast(intent);
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String, Object> conversation) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversation(String message) {
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        if(checkGroupConversation(conversationId))
            documentReference.update(Constants.KEY_SENDER_ID, mCurrentUser.getID()
                    ,Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, new Date());
        else
            documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, new Date());
    }

    private void checkConversation() {
        if (chatMessages.size() != 0) {
            checkConversationRemote(
                    mCurrentUser.getID(),
                    mReceiverId
            );

            checkConversationRemote(
                    mReceiverId,
                    mCurrentUser.getID()
            );
        }
    }

    private void checkConversationRemote(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };
}