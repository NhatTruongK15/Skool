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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.clown.databinding.ActivityChatBinding;
import com.example.clown.databinding.ActitvityDisplayFileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileDisplayActivitiy extends ChatActivity {
    public ActitvityDisplayFileBinding binding;
    private String encodedImage;
    private FirebaseFirestore database;
    private String videoPath;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActitvityDisplayFileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        innit();
    }
    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }
    public String filetype(String file){
        return file.substring(file.lastIndexOf("."));
    }

    public String checkFileType(String file) {
        String result = "";
        switch (filetype(file)) {
            case ".mp4":
            case ".mkv":
                result = "vid";
                break;
            case ".png":
            case ".jpg":
            case ".jpeg":
                result = "img";
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

    private Bitmap getBitmapFromEncodeString(String encodeImage) {
        if(encodeImage != null)
        {
            byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
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
    private void SendFileToDatabase(Uri  fileuri,String finame) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(finame);
        storageReference.putFile(fileuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                loading(true);
                getLinkDownload(finame);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
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
                showToast("upload success");

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
    public void innit(){

        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.imgMessage.setImageBitmap(resizeBitmap(getBitmapFromEncodeString(encodedImage)));
        if(videoPath!=null&&videoPath.compareTo("")!=0) {
            MediaController mediaController = new MediaController(getApplicationContext());
            mediaController.setAnchorView(binding.vidMessage);
            binding.vidMessage.setMediaController(mediaController);

            // implement on completion listener on video view
            binding.vidMessage.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Toast.makeText(getApplicationContext(), "Thank You...!!!", Toast.LENGTH_SHORT).show();
                }
            });
            binding.vidMessage.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Toast.makeText(getApplicationContext(), "Oops An Error Occur While Playing Video...!!!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
            binding.vidMessage.setVideoURI(Uri.parse(videoPath));
        }
        else{
            binding.vidMessage.setVisibility(View.GONE);
            binding.vidMessage.setLayoutParams(new FrameLayout.LayoutParams(1,1));
        }


    }
}
