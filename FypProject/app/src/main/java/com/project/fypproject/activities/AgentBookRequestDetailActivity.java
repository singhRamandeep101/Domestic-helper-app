package com.project.fypproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentBookRequestDetailActivity extends AppCompatActivity {

    private TextView tvRequestID, tvRequestState, tvEmployerName, tvHelperName, tvTranName;
    private LinearLayout layoutEmployerSelectedTime, layoutHelperSelectedTime, layoutTranslatorSelectedTime, layoutSuggestedTime, layoutConfirmedTime;
    private Button btnSend;

    private FirebaseFirestore db;
    private String requestId, currentUserEmail;
    private Map<String, Object> requestData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_book_request_detail);

        // 初始化 Firestore
        db = FirebaseFirestore.getInstance();

        // 初始化視圖
        tvRequestID = findViewById(R.id.tvRequestID);
        tvRequestState = findViewById(R.id.tvRequestState);
        tvEmployerName = findViewById(R.id.tvEmployerName);
        tvHelperName = findViewById(R.id.tvHelperName);
        tvTranName = findViewById(R.id.tvTranName);

        layoutEmployerSelectedTime = findViewById(R.id.layoutEmployerSelectedTime);
        layoutHelperSelectedTime = findViewById(R.id.layoutHelperSelectedTime);
        layoutTranslatorSelectedTime = findViewById(R.id.layoutTranslatorSelectedTime);
        layoutSuggestedTime = findViewById(R.id.layoutSuggestedTime);
        layoutConfirmedTime = findViewById(R.id.layoutConfirmedTime);

        btnSend = findViewById(R.id.btnSend);

        // 獲取上一個 Activity 傳遞的數據
        Intent intent = getIntent();
        requestId = intent.getStringExtra("requestId");
        requestId ="DWOEUbcanW7HFwSO5zL4";
        currentUserEmail = intent.getStringExtra("currentUserEmail");

        // 從 Firestore 加載數據
        loadRequestData();

        // 設置按鈕點擊事件
        setupChatButtons();
        setupSendButton();
    }

    private void loadRequestData() {
        db.collection("interview_request").document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        requestData = documentSnapshot.getData();

                        // 設置基本信息
                        tvRequestID.setText("Request ID: " + requestId);
                        tvRequestState.setText("Request State: " + requestData.get("employeeState"));
                        tvEmployerName.setText("Employer Name: " + requestData.get("employerEmail"));
                        tvHelperName.setText("Helper Name: " + requestData.get("employeeEmail"));

                        // 加載 Translator 名稱
                        loadTranslatorName((String) requestData.get("translatorEmail"));

                        // 根據狀態顯示內容
                        String state = (String) requestData.get("employeeState");
                        handleState(state);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load data!", Toast.LENGTH_SHORT).show());
    }

    private void loadTranslatorName(String translatorEmail) {
        db.collection("users").whereEqualTo("email", translatorEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String firstName = userDoc.getString("firstName");
                        String lastName = userDoc.getString("lastName");
                        tvTranName.setText("Translator Name: " + firstName + " " + lastName);
                    } else {
                        tvTranName.setText("Translator Name: Pending Confirmation");
                    }
                })
                .addOnFailureListener(e -> tvTranName.setText("Translator Name: Error loading"));
    }

    private void handleState(String state) {
        // 隱藏所有布局
        layoutEmployerSelectedTime.setVisibility(View.GONE);
        layoutHelperSelectedTime.setVisibility(View.GONE);
        layoutTranslatorSelectedTime.setVisibility(View.GONE);
        layoutSuggestedTime.setVisibility(View.GONE);
        layoutConfirmedTime.setVisibility(View.GONE);
        btnSend.setVisibility(View.GONE);

        switch (state) {
            case "Action Required":
                showActionRequiredState();
                break;
            case "Waiting All":
                showWaitingAllState();
                break;
            case "Waiting Helper":
                showWaitingHelperState();
                break;
            case "Waiting Translator":
                showWaitingTranslatorState();
                break;
            case "Waiting Confirm":
            case "All Confirm":
                showConfirmedState();
                break;
        }
    }

    private void showActionRequiredState() {
        layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
        layoutHelperSelectedTime.setVisibility(View.VISIBLE);
        layoutTranslatorSelectedTime.setVisibility(View.VISIBLE);
        layoutSuggestedTime.setVisibility(View.VISIBLE);

        // 加載數據並處理
        loadSuggestedTimes();
    }

    private void showWaitingAllState() {
        layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
        layoutHelperSelectedTime.setVisibility(View.VISIBLE);
        layoutTranslatorSelectedTime.setVisibility(View.VISIBLE);

        // 設置內容
        setHelperWaitingMessage();
        setTranslatorWaitingMessage();
    }

    private void showWaitingHelperState() {
        layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
        layoutHelperSelectedTime.setVisibility(View.VISIBLE);
        layoutTranslatorSelectedTime.setVisibility(View.VISIBLE);

        // 設置內容
        setHelperWaitingMessage();
    }

    private void showWaitingTranslatorState() {
        layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
        layoutHelperSelectedTime.setVisibility(View.VISIBLE);
        layoutTranslatorSelectedTime.setVisibility(View.VISIBLE);

        // 設置內容
        setTranslatorWaitingMessage();
    }

    private void showConfirmedState() {
        layoutConfirmedTime.setVisibility(View.VISIBLE);
        btnSend.setVisibility(View.VISIBLE);
    }

    private void setHelperWaitingMessage() {
        TextView helperMessage = new TextView(this);
        helperMessage.setText("Waiting for Helper to select time...");
        layoutHelperSelectedTime.addView(helperMessage);
    }

    private void setTranslatorWaitingMessage() {
        TextView translatorMessage = new TextView(this);
        translatorMessage.setText("Waiting for Translator to select time...");
        layoutTranslatorSelectedTime.addView(translatorMessage);
    }

    private void loadSuggestedTimes() {
        // TODO: 加載 Employer、Helper、Translator 的時間並計算交集
    }

    private void setupChatButtons() {
        ImageButton chatEmployer = findViewById(R.id.chatEmployer);
        ImageButton chatHelper = findViewById(R.id.chatHelper);
        ImageButton chatTranslator = findViewById(R.id.chatTranslator);

        chatEmployer.setOnClickListener(v -> startChatActivity((String) requestData.get("employerEmail")));
        chatHelper.setOnClickListener(v -> startChatActivity((String) requestData.get("employeeEmail")));
        chatTranslator.setOnClickListener(v -> startChatActivity((String) requestData.get("translatorEmail")));
    }

    private void startChatActivity(String recipientEmail) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("recipientEmail", recipientEmail);
        intent.putExtra("currentUserEmail", currentUserEmail);
        startActivity(intent);
    }

    private void setupSendButton() {
        btnSend.setOnClickListener(v -> {
            if (layoutConfirmedTime.getChildCount() > 0) {
                updateRequestStateToWaitingConfirm();
            } else {
                Toast.makeText(this, "No confirmed times available!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRequestStateToWaitingConfirm() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("agentState", "Waiting Confirm");
        updates.put("employeeState", "Action Required");
        updates.put("translatorState", "Action Required");
        updates.put("employerState", "Action Required");

        db.collection("interview_request").document(requestId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "State updated successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update state!", Toast.LENGTH_SHORT).show());
    }
}