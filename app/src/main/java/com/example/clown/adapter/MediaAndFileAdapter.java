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
import com.example.clown.models.MediaAndFile;

import java.util.List;

public class MediaAndFileAdapter extends RecyclerView.Adapter<MediaAndFileAdapter.MediaAndFileViewHolder> {
    private List<MediaAndFile> mediaandfile;
    private Context context;

    public MediaAndFileAdapter(Context context, List<MediaAndFile> mediaandfile) {
        this.context = context;
        this.mediaandfile = mediaandfile;
    }

    @Override
    public int getItemCount() {
        return mediaandfile == null ? 0 : mediaandfile.size();
    }

    @Override
    public MediaAndFileAdapter.MediaAndFileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media_and_file, parent, false);

        return new MediaAndFileViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {

        return 0;
    }

    @Override
    public void onBindViewHolder(MediaAndFileAdapter.MediaAndFileViewHolder holder, int position) {
        String vidPath=mediaandfile.get(position).getVidPath();
        String imgPath=mediaandfile.get(position).getImgPath();
        String filePath=mediaandfile.get(position).getFilePath();
        String finame=mediaandfile.get(position).getFiname();

        if (imgPath != null) {
            holder.imgbtnItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, FileDisplayActivitiy.class);
                    intent.putExtra("imgPath", imgPath);
                    intent.putExtra("finame", finame);
                    intent.putExtra("fiPath", "");
                    intent.putExtra("vidPath", "");
                    context.startActivity(intent);
                }
            });
            return;
        }
        if(vidPath!=null){
            if (vidPath.compareTo("") != 0) {
                holder.imgbtnItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, FileDisplayActivitiy.class);
                        intent.putExtra("vidPath", vidPath);
                        intent.putExtra("finame", finame);
                        intent.putExtra("fiPath", "");
                        intent.putExtra("imgPath", "");
                        context.startActivity(intent);
                    }
                });
            }
        }
        if(filePath!=null){
            if (filePath.compareTo("") != 0) {
                holder.imgbtnItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, FileDisplayActivitiy.class);
                        intent.putExtra("fiPath", filePath);
                        intent.putExtra("finame", finame);
                        intent.putExtra("imgPath", "");
                        intent.putExtra("vidPath", "");
                        context.startActivity(intent);
                    }
                });
            }

        }
    }

    /**
     * Data ViewHolder class.
     */
    public static class MediaAndFileViewHolder extends RecyclerView.ViewHolder {

        private ImageButton imgbtnItem;

        private  TextView textView;
        public MediaAndFileViewHolder(View itemView) {
            super(itemView);
            imgbtnItem = (ImageButton) itemView.findViewById(R.id.imgbtnItem);

            textView=(TextView) itemView.findViewById(R.id.txtvItem);
        }
    }
}
