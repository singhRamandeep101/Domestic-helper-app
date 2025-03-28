package com.project.fypproject.activities;

import android.content.Intent;
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
import androidx.appcompat.widget.AppCompatImageView;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
    FrameLayout btnSend, btnBook;
    TextView tvName;
    RecyclerView recyclerView;
    ChatAdapter adapter;
    String chatroomId;
    ChatRoom chatRoom;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;

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
        btnBook = findViewById(R.id.layoutBook);
        btnBack = findViewById(R.id.imgBack);
        recyclerView = findViewById(R.id.rv_chat);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        changeIcon();

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
                if (message.isEmpty())
                    return;
                sendMessage(message);
            }
        });

        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUser();
            }
        });

        getChatroom();
        setChat();
    }

    void setChat() {
        Query query = ChatUtil.getChatroomMessageReference(chatroomId).orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>().setQuery(query, ChatMessageModel.class).build();
        adapter = new ChatAdapter(options, getApplicationContext());
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

    void sendMessage(String message) {
        chatRoom.setLastMessageTimestamp(Timestamp.now());
        chatRoom.setLastMessageSenderId(ChatUtil.currentUserEmail());
        chatRoom.setLastMessage(message);
        ChatUtil.getChatroomReference(chatroomId).set(chatRoom);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message, ChatUtil.currentUserEmail(), Timestamp.now());
        ChatUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    edMessage.setText("");
                }
            }
        });

    }

    void getChatroom() {
        String employerEmail = getIntent().getStringExtra("employerEmail");
        ChatUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    chatRoom = task.getResult().toObject(ChatRoom.class);
                    if (chatRoom == null) {
                        String employeeEmail = getIntent().getStringExtra("employeeEmail");
                        String agentEmail = getIntent().getStringExtra("agentEmail");

                        if (employeeEmail != null && !employeeEmail.isEmpty() && agentEmail != null && !agentEmail.isEmpty() && employerEmail != null && !employerEmail.isEmpty()) {
                            chatRoom = new ChatRoom(chatroomId, Arrays.asList(ChatUtil.currentUserEmail(), otherUser.getEmail()), Timestamp.now(), "", employeeEmail, agentEmail,employerEmail);
                            ChatUtil.getChatroomReference(chatroomId).set(chatRoom);
                        } else {
                            chatRoom = new ChatRoom(chatroomId, Arrays.asList(ChatUtil.currentUserEmail(), otherUser.getEmail()), Timestamp.now(), "", null, null,null);
                            ChatUtil.getChatroomReference(chatroomId).set(chatRoom);
                        }
                    }
                    String firstMessage = getIntent().getStringExtra("message");
                    if (firstMessage != null && !firstMessage.isEmpty()) {
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

    void changeIcon() {
        db.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 获取 userType
                                String userType = document.getString("userType");

                                FrameLayout layoutBook = findViewById(R.id.layoutBook);
                                AppCompatImageView imageView = (AppCompatImageView) layoutBook.getChildAt(0);

                                if ("Employer".equals(userType)) {
                                    imageView.setImageResource(R.drawable.ic_select_date);
                                    layoutBook.setVisibility(View.VISIBLE);
                                } else if ("Agent".equals(userType)) {
                                    imageView.setImageResource(R.drawable.ic_book_date);
                                    layoutBook.setVisibility(View.VISIBLE);
                                } else {
                                    layoutBook.setVisibility(View.GONE);
                                }

                            }
                        }
                    }
                });
    }

    void checkUser() {
        db.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 获取 userType
                                String userType = document.getString("userType");

                                if ("Employer".equals(userType)) {
                                    db.collection("chatrooms")
                                            .document(chatroomId)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful() && task.getResult().exists()) {
                                                        String employeeEmail = task.getResult().getString("employeeEmail");
                                                        String agentEmail = task.getResult().getString("agentEmail");

                                                        if (employeeEmail != null && !employeeEmail.isEmpty() && agentEmail != null && !agentEmail.isEmpty()) {
                                                            Intent intent = new Intent(ChatActivity.this, EmployerSelectTimeActivity.class);
                                                            intent.putExtra("agentEmail", agentEmail);
                                                            intent.putExtra("employeeEmail", employeeEmail);
                                                            startActivity(intent);
                                                        } else {
                                                            Log.e("checkUser", "employeeEmail is null or empty");
                                                        }
                                                    } else {
                                                        Log.e("checkUser", "Chatroom not found or error occurred");
                                                    }
                                                }
                                            });

                                } else {
                                    db.collection("chatrooms")
                                            .document(chatroomId)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful() && task.getResult().exists()) {
                                                        String employeeEmail = task.getResult().getString("employeeEmail");
                                                        String agentEmail = task.getResult().getString("agentEmail");
                                                        String employerEmail = task.getResult().getString("employerEmail");

                                                        if (employeeEmail != null && !employeeEmail.isEmpty() && agentEmail != null && !agentEmail.isEmpty() && employerEmail != null && !employerEmail.isEmpty()) {
                                                            Intent intent = new Intent(ChatActivity.this, AgentBookTimeActivity.class);
                                                            intent.putExtra("agentEmail", agentEmail);
                                                            intent.putExtra("employeeEmail", employeeEmail);
                                                            intent.putExtra("employerEmail", employerEmail);
                                                            startActivity(intent);
                                                        } else {
                                                            Log.e("checkUser", "employeeEmail is null or empty");
                                                        }
                                                    } else {
                                                        Log.e("checkUser", "Chatroom not found or error occurred");
                                                    }
                                                }
                                            });
                                }
                            }
                        } else {
                            Log.e("checkUser", "Error fetching user data or no user found");
                        }
                    }
                });
    }
}
