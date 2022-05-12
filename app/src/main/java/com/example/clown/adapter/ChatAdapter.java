package com.example.clown.adapter;

import static com.example.clown.utilities.Constants.HD_RES;
import static com.example.clown.utilities.Constants.HD_RES_860;
import static com.example.clown.utilities.Constants.PIC_HOLDER;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clown.R;
import com.example.clown.activities.FileDisplayActivitiy;
import com.example.clown.activities.MainActivity;
import com.example.clown.databinding.ItemContainerReceivedMessageBinding;
import com.example.clown.databinding.ItemContainerSentMessageBinding;
import com.example.clown.models.ChatMessage;

import java.io.ByteArrayOutputStream;
import java.util.List;
import android.content.Intent;

public class ChatAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final String senderId;
    private Bitmap receiverProfileImage;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap) {
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, Bitmap receiverProfileImage) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.receiverProfileImage = receiverProfileImage;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;
        public int position = -1;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;

        }

        public byte[] BitmapToByte(Bitmap bitmap, int quality) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, stream);
            return stream.toByteArray();
        }

        void setData(ChatMessage chatMessage) {
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.imgMessage.setImageBitmap(chatMessage.message_img);
            binding.textMessage.setText(chatMessage.message);

            if (chatMessage.videoPath != null && chatMessage.videoPath.compareTo("") != 0) {
                binding.textMessage.setVisibility(View.GONE);

                chatMessage.mediaController = new MediaController(itemView.getContext());
                chatMessage.mediaController.setAnchorView(binding.vidMessage);
                binding.vidMessage.setMediaController(chatMessage.mediaController);

                // implement on completion listener on video view
                binding.vidMessage.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Toast.makeText(itemView.getContext(), "Thank You...!!!", Toast.LENGTH_SHORT).show();
                    }
                });
                binding.vidMessage.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Toast.makeText(itemView.getContext(), "Oops An Error Occur While Playing Video...!!!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
                binding.vidMessage.setVideoURI(Uri.parse(chatMessage.videoPath));
            } else {
                binding.vidMessage.setVisibility(View.GONE);
                binding.vidMessage.setLayoutParams(new FrameLayout.LayoutParams(0, 0));

            }


            if (chatMessage.message_img != null) {
                binding.btnMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context context = itemView.getContext();
                        Intent intent = new Intent(context, FileDisplayActivitiy.class);
                        intent.putExtra("imgPath", chatMessage.message_img_link);
                        intent.putExtra("finame", chatMessage.finame);

                        intent.putExtra("vidPath", "");
                        context.startActivity(intent);
                    }
                });
                return;
            }

            if (chatMessage.videoPath.compareTo("") != 0) {
                binding.btnMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = itemView.getContext();
                        Intent intent = new Intent(context, FileDisplayActivitiy.class);
                        intent.putExtra("vidPath", chatMessage.videoPath);
                        intent.putExtra("finame", chatMessage.finame);
                        intent.putExtra("imgPath", "");
                        context.startActivity(intent);
                    }
                });
            }
            if (chatMessage.message.compareTo("") != 0) {
                binding.btnMore.setVisibility(View.GONE);
            }


        }


        private static String encodeImage(Bitmap bitmap) {
            int previewWidth = HD_RES;
            int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
            Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
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

    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;
        public int position = -1;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.imgMessage.setImageBitmap(chatMessage.message_img);
            binding.textMessage.setText(chatMessage.message);

            if (chatMessage.videoPath != null && chatMessage.videoPath.compareTo("") != 0) {
                binding.textMessage.setVisibility(View.GONE);

                chatMessage.mediaController = new MediaController(itemView.getContext());
                chatMessage.mediaController.setAnchorView(binding.vidMessage);
                binding.vidMessage.setMediaController(chatMessage.mediaController);

                // implement on completion listener on video view
                binding.vidMessage.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Toast.makeText(itemView.getContext(), "Thank You...!!!", Toast.LENGTH_SHORT).show();
                    }
                });
                binding.vidMessage.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Toast.makeText(itemView.getContext(), "Oops An While Playing Video...!!!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
                binding.vidMessage.setVideoURI(Uri.parse(chatMessage.videoPath));
            } else {
                binding.vidMessage.setVisibility(View.GONE);
                binding.vidMessage.setLayoutParams(new FrameLayout.LayoutParams(1, 1));
            }


            if (chatMessage.message_img != null) {
                binding.btnMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context context = itemView.getContext();
                        Intent intent = new Intent(context, FileDisplayActivitiy.class);
                        intent.putExtra("imgPath", chatMessage.message_img_link);
                        intent.putExtra("finame", chatMessage.finame);
                        intent.putExtra("vidPath", "");
                        context.startActivity(intent);
                    }
                });
                return;
            }

            if(chatMessage.videoPath!=null) {
                if (chatMessage.videoPath.compareTo("") != 0) {
                    binding.btnMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Context context = itemView.getContext();
                            Intent intent = new Intent(context, FileDisplayActivitiy.class);
                            intent.putExtra("vidPath", chatMessage.videoPath);
                            intent.putExtra("finame", chatMessage.finame);
                            intent.putExtra("imgPath", "");
                            context.startActivity(intent);
                        }
                    });
                }
            }
            if (chatMessage.message.compareTo("") != 0) {
                binding.btnMore.setVisibility(View.GONE);
            }

        }

    }

    public byte[] BitmapToByte(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, stream);
        return stream.toByteArray();
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = HD_RES;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
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


}

