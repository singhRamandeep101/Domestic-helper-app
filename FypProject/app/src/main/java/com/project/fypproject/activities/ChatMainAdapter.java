package com.project.fypproject.activities;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.project.fypproject.R;
import com.project.fypproject.models.ChatModel;
import com.project.fypproject.models.ChatRoom;

public class ChatMainAdapter extends FirestoreRecyclerAdapter<ChatRoom,ChatMainAdapter.ChatRoomViewHolder> {
    Context context;
    public ChatMainAdapter(@NonNull FirestoreRecyclerOptions<ChatRoom> options,Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position, @NonNull ChatRoom model) {
        ChatUtil.getOtherUserFromChatroom(model.getUserEmails()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    boolean lastMessageSendByMe = model.getLastMessageSenderId().equals(ChatUtil.currentUserEmail());

                    ChatModel otherUserModel = task.getResult().toObject(ChatModel.class);
                    holder.tvName.setText(otherUserModel.getLastName() + " " + otherUserModel.getFirstName());
                    if (lastMessageSendByMe)
                        holder.tvLastMessage.setText("You : " + model.getLastMessage());
                    else
                        holder.tvLastMessage.setText(model.getLastMessage());
                    holder.tvLastMessageTime.setText(ChatUtil.timestampToString(model.getLastMessageTimestamp()));

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, ChatActivity.class);
                            ChatUtil.passUserIntent(intent,otherUserModel);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    });
            }
        }

        });

    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list,parent,false);
        return new ChatRoomViewHolder(view);
    }

    class ChatRoomViewHolder extends RecyclerView.ViewHolder{
        TextView tvName,tvLastMessage,tvLastMessageTime;
        ImageView ImgIcon;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvLastMessageTime = itemView.findViewById(R.id.tvLastMessageTime);

        }
    }
}
