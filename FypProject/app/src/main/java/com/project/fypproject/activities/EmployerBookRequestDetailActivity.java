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

public class EmployerBookRequestDetailActivity extends AppCompatActivity {

    private TextView tvRequestID, tvRequestState, tvEmployerName, tvHelperName, tvTranName,tvAgentName;
    private ImageButton chatAgent;

    String requestID, requestState, employerName, helperName, translatorEmail, employerEmail, employeeEmail;
    private String agentEmail;
    LinearLayout layoutEmployerSelectedTime,layoutConfirmedTime;

    private FirebaseFirestore db;

    Button btnSendDecline,btnBook,btnCheckBook;

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_book_request_detail);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        tvRequestID = findViewById(R.id.tvRequestID);
        tvRequestState = findViewById(R.id.tvRequestState);
        tvEmployerName = findViewById(R.id.tvEmployerName);
        tvHelperName = findViewById(R.id.tvHelperName);
        tvTranName = findViewById(R.id.tvTranName);
        chatAgent = findViewById(R.id.chatAgent);
        layoutEmployerSelectedTime = findViewById(R.id.layoutEmployerSelectedTime);
        layoutConfirmedTime = findViewById(R.id.layoutConfirmedTime);
        btnCheckBook = findViewById(R.id.btnCheckBook);
        tvAgentName = findViewById(R.id.tvAgentName);

        Intent intent = getIntent();
        requestID = intent.getStringExtra("requestID");
        requestState = intent.getStringExtra("requestState");
        employerName = intent.getStringExtra("employerName");
        helperName = intent.getStringExtra("helperName");

        tvRequestID.setText("Request ID: " + requestID);
        tvRequestState.setText("Request State: " + requestState);
        tvEmployerName.setText("Employer Name: " + employerName);
        tvHelperName.setText("DomesticHelper Name: " + helperName);

        getInfo();

        chatAgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChat(agentEmail);
            }
        });

        btnCheckBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployerBookRequestDetailActivity.this, BookRecordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        if ("Pending Confirmation".equals(requestState)) {
            layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
            getSelectedTime(requestID, "employerSelectedTime", layoutEmployerSelectedTime);
        }else if ("Final Time Confirmed".equals(requestState)){
            layoutConfirmedTime.setVisibility(View.VISIBLE);
            btnCheckBook.setVisibility(View.VISIBLE);
            getSelectedTime(requestID, "confirmedTime", layoutConfirmedTime);
        }else{
            layoutEmployerSelectedTime.setVisibility(View.GONE);
        }
    }

    private void getInfo() {
        db.collection("interview_request")
                .whereEqualTo("docId", requestID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            translatorEmail = querySnapshot.getDocuments().get(0).getString("translatorEmail");
                            employerEmail = querySnapshot.getDocuments().get(0).getString("employerEmail");
                            employeeEmail = querySnapshot.getDocuments().get(0).getString("employeeEmail");
                            agentEmail = querySnapshot.getDocuments().get(0).getString("agentEmail");

                            if (translatorEmail != null && !translatorEmail.isEmpty()) {
                                db.collection("users")
                                        .whereEqualTo("email", translatorEmail)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> userTask) {
                                                if (userTask.isSuccessful() && !userTask.getResult().isEmpty()) {
                                                    String firstName = userTask.getResult().getDocuments().get(0).getString("firstName");
                                                    String lastName = userTask.getResult().getDocuments().get(0).getString("lastName");
                                                    tvTranName.setText("Translator Name: " + lastName + " " + firstName);
                                                } else {
                                                    Log.e("Translator", "Translator Not Found");
                                                }
                                            }
                                        });
                            } else {
                                Log.e("Translator", "Translator Email empty");
                            }

                            if (agentEmail != null && !agentEmail.isEmpty()) {
                                db.collection("users")
                                        .whereEqualTo("email", agentEmail)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> agentTask) {
                                                if (agentTask.isSuccessful() && !agentTask.getResult().isEmpty()) {
                                                    String firstName = agentTask.getResult().getDocuments().get(0).getString("firstName");
                                                    String lastName = agentTask.getResult().getDocuments().get(0).getString("lastName");
                                                    TextView tvAgentName = findViewById(R.id.tvAgentName);
                                                    tvAgentName.setText("Agent Name: " + lastName + " " + firstName);
                                                } else {
                                                    Log.e("Agent", "Agent Not Found");
                                                }
                                            }
                                        });
                            } else {
                                Log.e("Agent", "Agent Email empty");
                            }
                        } else {
                            Log.e("Firestore", "Request Document Not Found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error fetching request info", e);
                    }
                });
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

                                    Intent intent = new Intent(EmployerBookRequestDetailActivity.this, ChatActivity.class);
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

    private void getSelectedTime(String requestId, String timeType, LinearLayout targetLayout) {
        db.collection("interview_request")
                .whereEqualTo("docId", requestId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            Map<String, Object> selectedTime = (Map<String, Object>) document.get(timeType);

                            if (selectedTime != null) {
                                for (String date : selectedTime.keySet()) {
                                    Map<String, Boolean> timeSlots = (Map<String, Boolean>) selectedTime.get(date);
                                    addDateAndTimeSlotsToLayout(date, timeSlots, targetLayout);
                                }
                            }
                        } else {
                            Log.e("Firestore", "Failed to fetch " + timeType, task.getException());
                        }
                    }
                });
    }

    private void addDateAndTimeSlotsToLayout(String date, Map<String, Boolean> timeSlots, LinearLayout parentLayout) {
        TextView dateTextView = new TextView(this);
        dateTextView.setText(date);
        dateTextView.setTextSize(18);
        dateTextView.setTextColor(getResources().getColor(R.color.black));
        dateTextView.setPadding(0, 16, 0, 8);
        dateTextView.setTypeface(null, Typeface.BOLD);
        parentLayout.addView(dateTextView);

        List<String> sortedTimeSlots = new ArrayList<>(timeSlots.keySet());
        Collections.sort(sortedTimeSlots);

        for (String timeSlot : sortedTimeSlots) {
            if (timeSlots.get(timeSlot)) {
                TextView timeSlotTextView = new TextView(this);
                timeSlotTextView.setText(timeSlot);
                timeSlotTextView.setTextSize(16);
                timeSlotTextView.setTextColor(getResources().getColor(R.color.black));
                timeSlotTextView.setPadding(0, 4, 0, 4);
                parentLayout.addView(timeSlotTextView);
            }
        }
    }
}