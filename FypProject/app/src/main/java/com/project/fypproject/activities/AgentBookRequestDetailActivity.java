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

public class AgentBookRequestDetailActivity extends AppCompatActivity {

    private TextView tvRequestID, tvRequestState, tvEmployerName, tvHelperName, tvTranName, tvNoMatchTime;
    private ImageButton chatEmployer, chatHelper, chatTranslator;

    private String requestID, requestState, employerName, helperName, translatorEmail, employerEmail, employeeEmail;
    private String currentUserEmail;
    private LinearLayout layoutEmployerSelectedTime, layoutHelperSelectedTime, layoutTranslatorSelectedTime, layoutMatchingTime, layoutConfirmedTime;

    private FirebaseFirestore db;
    private Map<String, List<String>> matchingTimes = new HashMap<>();
    private Map<String, List<String>> confirmedTimes = new HashMap<>();

    Button btnSendReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_book_request_detail);

        // 初始化 Firestore
        db = FirebaseFirestore.getInstance();

        // 綁定視圖
        tvRequestID = findViewById(R.id.tvRequestID);
        tvRequestState = findViewById(R.id.tvRequestState);
        tvEmployerName = findViewById(R.id.tvEmployerName);
        tvHelperName = findViewById(R.id.tvHelperName);
        tvTranName = findViewById(R.id.tvTranName);
        chatEmployer = findViewById(R.id.chatEmployer);
        chatHelper = findViewById(R.id.chatHelper);
        chatTranslator = findViewById(R.id.chatTranslator);
        layoutEmployerSelectedTime = findViewById(R.id.layoutEmployerSelectedTime);
        layoutHelperSelectedTime = findViewById(R.id.layoutHelperSelectedTime);
        layoutTranslatorSelectedTime = findViewById(R.id.layoutTranslatorSelectedTime);
        layoutMatchingTime = findViewById(R.id.layoutMatchingTime);
        layoutConfirmedTime = findViewById(R.id.layoutConfirmedTime);
        tvNoMatchTime = findViewById(R.id.tvNoMatchTime);
        btnSendReject = findViewById(R.id.btnSendReject);

        // 從上一個 Activity 獲取數據
        Intent intent = getIntent();
//        requestID = intent.getStringExtra("requestID");
        requestID = "DWOEUbcanW7HFwSO5zL4";
