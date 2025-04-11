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
import com.google.android.gms.tasks.Task;
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

public class TranslatorBookRequestDetailActivity extends AppCompatActivity {

    LinearLayout layoutEmployerSelectedTime, layoutConfirmTime,layoutSelectedTime,layoutConfirmedTime;
    private Button btnSendDecline, btnSetBook,btnCheckBook;

    private TextView tvRequestID, tvRequestState, tvEmployerName, tvHelperName, tvTranName,tvAgentName;

    private ImageButton chatAgent;

    private FirebaseFirestore db;
    private String requestID,employerName, helperName,translatorEmail,agentEmail,employerEmail, employeeEmail;
    private String requestState;

    private Map<String, List<String>> selectedTimes = new HashMap<>();
    private Map<String, List<String>> confirmedTimes = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translator_book_request_detail);

        db = FirebaseFirestore.getInstance();

        layoutEmployerSelectedTime = findViewById(R.id.layoutEmployerSelectedTime);
        layoutConfirmTime = findViewById(R.id.layoutConfirmTime);
        btnSendDecline = findViewById(R.id.btnSendDecline);
        btnSetBook = findViewById(R.id.btnSetBook);
        tvRequestID = findViewById(R.id.tvRequestID);
        tvRequestState = findViewById(R.id.tvRequestState);
        tvAgentName = findViewById(R.id.tvAgentName);
        tvEmployerName = findViewById(R.id.tvEmployerName);
        tvHelperName = findViewById(R.id.tvHelperName);
        tvTranName = findViewById(R.id.tvTranName);
        chatAgent = findViewById(R.id.chatAgent);
        layoutSelectedTime = findViewById(R.id.layoutSelectedTime);
        layoutConfirmedTime = findViewById(R.id.layoutConfirmedTime);
        btnCheckBook = findViewById(R.id.btnCheckBook);

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

        btnSendDecline.setOnClickListener(v -> sendDeclineRequest());

        btnSetBook.setOnClickListener(v -> setBookingTime());

        btnCheckBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TranslatorBookRequestDetailActivity.this, BookRecordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        if ("Action Required".equals(requestState)) {
            layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
            btnSendDecline.setVisibility(View.VISIBLE);
            getSelectedTime("employerSelectedTime",layoutEmployerSelectedTime);
        }else if ("Final Time Confirmed".equals(requestState)) {
            layoutConfirmedTime.setVisibility(View.VISIBLE);
            btnCheckBook.setVisibility(View.VISIBLE);
            getSelectedTime("confirmedTime", layoutConfirmedTime);
        } else if ("Awaiting DomesticHelper".equals(requestState)) {
            layoutSelectedTime.setVisibility(View.VISIBLE);
            getSelectedTime("translatorSelectedTime", layoutSelectedTime);
        } else if ("Awaiting Agent".equals(requestState)) {
            layoutSelectedTime.setVisibility(View.VISIBLE);
            getSelectedTime("translatorSelectedTime", layoutSelectedTime);
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

                                    Intent intent = new Intent(TranslatorBookRequestDetailActivity.this, ChatActivity.class);
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

    private void getSelectedTime(String timeType, LinearLayout targetLayout) {
        db.collection("interview_request")
                .whereEqualTo("docId", requestID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        Map<String, Object> selectedTime = (Map<String, Object>) document.get(timeType);

                        if (selectedTime != null) {
                            for (String date : selectedTime.keySet()) {
                                Map<String, Boolean> timeSlots = (Map<String, Boolean>) selectedTime.get(date);

                                if(targetLayout.equals(layoutEmployerSelectedTime)){
                                    selectedTimes.put(date, new ArrayList<>(timeSlots.keySet()));
                                    addDateAndTimeSlotsToLayout(date, timeSlots, layoutEmployerSelectedTime);
                                }else{
                                    addDateAndTimeSlotsToLayout(date, timeSlots, targetLayout);
                                }
                            }
                        }
                    } else {
                        Log.e("Firestore", "Failed to fetch " + timeType, task.getException());
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
            LinearLayout timeLayout = new LinearLayout(this);
            timeLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView timeTextView = new TextView(this);
            timeTextView.setText(timeSlot);
            timeTextView.setTextSize(16);
            timeTextView.setTextColor(getResources().getColor(R.color.black));
            timeTextView.setPadding(0, 30, 0, 4);
            timeTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            if(parentLayout.equals(layoutEmployerSelectedTime)){
                ImageView yesIcon = new ImageView(this);
                yesIcon.setImageResource(R.drawable.icon_confirm);

                if (confirmedTimes.containsKey(date) && confirmedTimes.get(date).contains(timeSlot)) {
                    yesIcon.setVisibility(View.GONE);
                }

                yesIcon.setOnClickListener(v -> {
                    addConfirmedTime(date, timeSlot);
                    yesIcon.setVisibility(View.GONE);
                });

                timeLayout.addView(timeTextView);
                timeLayout.addView(yesIcon);
                parentLayout.addView(timeLayout);
            }else{
                timeLayout.addView(timeTextView);
                parentLayout.addView(timeLayout);
            }
        }
    }

    private void addConfirmedTime(String date, String time) {
        if (!confirmedTimes.containsKey(date)) {
            confirmedTimes.put(date, new ArrayList<>());
        }
        if (!confirmedTimes.get(date).contains(time)) {
            confirmedTimes.get(date).add(time);
        }

        updateConfirmedTimeUI();
    }

    private void updateConfirmedTimeUI() {
        layoutConfirmTime.removeAllViews();

        TextView titleTextView = new TextView(this);
        titleTextView.setText("Confirm Time");
        titleTextView.setTextSize(20);
        titleTextView.setTextColor(getResources().getColor(R.color.black));
        titleTextView.setTypeface(null, Typeface.BOLD);
        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setPadding(0, 16, 0, 8);
        layoutConfirmTime.addView(titleTextView);

        if (confirmedTimes.isEmpty()) {
            layoutConfirmTime.setVisibility(View.GONE);
            btnSetBook.setVisibility(View.GONE);
            btnSendDecline.setVisibility(View.VISIBLE);
        } else {
            layoutConfirmTime.setVisibility(View.VISIBLE);
            btnSetBook.setVisibility(View.VISIBLE);
            btnSendDecline.setVisibility(View.GONE);

            List<String> sortedDates = new ArrayList<>(confirmedTimes.keySet());
            Collections.sort(sortedDates);

            for (String date : sortedDates) {
                TextView dateTextView = new TextView(this);
                dateTextView.setText(date);
                dateTextView.setTextSize(18);
                dateTextView.setTextColor(getResources().getColor(R.color.black));
                dateTextView.setPadding(0, 16, 0, 8);
                dateTextView.setTypeface(null, Typeface.BOLD);
                layoutConfirmTime.addView(dateTextView);

                List<String> sortedTimes = new ArrayList<>(confirmedTimes.get(date));
                Collections.sort(sortedTimes);

                for (String time : sortedTimes) {
                    LinearLayout timeLayout = new LinearLayout(this);
                    timeLayout.setOrientation(LinearLayout.HORIZONTAL);

                    TextView timeTextView = new TextView(this);
                    timeTextView.setText(time);
                    timeTextView.setTextSize(16);
                    timeTextView.setTextColor(getResources().getColor(R.color.black));
                    timeTextView.setPadding(0, 30, 0, 4);
                    timeTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                    ImageView noIcon = new ImageView(this);
                    noIcon.setImageResource(R.drawable.ic_rej);

                    noIcon.setOnClickListener(v -> {
                        removeConfirmedTime(date, time);
                    });

                    timeLayout.addView(timeTextView);
                    timeLayout.addView(noIcon);
                    layoutConfirmTime.addView(timeLayout);
                }
            }
        }
    }

    private void removeConfirmedTime(String date, String time) {
        if (confirmedTimes.containsKey(date)) {
            confirmedTimes.get(date).remove(time);
            if (confirmedTimes.get(date).isEmpty()) {
                confirmedTimes.remove(date);
            }
        }

        updateEmployerSelectedTimeUI();
        updateConfirmedTimeUI();
    }

    private void updateEmployerSelectedTimeUI() {
        layoutEmployerSelectedTime.removeAllViews();

        TextView titleTextView = new TextView(this);
        titleTextView.setText("Available Time");
        titleTextView.setTextSize(20);
        titleTextView.setTextColor(getResources().getColor(R.color.black));
        titleTextView.setTypeface(null, Typeface.BOLD);
        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setPadding(0, 16, 0, 8);
        layoutEmployerSelectedTime.addView(titleTextView);

        for (String date : selectedTimes.keySet()) {
            Map<String, Boolean> timeSlots = new HashMap<>();
            for (String time : selectedTimes.get(date)) {
                timeSlots.put(time, true);
            }
            addDateAndTimeSlotsToLayout(date, timeSlots, layoutEmployerSelectedTime);
        }
    }

    private void sendDeclineRequest() {
        db.collection("interview_request")
                .document(requestID)
                .update("agentState", "Translator Decline",
                        "employerState", "Declined",
                        "translatorState", "Translator Decline",
                        "employeeState", "Translator Decline")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Request Declined Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(TranslatorBookRequestDetailActivity.this, BookRecordActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to decline request", e));
    }

    private void setBookingTime() {
        Map<String, Map<String, Boolean>> formattedConfirmedTimes = new HashMap<>();
        for (String date : confirmedTimes.keySet()) {
            Map<String, Boolean> timeSlots = new HashMap<>();
            for (String time : confirmedTimes.get(date)) {
                timeSlots.put(time, true);
            }
            formattedConfirmedTimes.put(date, timeSlots);
        }

        db.collection("interview_request")
                .document(requestID)
                .update("translatorSelectedTime", formattedConfirmedTimes, "translatorState", "Translator Confirmed")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Request Confirmed Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(TranslatorBookRequestDetailActivity.this, BookRecordActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed Confirmed", e));
    }
}