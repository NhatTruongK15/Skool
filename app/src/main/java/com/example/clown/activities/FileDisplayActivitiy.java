package com.example.clown.activities;

import static com.example.clown.utilities.Constants.HD_RES;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clown.R;
import com.example.clown.databinding.ActitvityDisplayFileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;

public class FileDisplayActivitiy extends AppCompatActivity {
    public ActitvityDisplayFileBinding binding;
    private String imagePath = "";
    private FirebaseFirestore database;
    private String videoPath = "";
    MediaController mediaController;
    VideoView videoView;
    ImageView imageView;
    TextView textView;

    ImageView imageBack;
    ImageView imageDownload;
    String finame;
    private String filePath = "";

    String downloadImagePath = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActitvityDisplayFileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imageBack = findViewById(R.id.imageBack);
        imageDownload = findViewById(R.id.imageDownload);
        imageView = findViewById(R.id.imgMessage);
        videoView = findViewById(R.id.vidMessage);
        textView = findViewById(R.id.textMessage);
        imageBack.setOnClickListener(v -> onBackPressed());

        Bundle bundle = null;
        bundle = getIntent().getExtras();
        if (bundle != null) {
            imagePath = bundle.getString("imgPath");
            videoPath = bundle.getString("vidPath");
            filePath = bundle.getString("fiPath");
            finame = bundle.getString("finame");
        }

        innit();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }

    public String filetype(String file) {
        return file.substring(file.lastIndexOf("."));
    }

    private String encodeImageFromUri(Uri fileuri) {
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
        if (encodeImage != null) {
            byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = HD_RES;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(resizeBitmap(bitmap), previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 95, byteArrayOutputStream);
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

    public String filelink = "";
    ActivityResultLauncher<Intent> activityResultLauncher;

    private void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {
//        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle(fileName);
        request.setDescription("Downloading...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()));
//        request.setDestinationInExternalFilesDir(context,destinationDirectory,fileName+fileExtension);

        DownloadManager down = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (down != null) {
            down.enqueue(request);
        }
//        downloadManager.enqueue(request);
    }

    public Boolean isUploadingFile = false;

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void getLinkDownload(String finame) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(finame);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                filelink = uri.toString();
                loading(false);
                isUploadingFile = false;
//                showToast("upload success");

            }
        });
    }

    public String VideoToBase64(String finame) {
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

    public String videolocation = null;

    public void Base64ToVideo(String encodedString) {
        byte[] decodedBytes = Base64.decode(encodedString.getBytes(), Base64.DEFAULT);
        try {
            FileOutputStream out = new FileOutputStream(
                    Environment.getExternalStorageDirectory()
                            + "/my/Convert.mp4");
            out.write(decodedBytes);
            out.close();
            videolocation = Environment.getExternalStorageDirectory() + "/my/Convert.mp4";
//            binding.vidMessage.setVideoPath(videolocation);
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("Error", e.toString());
        }
    }

    public void innit() {

        if (videoPath.compareTo("") != 0) {
//            Toast.makeText(this, "654", Toast.LENGTH_SHORT).show();
            mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            // implement on completion listener on video view
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
//                    Toast.makeText(getApplicationContext(), "Thank You...!!!", Toast.LENGTH_SHORT).show();
                }
            });
            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
//                    Toast.makeText(getApplicationContext(), "Oops An Error Occur While Playing Video...!!!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
            videoView.setVideoURI(Uri.parse(videoPath));
            videoView.start();
        } else {
            videoView.setVisibility(View.GONE);
            videoView.setLayoutParams(new FrameLayout.LayoutParams(1, 1));
        }
        if (imagePath.compareTo("") != 0) {
            Bitmap bitmap = null;
            try {

                final File localFile = File.createTempFile(finame, "");
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(finame);
                storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap tempbitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        imageView.setImageBitmap(tempbitmap);
                        imageView.setVisibility(View.VISIBLE);
//                        Toast.makeText(getApplicationContext(), "retr", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getApplicationContext(), "unretr", Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (Exception e) {
                imageView.setImageBitmap(bitmap);
            }

//            Toast.makeText(this, "321", Toast.LENGTH_SHORT).show();
        } else {
            imageView.setVisibility(View.GONE);
        }

        if (filePath.compareTo("") != 0&&videoPath.compareTo("")==0) {
            textView.setText(finame + " is not available to preview");
            binding.progressBar.setVisibility(View.GONE);

        } else {
            textView.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);

        }


        imageDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imagePath.compareTo("") != 0) {
                    downloadFile(getApplicationContext(), finame, "", Environment.DIRECTORY_DOWNLOADS, imagePath);
                } else if (videoPath.compareTo("") != 0) {
                    downloadFile(getApplicationContext(), finame, "", Environment.DIRECTORY_DOWNLOADS, videoPath);
                } else if (filePath.compareTo("") != 0) {
                    downloadFile(getApplicationContext(), finame, "", Environment.DIRECTORY_DOWNLOADS, filePath);
                }
            }
        });
    }

    public Bitmap ByteArrayToBitmap(byte[] arr) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length);
        return bitmap;
    }

    private static final String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";

    private static String getRandomString(final int sizeOfRandomString) {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}
