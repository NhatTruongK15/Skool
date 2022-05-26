package com.example.clown.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.R;
import com.example.clown.activities.FileDisplayActivitiy;
import com.example.clown.databinding.ItemMediaAndFileBinding;
import com.example.clown.models.MediaAndFile;

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
    public static class MediaAndFileViewHolder extends RecyclerView.ViewHolder {

        private ItemMediaAndFileBinding binding;

        public MediaAndFileViewHolder(ItemMediaAndFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

        void setData(MediaAndFile mediaAndFile) {
            if (mediaAndFile.imgPath != null) {
                if(mediaAndFile.imgPath.compareTo("")!=0){
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
    }
}
