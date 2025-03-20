package com.project.fypproject.activities;

import android.graphics.Bitmap;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.fypproject.databinding.ItemReceMessageBinding;
import com.project.fypproject.databinding.ItemSendMessageBinding;
import com.project.fypproject.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    private final Bitmap reImage;
    private final String senderId;
    public static final int VIEW_TYPE_SEND = 1;
    public static final int VIEW_TYPE_REC = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap reImage, String senderId) {
        this.chatMessages = chatMessages;
        this.reImage = reImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SEND;
        }else{
            return VIEW_TYPE_REC;
        }
    }

    static class SendMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemSendMessageBinding binding;

        SendMessageViewHolder(ItemSendMessageBinding itemSendMessageBinding){
            super(itemSendMessageBinding.getRoot());
            binding = itemSendMessageBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.tvMessage.setText(chatMessage.message);
            binding.tvDateTime.setText(chatMessage.dataTime);
        }
    }
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemReceMessageBinding binding;

        ReceivedMessageViewHolder(ItemReceMessageBinding itemReceMessageBinding){
            super(itemReceMessageBinding.getRoot());
            binding = itemReceMessageBinding;
        }

        void setData(ChatMessage chatMessage,Bitmap reImage){
            binding.tvMessage.setText(chatMessage.message);
            binding.tvDateTime.setText(chatMessage.dataTime);
            binding.imgProfile.setImageBitmap(reImage);
        }
    }
}
