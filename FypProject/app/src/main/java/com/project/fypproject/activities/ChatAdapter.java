package com.project.fypproject.activities;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.project.fypproject.R;
import com.project.fypproject.models.ChatMessageModel;
import com.project.fypproject.models.ChatModel;

public class ChatAdapter extends FirestoreRecyclerAdapter<ChatMessageModel,ChatAdapter.ChatModelViewHolder> {
    Context context;
    public ChatAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options,Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {
        if(model.getSenderId().equals(ChatUtil.currentUserEmail())){
            holder.left_chat.setVisibility(View.GONE);
            holder.right_chat.setVisibility(View.VISIBLE);
            holder.tvRight.setText(model.getMessage());
        }else{
            holder.right_chat.setVisibility(View.GONE);
            holder.left_chat.setVisibility(View.VISIBLE);
            holder.tvLeft.setText(model.getMessage());
        }
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_send_message,parent,false);
        return new ChatModelViewHolder(view);
    }

    class ChatModelViewHolder extends RecyclerView.ViewHolder{
        LinearLayout left_chat,right_chat;
        TextView tvLeft,tvRight;

        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);
            left_chat = itemView.findViewById(R.id.left_chat);
            right_chat = itemView.findViewById(R.id.right_chat);
            tvLeft = itemView.findViewById(R.id.tvLeft);
            tvRight = itemView.findViewById(R.id.tvRight);

        }
    }
}