//        requestState = intent.getStringExtra("requestState");
        requestState = "Action Required";
        employerName = intent.getStringExtra("employerName");
        helperName = intent.getStringExtra("helperName");

        // 設置數據到 TextViews
        tvRequestID.setText("Request ID: " + requestID);
        tvRequestState.setText("Request State: " + requestState);
        tvEmployerName.setText("Employer Name: " + employerName);
        tvHelperName.setText("DomesticHelper Name: " + helperName);

        // 獲取翻譯員資訊
        getInfo();

        // 為聊天按鈕設置點擊事件
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

        if ("Awaiting Both".equals(requestState)) {
            layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
            layoutHelperSelectedTime.setVisibility(View.GONE);
            layoutTranslatorSelectedTime.setVisibility(View.GONE);
            layoutMatchingTime.setVisibility(View.GONE);
            layoutConfirmedTime.setVisibility(View.GONE);

            getSelectedTime(requestID, "employerSelectedTime", layoutEmployerSelectedTime);

        } else if ("Awaiting Translator Confirmation".equals(requestState)) {
            layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
            layoutHelperSelectedTime.setVisibility(View.VISIBLE);
            layoutTranslatorSelectedTime.setVisibility(View.GONE);
            layoutMatchingTime.setVisibility(View.GONE);
            layoutConfirmedTime.setVisibility(View.GONE);
            getSelectedTime(requestID, "employerSelectedTime", layoutEmployerSelectedTime);
            getSelectedTime(requestID, "helperSelectedTime", layoutHelperSelectedTime);

        } else if ("Awaiting DomesticHelper Confirmation".equals(requestState)) {
            layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
            layoutTranslatorSelectedTime.setVisibility(View.VISIBLE);
            layoutHelperSelectedTime.setVisibility(View.GONE);
            layoutMatchingTime.setVisibility(View.GONE);
            layoutConfirmedTime.setVisibility(View.GONE);
            getSelectedTime(requestID, "employerSelectedTime", layoutEmployerSelectedTime);
            getSelectedTime(requestID, "translatorSelectedTime", layoutTranslatorSelectedTime);

        } else if ("Action Required".equals(requestState)) {
            layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
            layoutHelperSelectedTime.setVisibility(View.VISIBLE);
            layoutTranslatorSelectedTime.setVisibility(View.VISIBLE);
            layoutMatchingTime.setVisibility(View.VISIBLE);
            layoutConfirmedTime.setVisibility(View.GONE);
            getSelectedTime(requestID, "employerSelectedTime", layoutEmployerSelectedTime);
            getSelectedTime(requestID, "helperSelectedTime", layoutHelperSelectedTime);
            getSelectedTime(requestID, "translatorSelectedTime", layoutTranslatorSelectedTime);

            fetchAndMatchTimes();
        }
    }

    private void getInfo() {
        // 使用 requestID 從 interview_request 集合中獲取 translatorEmail
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

                            if (translatorEmail != null && !translatorEmail.isEmpty()) {
                                // 使用 translatorEmail 從 users 集合中獲取翻譯員的名字
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
                        } else {
                            Log.e("Translator", "Translator Not Found2");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Translator", "Error fetching translator info");
                    }
                });
    }

    private void openChat(String email) {
        // Step 3: Query users collection for the agent's details using agentEmail
        db.collection("users")
                .whereEqualTo("email", email) // Search for the agent by email
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            QuerySnapshot Docs = task.getResult();

                            if (!Docs.isEmpty()) {
                                // Get the agent document
                                DocumentSnapshot Doc = Docs.getDocuments().get(0);

                                // Retrieve agent's details
                                String firstName = Doc.getString("firstName");
                                String lastName = Doc.getString("lastName");
                                String userType = Doc.getString("userType");

                                if (firstName != null && lastName != null) {
                                    // Step 4: Create and set ChatModel
                                    ChatModel chatModel = new ChatModel();
                                    chatModel.setEmail(email);
                                    chatModel.setFirstName(firstName);
                                    chatModel.setLastName(lastName);
                                    chatModel.setUserType(userType);

                                    Intent intent = new Intent(AgentBookRequestDetailActivity.this, ChatActivity.class);
                                    ChatUtil.passUserIntent(intent, chatModel); // Pass ChatModel via ChatUtil
                                    startActivity(intent); // Start ChatActivity

                                    // Log the information for verification
                                    Log.d("Firestore", "ChatModel set with agent details: " +
                                            "FirstName: " + firstName + ", LastName: " + lastName +
                                            ", AgentEmail: " + email);

                                    // Optionally, you can save ChatModel to Firestore or proceed to the next action
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
        // 添加日期 TextView
        TextView dateTextView = new TextView(this);
        dateTextView.setText(date);
        dateTextView.setTextSize(18);
        dateTextView.setTextColor(getResources().getColor(R.color.black));
        dateTextView.setPadding(0, 16, 0, 8);
        dateTextView.setTypeface(null, Typeface.BOLD);
        parentLayout.addView(dateTextView);

        // 將時間段的鍵進行排序
        List<String> sortedTimeSlots = new ArrayList<>(timeSlots.keySet());
        Collections.sort(sortedTimeSlots); // 按字母排序（時間格式如 "10:00 - 11:00" 會按順序排列）

        // 添加排序後的時間段
        for (String timeSlot : sortedTimeSlots) {
            if (timeSlots.get(timeSlot)) { // 只顯示值為 true 的時間段
                TextView timeSlotTextView = new TextView(this);
                timeSlotTextView.setText(timeSlot);
                timeSlotTextView.setTextSize(16);
                timeSlotTextView.setTextColor(getResources().getColor(R.color.black));
                timeSlotTextView.setPadding(0, 4, 0, 4);
                parentLayout.addView(timeSlotTextView);
            }
        }
    }

    private void fetchAndMatchTimes() {
        db.collection("interview_request")
                .whereEqualTo("docId", requestID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        Map<String, Object> employerTimes = (Map<String, Object>) task.getResult().getDocuments().get(0).get("employerSelectedTime");
                        Map<String, Object> helperTimes = (Map<String, Object>) task.getResult().getDocuments().get(0).get("helperSelectedTime");
                        Map<String, Object> translatorTimes = (Map<String, Object>) task.getResult().getDocuments().get(0).get("translatorSelectedTime");

                        matchingTimes = findMatchingTimes(employerTimes, helperTimes, translatorTimes);

                        if (matchingTimes.isEmpty()) {
                            tvNoMatchTime.setVisibility(View.VISIBLE);
                            btnSendReject.setVisibility(View.VISIBLE);
                            layoutMatchingTime.setVisibility(View.GONE);
                            Log.d("NoMatchTime", "No matching times found");
                        } else {
                            tvNoMatchTime.setVisibility(View.GONE);
                            btnSendReject.setVisibility(View.GONE);
                            layoutMatchingTime.setVisibility(View.VISIBLE);
                            Log.d("MatchTime", "Matching times found: " + matchingTimes);
                        }

                        updateMatchingTimeUI();
                    } else {
                        Log.e("Firestore", "Failed to fetch selected times");
                        tvNoMatchTime.setVisibility(View.VISIBLE);
                        btnSendReject.setVisibility(View.VISIBLE);
                        layoutMatchingTime.setVisibility(View.GONE);
                    }
                });
    }

    private Map<String, List<String>> findMatchingTimes(Map<String, Object> employerTimes, Map<String, Object> helperTimes, Map<String, Object> translatorTimes) {
        Map<String, List<String>> matches = new HashMap<>();

        if (employerTimes != null && helperTimes != null && translatorTimes != null) {
            for (String date : employerTimes.keySet()) {
                Map<String, Boolean> employerSlots = (Map<String, Boolean>) employerTimes.get(date);
                Map<String, Boolean> helperSlots = (Map<String, Boolean>) helperTimes.get(date);
                Map<String, Boolean> translatorSlots = (Map<String, Boolean>) translatorTimes.get(date);

                if (employerSlots != null && helperSlots != null && translatorSlots != null) {
                    List<String> matchingSlots = new ArrayList<>();
                    for (String time : employerSlots.keySet()) {
                        if (employerSlots.get(time) && helperSlots.getOrDefault(time, false) && translatorSlots.getOrDefault(time, false)) {
                            matchingSlots.add(time);
                        }
                    }
                    if (!matchingSlots.isEmpty()) {
                        matches.put(date, matchingSlots);
                    }
                }
            }
        }

        return matches;
    }

    private boolean isTimeConfirmed = false; // 用于记录是否已经确认了一个时间

    private void updateMatchingTimeUI() {
        // 清空布局，但保留标题
        layoutMatchingTime.removeAllViews();

        // 添加标题
        TextView titleTextView = new TextView(this);
        titleTextView.setText("Matching Time");
        titleTextView.setTextSize(20);
        titleTextView.setTextColor(getResources().getColor(R.color.black));
        titleTextView.setTypeface(null, Typeface.BOLD);
        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setPadding(0, 16, 0, 8);
        layoutMatchingTime.addView(titleTextView);

        if (matchingTimes.isEmpty()) {
            tvNoMatchTime.setVisibility(View.VISIBLE);
            btnSendReject.setVisibility(View.VISIBLE);
            layoutMatchingTime.setVisibility(View.VISIBLE);
        } else {
            tvNoMatchTime.setVisibility(View.GONE);
            btnSendReject.setVisibility(View.GONE);
            layoutMatchingTime.setVisibility(View.VISIBLE);

            for (String date : matchingTimes.keySet()) {
                // 添加日期 TextView
                TextView dateTextView = new TextView(this);
                dateTextView.setText(date);
                dateTextView.setTextSize(18);
                dateTextView.setTextColor(getResources().getColor(R.color.black));
                dateTextView.setPadding(0, 16, 0, 8);
                dateTextView.setTypeface(null, Typeface.BOLD);
                layoutMatchingTime.addView(dateTextView);

                List<String> sortedTimes = new ArrayList<>(matchingTimes.get(date));
                Collections.sort(sortedTimes);

                // 添加时间段
                for (String time : sortedTimes) {
                    LinearLayout timeLayout = new LinearLayout(this);
                    timeLayout.setOrientation(LinearLayout.HORIZONTAL);

                    TextView timeTextView = new TextView(this);
                    timeTextView.setText(time);
                    timeTextView.setTextSize(16);
                    timeTextView.setTextColor(getResources().getColor(R.color.black));
                    timeTextView.setPadding(0, 4, 0, 4);
                    timeTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                    ImageView yesIcon = new ImageView(this);
                    yesIcon.setImageResource(R.drawable.icon_confirm);

                    // 如果已经有时间被确认，隐藏所有 "Yes" 按钮
                    if (isTimeConfirmed) {
                        yesIcon.setVisibility(View.GONE);
                    }

                    // 检查时间段是否已被确认，如果是，则隐藏 Yes 按钮
                    if (confirmedTimes.containsKey(date) && confirmedTimes.get(date).contains(time)) {
                        yesIcon.setVisibility(View.GONE);
                    }

                    yesIcon.setOnClickListener(v -> {
                        addConfirmedTime(date, time);
                        isTimeConfirmed = true; // 标记为已确认
                        updateMatchingTimeUI(); // 隐藏所有其他 "Yes" 按钮
                    });

                    timeLayout.addView(timeTextView);
                    timeLayout.addView(yesIcon);
                    layoutMatchingTime.addView(timeLayout);
                }
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

        // 更新 UI
        isTimeConfirmed = true; // 标记为已确认
        updateMatchingTimeUI(); // 更新 Matching Time UI，隐藏所有其他 Yes 按钮
        updateConfirmedTimeUI(); // 更新 Confirmed Time UI，添加新时间段
    }

    private void updateConfirmedTimeUI() {
        // 清空布局，但保留标题
        layoutConfirmedTime.removeAllViews();

        // 添加标题
        TextView titleTextView = new TextView(this);
        titleTextView.setText("Confirmed Time");
        titleTextView.setTextSize(20);
        titleTextView.setTextColor(getResources().getColor(R.color.black));
        titleTextView.setTypeface(null, Typeface.BOLD);
        titleTextView.setPadding(0, 16, 0, 8);
        layoutConfirmedTime.addView(titleTextView);

        if (confirmedTimes.isEmpty()) {
            layoutConfirmedTime.setVisibility(View.GONE);
        } else {
            layoutConfirmedTime.setVisibility(View.VISIBLE);

            for (String date : confirmedTimes.keySet()) {
                // 添加日期 TextView
                TextView dateTextView = new TextView(this);
                dateTextView.setText(date);
                dateTextView.setTextSize(18);
                dateTextView.setTextColor(getResources().getColor(R.color.black));
                dateTextView.setPadding(0, 16, 0, 8);
                titleTextView.setGravity(Gravity.CENTER);
                dateTextView.setTypeface(null, Typeface.BOLD);
                layoutConfirmedTime.addView(dateTextView);
                List<String> sortedTimes = new ArrayList<>(confirmedTimes.get(date));
                Collections.sort(sortedTimes);

                // 添加时间段
                for (String time : sortedTimes) {
                    LinearLayout timeLayout = new LinearLayout(this);
                    timeLayout.setOrientation(LinearLayout.HORIZONTAL);

                    TextView timeTextView = new TextView(this);
                    timeTextView.setText(time);
                    timeTextView.setTextSize(16);
                    timeTextView.setTextColor(getResources().getColor(R.color.black));
                    timeTextView.setPadding(0, 4, 0, 4);
                    timeTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                    ImageView noIcon = new ImageView(this);
                    noIcon.setImageResource(R.drawable.ic_rej);

                    noIcon.setOnClickListener(v -> {
                        removeConfirmedTime(date, time); // 从 Confirmed Time 中移除时间段
                        timeLayout.setVisibility(View.GONE);
                    });

                    timeLayout.addView(timeTextView);
                    timeLayout.addView(noIcon);
                    layoutConfirmedTime.addView(timeLayout);
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

        // 如果移除了最后一个确认时间，重置 isTimeConfirmed
        if (confirmedTimes.isEmpty()) {
            isTimeConfirmed = false;
        }

        // 更新 UI
        updateConfirmedTimeUI(); // 移除 Confirmed Time 的时间段
        updateMatchingTimeUI(); // 重新显示 Matching Time 的 Yes 按钮
    }
}