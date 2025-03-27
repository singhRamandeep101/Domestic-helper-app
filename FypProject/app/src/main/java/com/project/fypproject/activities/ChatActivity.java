package com.project.fypproject.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;
import com.project.fypproject.models.ChatMessageModel;
import com.project.fypproject.models.ChatModel;
import com.project.fypproject.models.ChatRoom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    ChatModel otherUser;
    EditText edMessage;
    ImageView btnBack;
    FrameLayout btnSend;
    TextView tvName;
    RecyclerView recyclerView;
    ChatAdapter adapter;
    String chatroomId;
    ChatRoom chatRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        otherUser = ChatUtil.getUserModelIntent(getIntent());
        chatroomId = ChatUtil.getChatroomId(ChatUtil.currentUserEmail(), otherUser.getEmail());
        tvName = findViewById(R.id.tvName);
        edMessage = findViewById(R.id.inputBox);
        btnSend = findViewById(R.id.layoutSend);
        btnBack = findViewById(R.id.imgBack);
        recyclerView = findViewById(R.id.rv_chat);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tvName.setText(otherUser.getLastName() + " " + otherUser.getFirstName());

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = edMessage.getText().toString();
                if(message.isEmpty())
                    return;
                sendMessage(message);
            }
        });

        getChatroom();
        setChat();
    }
    void setChat(){
        Query query = ChatUtil.getChatroomMessageReference(chatroomId).orderBy("timestamp",Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>().setQuery(query,ChatMessageModel.class).build();
        adapter = new ChatAdapter(options,getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });

    }
    void sendMessage(String message){
        chatRoom.setLastMessageTimestamp(Timestamp.now());
        chatRoom.setLastMessageSenderId(ChatUtil.currentUserEmail());
        chatRoom.setLastMessage(message);
        ChatUtil.getChatroomReference(chatroomId).set(chatRoom);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message,ChatUtil.currentUserEmail(),Timestamp.now());
        ChatUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful()){
                    edMessage.setText("");
                }
            }
        });

    }
    void getChatroom(){
        ChatUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    chatRoom = task.getResult().toObject(ChatRoom.class);
                    if(chatRoom==null){
                        chatRoom = new ChatRoom(chatroomId, Arrays.asList(ChatUtil.currentUserEmail(),otherUser.getEmail()), Timestamp.now(),"");
                        ChatUtil.getChatroomReference(chatroomId).set(chatRoom);
                    }
                    String firstMessage = getIntent().getStringExtra("message");
                    if (firstMessage!=null && !firstMessage.isEmpty()){
                        sendMessage(firstMessage);
                    }
                }
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
