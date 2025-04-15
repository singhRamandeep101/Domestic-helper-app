package com.project.fypproject.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;
import com.project.fypproject.models.ChatModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingDetailActivity extends AppCompatActivity {

    TextView tvDate,tvTime,tvAgentName,tvEmployerName,tvHelperName,tvTranslatorName;

    String  bookingID,date,time,meetingState,agentName,employerName,helperName,translatorName,agentEmail,employerEmail,employeeEmail,translatorEmail,userType;

    FirebaseAuth auth;
    FirebaseUser user;

    ImageButton chatEmployer,chatHelper,chatTranslator,chatAgent;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        tvDate = findViewById(R.id.tvDate);
        tvTime =  findViewById(R.id.tvTime);
        tvAgentName = findViewById(R.id.tvAgentName);
        tvEmployerName = findViewById(R.id.tvEmployerName);
        tvHelperName = findViewById(R.id.tvHelperName);
        tvTranslatorName = findViewById(R.id.tvTranName);
        chatEmployer = findViewById(R.id.chatEmployer);
        chatHelper = findViewById(R.id.chatHelper);
        chatTranslator = findViewById(R.id.chatTranslator);
        chatAgent = findViewById(R.id.chatAgent);


        Intent intent = getIntent();
        bookingID = intent.getStringExtra("bookingID");
        date = intent.getStringExtra("date");
        time = intent.getStringExtra("timeSlot");
        meetingState = intent.getStringExtra("meetingStatus");
        agentName = intent.getStringExtra("agentName");
        employerName = intent.getStringExtra("employerName");
        helperName = intent.getStringExtra("employeeName");
        translatorName = intent.getStringExtra("translatorName");
        agentEmail = intent.getStringExtra("agentEmail");
        employerEmail = intent.getStringExtra("employerEmail");
        employeeEmail = intent.getStringExtra("employeeEmail");
        translatorEmail = intent.getStringExtra("translatorEmail");
        userType = intent.getStringExtra("userType");


        tvDate.setText("Date: " + date);
        tvTime.setText("Time: " + time);
        tvAgentName.setText("Agent Name: " + agentName);
        tvEmployerName.setText("Employer Name: " + employerName);
        tvHelperName.setText("DomesticHelper Name: " + helperName);
        tvTranslatorName.setText("Translator Name: " + translatorName);

        chatAgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChat(agentEmail);
            }
        });

        chatEmployer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChat(employerEmail);
            }
        });

        chatHelper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChat(employeeEmail);
            }
        });

        chatTranslator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChat(translatorEmail);
            }
        });

        if ("Agent".equals(userType)) {
            chatAgent.setVisibility(View.GONE);
            chatEmployer.setVisibility(View.VISIBLE);
            chatHelper.setVisibility(View.VISIBLE);
            chatTranslator.setVisibility(View.VISIBLE);
        }else {
            chatAgent.setVisibility(View.VISIBLE);
            chatEmployer.setVisibility(View.GONE);
            chatHelper.setVisibility(View.GONE);
            chatTranslator.setVisibility(View.GONE);
        }

    }

    private void openChat(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            QuerySnapshot Docs = task.getResult();

                            if (!Docs.isEmpty()) {
                                DocumentSnapshot Doc = Docs.getDocuments().get(0);

                                String firstName = Doc.getString("firstName");
                                String lastName = Doc.getString("lastName");
                                String userType = Doc.getString("userType");

                                if (firstName != null && lastName != null) {
                                    ChatModel chatModel = new ChatModel();
                                    chatModel.setEmail(email);
                                    chatModel.setFirstName(firstName);
                                    chatModel.setLastName(lastName);
                                    chatModel.setUserType(userType);

                                    Intent intent = new Intent(BookingDetailActivity.this, ChatActivity.class);
                                    ChatUtil.passUserIntent(intent, chatModel);
                                    startActivity(intent);

                                    Log.d("Firestore", "ChatModel set with agent details: " +
                                            "FirstName: " + firstName + ", LastName: " + lastName +
                                            ", AgentEmail: " + email);

                                } else {
                                    Log.e("Firestore", "Agent details are incomplete for email: " + email);
                                }
                            } else {
                                Log.e("Firestore", "No document found for Email: " + email);
                            }
                        } else {
                            Log.e("Firestore", "Failed to query users collection", task.getException());
                        }
                    }
                });
    }
}