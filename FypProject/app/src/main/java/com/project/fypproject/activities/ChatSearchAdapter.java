package com.project.fypproject.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.project.fypproject.R;
import com.project.fypproject.models.ChatModel;

public class ChatSearchAdapter extends FirestoreRecyclerAdapter<ChatModel,ChatSearchAdapter.ChatModelViewHolder> {
    Context context;
    public ChatSearchAdapter(@NonNull FirestoreRecyclerOptions<ChatModel> options,Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatModel model) {
        holder.tvName.setText((model.getLastName() + model.getFirstName()));
        holder.tvEmail.setText(model.getEmail());


    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_search_list,parent,false);
        return new ChatModelViewHolder(view);
    }

    class ChatModelViewHolder extends RecyclerView.ViewHolder{
        TextView tvName,tvEmail;
        ImageView ImgIcon;

        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.Email);
            ImgIcon = itemView.findViewById(R.id.ImgIcon);

        }
    }
}
