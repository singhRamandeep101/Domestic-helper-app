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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;
import com.project.fypproject.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private List<ChatMessage> chatMessages;

    private ChatAdapter chatAdapter;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private TextView tvName;
    private EditText editMessage;

    String agentEmail;
    String senderEmail;
    String employeeEmail;

    private Button btnBack;
    private FrameLayout layoutSend;
    private ProgressBar pgb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        tvName = findViewById(R.id.tvName);
        editMessage = findViewById(R.id.inputBox);
        layoutSend = findViewById(R.id.layoutSend);
        recyclerView = findViewById(R.id.rv_chat);
        pgb = findViewById(R.id.pgBar);

        layoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        agentEmail = getIntent().getStringExtra("email");

        if (agentEmail.equals("agentd@gmail.com")) {
            employeeEmail= "youtube@gmail.com";
        }else{
            employeeEmail="agentd@gmail.com";
        }

        senderEmail = user.getEmail();

        db = FirebaseFirestore.getInstance();


        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages,getDrawable(R.drawable.icon_employer),senderEmail);
        recyclerView.setAdapter(chatAdapter);
        listMessage();

//        db.collection("MaidInfo")
//                .whereEqualTo("email", "a")
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (!queryDocumentSnapshots.isEmpty()) {
//
//                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
//                            agentEmail = (String) doc.get("agentEmail");
//
//                            if (agentEmail != null) {
                                db.collection("users")
                                        .whereEqualTo("email", employeeEmail)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot agentSnapshots) {
                                                if (!agentSnapshots.isEmpty()) {
                                                    DocumentSnapshot agentDoc = agentSnapshots.getDocuments().get(0);
                                                    String fName = (String) agentDoc.get("firstName");
                                                    String lName = (String) agentDoc.get("lastName");
                                                    tvName.setText(lName + " " + fName);
                                                } else {
                                                    Log.e("Firestore", "Error find Agent Name");
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("Firestore", "Error find Agent Data", e);
                                            }
                                        });
//                            } else {
//                                Log.e("Firestore", "no find agent Email");
//                           }
//                        }
//                   }
//                });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void sendMessage(){
        HashMap<String,Object> message = new HashMap<>();
        message.put("senderEmail",senderEmail);
        message.put("receiverEmail",employeeEmail);
        message.put("message",editMessage.getText().toString());
        message.put("timestamp",new Date());
        db.collection("chat").add(message);
        editMessage.setText(null);
    }

    private void listMessage(){
        db.collection("chat")
                .whereEqualTo("senderEmail",senderEmail)
                .whereEqualTo("receiverEmail",employeeEmail)
                .addSnapshotListener(eventListener);
        db.collection("chat")
                .whereEqualTo("senderEmail",employeeEmail)
                .whereEqualTo("receiverEmail",senderEmail)
                .addSnapshotListener(eventListener);

    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error!=null){
            return;
        }
        if(value!=null){
            int count = chatMessages.size();
            for(DocumentChange documentChange: value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderEmail = documentChange.getDocument().getString("senderEmail");
                    chatMessage.receiverEmail = documentChange.getDocument().getString("receiverEmail");
                    chatMessage.message = documentChange.getDocument().getString("message");
                    chatMessage.dataTime = getDateTime(documentChange.getDocument().getDate("timestamp"));
                    chatMessage.dateObject = documentChange.getDocument().getDate("timestamp");
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages,(obj1,obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if(count==0){
                chatAdapter.notifyDataSetChanged();
            }else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
                recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            recyclerView.setVisibility(View.VISIBLE);
        }
        pgb.setVisibility(View.GONE);
    };

    private Bitmap getBitmapEncode(String encodeImage) {
        byte[] bytes = Base64.decode(encodeImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
    private String getDateTime(Date date){
        return new SimpleDateFormat("MMMM dd,yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}