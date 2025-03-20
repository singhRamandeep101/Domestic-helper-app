package com.project.fypproject.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.fypproject.databinding.ItemReceMessageBinding;
import com.project.fypproject.databinding.ItemSendMessageBinding;
import com.project.fypproject.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    private final Drawable reImage;
    private final String senderEmail;
    public static final int VIEW_TYPE_SEND = 1;
    public static final int VIEW_TYPE_REC = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, Drawable reImage, String senderEmail) {
        this.chatMessages = chatMessages;
        this.reImage = reImage;
        this.senderEmail = senderEmail;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SEND){
            return new SendMessageViewHolder(ItemSendMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }else{
            return new ReceivedMessageViewHolder(ItemReceMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SEND){
            ((SendMessageViewHolder) holder).setData(chatMessages.get(position));
        }else{
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position),reImage );
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderEmail.equals(senderEmail)){
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

        void setData(ChatMessage chatMessage,Drawable reImage){
            binding.tvMessage.setText(chatMessage.message);
            binding.tvDateTime.setText(chatMessage.dataTime);
            binding.imgProfile.setImageDrawable(reImage);
        }
    }
}
