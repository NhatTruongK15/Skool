package com.example.clown.adapter;

import static com.example.clown.utilities.Constants.HD_RES;
import static com.example.clown.utilities.Constants.HD_RES_860;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.VideoBitmapDecoder;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.clown.R;
import com.example.clown.activities.FileDisplayActivitiy;
import com.example.clown.databinding.ItemMediaAndFileBinding;
import com.example.clown.models.MediaAndFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

public class MediaAndFileAdapter extends RecyclerView.Adapter<MediaAndFileAdapter.MediaAndFileViewHolder> {
    private List<MediaAndFile> mediaandfile;
    private Context context;

    public MediaAndFileAdapter(Context context, List<MediaAndFile> mediaandfile) {
        this.context = context;
        this.mediaandfile = mediaandfile;
    }

    public MediaAndFileAdapter(ItemMediaAndFileBinding inflate) {

    }

    @Override
    public int getItemCount() {
        return mediaandfile == null ? 0 : mediaandfile.size();
    }

    @NonNull
    @Override
    public MediaAndFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MediaAndFileAdapter.MediaAndFileViewHolder(
                ItemMediaAndFileBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public int getItemViewType(int position) {

        return 0;
    }

    @Override
    public void onBindViewHolder(MediaAndFileAdapter.MediaAndFileViewHolder holder, int position) {
        holder.setData(mediaandfile.get(position));
//        String vidPath=mediaandfile.get(position).getVidPath();
//        String imgPath=mediaandfile.get(position).getImgPath();
//        String filePath=mediaandfile.get(position).getFilePath();
//        String finame=mediaandfile.get(position).getFiname();
//
//        if (imgPath != null) {
//            holder.imgbtnItem.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Context context = view.getContext();
//                    Intent intent = new Intent(context, FileDisplayActivitiy.class);
//                    intent.putExtra("imgPath", imgPath);
//                    intent.putExtra("finame", finame);
//                    intent.putExtra("fiPath", "");
//                    intent.putExtra("vidPath", "");
//                    context.startActivity(intent);
//                }
//            });
//            return;
//        }
//        if(vidPath!=null){
//            if (vidPath.compareTo("") != 0) {
//                holder.imgbtnItem.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Context context = v.getContext();
//                        Intent intent = new Intent(context, FileDisplayActivitiy.class);
//                        intent.putExtra("vidPath", vidPath);
//                        intent.putExtra("finame", finame);
//                        intent.putExtra("fiPath", "");
//                        intent.putExtra("imgPath", "");
//                        context.startActivity(intent);
//                    }
//                });
//            }
//        }
//        if(filePath!=null){
//            if (filePath.compareTo("") != 0) {
//                holder.imgbtnItem.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Context context = v.getContext();
//                        Intent intent = new Intent(context, FileDisplayActivitiy.class);
//                        intent.putExtra("fiPath", filePath);
//                        intent.putExtra("finame", finame);
//                        intent.putExtra("imgPath", "");
//                        intent.putExtra("vidPath", "");
//                        context.startActivity(intent);
//                    }
//                });
//            }
//
//        }

    }

    /**
     * Data ViewHolder class.
     */

    public class ThumbnailThread extends Thread{

    }

    public static class MediaAndFileViewHolder extends RecyclerView.ViewHolder {

        private ItemMediaAndFileBinding binding;

        public MediaAndFileViewHolder(ItemMediaAndFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

        public String filetype(String file) {
            return file.substring(file.lastIndexOf("."));
        }

        public String checkFileType(String file) {
            String result = "";
            switch (filetype(file)) {
                case ".mp4":
                    result = "VIDEO";
                    break;
                case ".png":
                case ".jpg":
                case ".jpeg":
                case ".gif":
                    result = "IMAGE";
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

                    result = "FILE";
                    break;
                default:
                    break;
            }
            return result;
        }

        public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                       boolean filter) {
            float ratio = Math.min(
                    (float) maxImageSize / realImage.getWidth(),
                    (float) maxImageSize / realImage.getHeight());
            int width = Math.round((float) ratio * realImage.getWidth());
            int height = Math.round((float) ratio * realImage.getHeight());
            Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                    height, filter);
            return newBitmap;
        }

        void setData(MediaAndFile mediaAndFile) {
//            binding.btnItem.setText(checkFileType(mediaAndFile.finame)+"\n"+mediaAndFile.timestamp );
            if (mediaAndFile.imgPath != null) {
                if (mediaAndFile.imgPath.compareTo("") != 0) {

                    getImgThumbnailFromURL(mediaAndFile.imgPath);
                    binding.btnItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Context context = view.getContext();
                            Intent intent = new Intent(context, FileDisplayActivitiy.class);
                            intent.putExtra("imgPath", mediaAndFile.imgPath);
                            intent.putExtra("finame", mediaAndFile.finame);
                            intent.putExtra("fiPath", "");
                            intent.putExtra("vidPath", "");
                            context.startActivity(intent);
                        }
                    });
                    return;
                }

            }
            if (mediaAndFile.vidPath != null) {
                if (mediaAndFile.vidPath.compareTo("") != 0) {

                    getVidThumbnailFromURL(mediaAndFile.vidPath);
                    binding.btnItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Context context = v.getContext();
                            Intent intent = new Intent(context, FileDisplayActivitiy.class);
                            intent.putExtra("vidPath", mediaAndFile.vidPath);
                            intent.putExtra("finame", mediaAndFile.finame);
                            intent.putExtra("fiPath", "");
                            intent.putExtra("imgPath", "");
                            context.startActivity(intent);
                        }
                    });
                    return;
                }
            }
            if (mediaAndFile.filePath != null) {
                if (mediaAndFile.filePath.compareTo("") != 0) {
                    getFileThumbnailFromURL(mediaAndFile.filePath);
                    binding.btnItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Context context = v.getContext();
                            Intent intent = new Intent(context, FileDisplayActivitiy.class);
                            intent.putExtra("fiPath", mediaAndFile.filePath);
                            intent.putExtra("finame", mediaAndFile.finame);
                            intent.putExtra("imgPath", "");
                            intent.putExtra("vidPath", "");
                            context.startActivity(intent);
                        }
                    });
                }

            }
        }

        public String getThumbnailInDir(String dir) {
            String path = null;

            return path;
        }

        public void getVidThumbnailFromURL(String url) {
//// Create a MediaMetaDataRetriever
//            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//// Set video url as data source
//            retriever.setDataSource(url, new HashMap<String, String>());
//// Get frame at 2nd second as Bitmap image
//            Bitmap source = retriever.getFrameAtTime(20000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//// Display the Bitmap image in an ImageView
//            Bitmap rescalebitmap = scaleDown(source, 300, true);
//            FileOutputStream outputstream = null;
//            File file = Environment.getExternalStorageDirectory();
//            File dir = new File(file.getAbsolutePath() + "//myThumbnails");
//            dir.mkdir();
//            String filename = String.format("%d.bmp", System.currentTimeMillis());
//            File outFile = new File(dir, filename);
//            try {
//                outputstream = new FileOutputStream(outFile);
//            } catch (Exception ex) {
//
//            }
//            rescalebitmap.compress(Bitmap.CompressFormat.PNG, 100, outputstream);
//            try {
//                outputstream.flush();
//            } catch (Exception ex) {
//
//            }
//            try {
//                outputstream.close();
//            } catch (Exception ex) {
//
//            }

//            BitmapPool bitmapPool = Glide.get(itemView.getContext()).getBitmapPool();
//            int microSecond = 1000000;// 1th second as an example
//            BitmapDrawable bitmapDrawable=new BitmapDrawable();
//            Bitmap bitmap=bitmapDrawable.getBitmap();
//            Glide.with(itemView.getContext())
//                    .load(url)
//                    .override(300,300)// Example
//                    .into(binding.btnItem);


//            Bitmap icon = BitmapFactory.decodeResource(itemView.getResources(),
//                    R.mipmap.playinterfacevideoarrowbutton128w);
//            Bitmap scale=resizeBitmap(icon);
//            binding.btnItem.setImageBitmap(scale);


            BitmapDrawable bitmapDrawable=new BitmapDrawable();
            Bitmap bitmap=bitmapDrawable.getBitmap();
            Glide.with(itemView).load(url).into(binding.imgItem);
        }
        public void getImgThumbnailFromURL(String url) {

            Glide.with(itemView.getContext())
                    .asBitmap()
                    .load(url)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Bitmap scale=resizeBitmap(resource);
                            binding.imgItem.setImageBitmap(scale);
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }

        public void getFileThumbnailFromURL(String url) {
            Bitmap icon = BitmapFactory.decodeResource(itemView.getResources(),
                    R.mipmap.documentfiledocpaper2562);
            Bitmap scale=resizeBitmap(icon);
            binding.imgItem.setImageBitmap(scale);
//            Glide.with(itemView).load(url).into(binding.imgItem);

        }


        private static final float PREFERRED_WIDTH = 300;
        private static final float PREFERRED_HEIGHT = 300;
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
    }

}
