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

public class AgentBookRequestDetailActivity extends AppCompatActivity {

    private TextView tvRequestID, tvRequestState, tvEmployerName, tvHelperName, tvTranName, tvNoMatchTime,tvAgentName;
    private ImageButton chatEmployer, chatHelper, chatTranslator;

    private String requestID, requestState, employerName, helperName, translatorEmail, employerEmail, employeeEmail;
    private String currentUserEmail;
    LinearLayout layoutEmployerSelectedTime, layoutHelperSelectedTime, layoutTranslatorSelectedTime, layoutMatchingTime, layoutConfirmedTime,layoutConfirmTime;

    private FirebaseFirestore db;
    private Map<String, List<String>> matchingTimes = new HashMap<>();
    private Map<String, List<String>> confirmedTimes = new HashMap<>();

    Button btnSendDecline,btnBook,btnCheckBook;

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_book_request_detail);

        // 初始化 Firestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

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
        layoutConfirmTime = findViewById(R.id.layoutConfirmTime);
        layoutConfirmedTime = findViewById(R.id.layoutConfirmedTime);
        tvNoMatchTime = findViewById(R.id.tvNoMatchTime);
        btnSendDecline = findViewById(R.id.btnSendDecline);
        btnBook = findViewById(R.id.btnBook);
        btnCheckBook = findViewById(R.id.btnCheckBook);
        tvAgentName = findViewById(R.id.tvAgentName);

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

        btnCheckBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AgentBookRequestDetailActivity.this, BookRecordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSendDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 更新 Firestore 中的状态
                db.collection("interview_request")
                        .document(requestID) // 使用当前的 requestID 定位文档
                        .update(
                                "agentState", "Declined",
                                "employerState", "Declined",
                                "translatorState", "Declined",
                                "employeeState", "Declined"
                        )
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(AgentBookRequestDetailActivity.this, "The Decline Request Has Been Sent Successfully.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AgentBookRequestDetailActivity.this, BookingRequestActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AgentBookRequestDetailActivity.this, "Failed to Declined request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("Firestore", "Error updating states", e);
                            }
                        });
            }
        });

        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 确保 confirmedTimes 只有一个日期
                if (confirmedTimes.size() == 1) {
                    // 获取唯一的日期和时间段
                    String confirmedDate = confirmedTimes.keySet().iterator().next(); // 获取日期
                    List<String> timeSlots = confirmedTimes.get(confirmedDate); // 获取时间段列表

                    if (timeSlots != null && timeSlots.size() == 1) {
                        String confirmedTimeSlot = timeSlots.get(0); // 获取唯一的时间段

                        // 构造 Firestore 的 confirmedTime 数据
                        Map<String, Object> confirmedTimeMap = new HashMap<>();
                        Map<String, Boolean> timeSlotMap = new HashMap<>();
                        timeSlotMap.put(confirmedTimeSlot, true);
                        confirmedTimeMap.put(confirmedDate, timeSlotMap);

                        // 更新 interview_request 集合中的状态和 confirmedTime
                        db.collection("interview_request")
                                .document(requestID) // 使用当前的 requestID 定位文档
                                .update(
                                        "agentState", "Final Time Confirmed",
                                        "employerState", "Final Time Confirmed",
                                        "translatorState", "Final Time Confirmed",
                                        "employeeState", "Final Time Confirmed",
                                        "confirmedTime", confirmedTimeMap // 添加 confirmedTime 数据
                                )
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // 构造 booking 集合的数据
                                        Map<String, Object> bookingData = new HashMap<>();
                                        bookingData.put("timeSlot", confirmedTimeSlot);
                                        bookingData.put("agentEmail", user.getEmail()); // 当前用户的 email
                                        bookingData.put("employeeEmail", employeeEmail);
                                        bookingData.put("employerEmail", employerEmail);
                                        bookingData.put("translatorEmail", translatorEmail);
                                        bookingData.put("date", confirmedDate);
                                        bookingData.put("state", "Waiting for interview");

                                        // 添加到 booking 集合
                                        db.collection("booking")
                                                .add(bookingData)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Log.d("Firestore", "Booking added with ID: " + documentReference.getId());
                                                        // 提示成功并跳转到 BookingRequestActivity
                                                        Toast.makeText(AgentBookRequestDetailActivity.this, "The Confirmation Request Has Been Sent Successfully.", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(AgentBookRequestDetailActivity.this, BookingRequestActivity.class);
                                                        startActivity(intent); // Start BookingRequestActivity
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e("Firestore", "Error adding booking: ", e);
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AgentBookRequestDetailActivity.this, "Failed to confirm request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("Firestore", "Error updating states", e);
                                    }
                                });
                    } else {
                        Toast.makeText(AgentBookRequestDetailActivity.this, "Error: Confirmed time is invalid or missing.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AgentBookRequestDetailActivity.this, "Error: ConfirmedTimes must contain exactly one entry.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if ("Awaiting Both".equals(requestState)) {
            layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
            layoutHelperSelectedTime.setVisibility(View.GONE);
            layoutTranslatorSelectedTime.setVisibility(View.GONE);
            layoutMatchingTime.setVisibility(View.GONE);
            layoutConfirmTime.setVisibility(View.GONE);

            getSelectedTime(requestID, "employerSelectedTime", layoutEmployerSelectedTime);

        } else if ("Awaiting Translator".equals(requestState)) {
            layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
            layoutHelperSelectedTime.setVisibility(View.VISIBLE);
            layoutTranslatorSelectedTime.setVisibility(View.GONE);
            layoutMatchingTime.setVisibility(View.GONE);
            layoutConfirmTime.setVisibility(View.GONE);
            getSelectedTime(requestID, "employerSelectedTime", layoutEmployerSelectedTime);
            getSelectedTime(requestID, "helperSelectedTime", layoutHelperSelectedTime);

        } else if ("Awaiting DomesticHelper".equals(requestState)) {
            layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
            layoutTranslatorSelectedTime.setVisibility(View.VISIBLE);
            layoutHelperSelectedTime.setVisibility(View.GONE);
            layoutMatchingTime.setVisibility(View.GONE);
            layoutConfirmTime.setVisibility(View.GONE);
            getSelectedTime(requestID, "employerSelectedTime", layoutEmployerSelectedTime);
            getSelectedTime(requestID, "translatorSelectedTime", layoutTranslatorSelectedTime);

        } else if ("Action Required".equals(requestState)) {
            layoutEmployerSelectedTime.setVisibility(View.VISIBLE);
            layoutHelperSelectedTime.setVisibility(View.VISIBLE);
            layoutTranslatorSelectedTime.setVisibility(View.VISIBLE);
            layoutMatchingTime.setVisibility(View.VISIBLE);
            layoutConfirmTime.setVisibility(View.GONE);
            getSelectedTime(requestID, "employerSelectedTime", layoutEmployerSelectedTime);
            getSelectedTime(requestID, "helperSelectedTime", layoutHelperSelectedTime);
            getSelectedTime(requestID, "translatorSelectedTime", layoutTranslatorSelectedTime);

            fetchAndMatchTimes();
        }else if ("Declined".equals(requestState)) {
            layoutEmployerSelectedTime.setVisibility(View.GONE);
            layoutTranslatorSelectedTime.setVisibility(View.GONE);
            layoutHelperSelectedTime.setVisibility(View.GONE);
            layoutMatchingTime.setVisibility(View.GONE);
            layoutConfirmTime.setVisibility(View.GONE);
        }else{
            layoutEmployerSelectedTime.setVisibility(View.GONE);
            layoutTranslatorSelectedTime.setVisibility(View.GONE);
            layoutHelperSelectedTime.setVisibility(View.GONE);
            layoutMatchingTime.setVisibility(View.GONE);
            layoutConfirmTime.setVisibility(View.GONE);
            layoutConfirmedTime.setVisibility(View.VISIBLE);
            btnCheckBook.setVisibility(View.VISIBLE);
            getSelectedTime(requestID, "confirmedTime", layoutConfirmedTime);
        }
    }

    private void getInfo() {
        // 使用 requestID 從 interview_request 集合中獲取相關信息
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
                            currentUserEmail = querySnapshot.getDocuments().get(0).getString("agentEmail"); // 获取 agentEmail

                            // 获取翻译员的名字
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

                            // 获取代理人的名字
                            if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
                                db.collection("users")
                                        .whereEqualTo("email", currentUserEmail)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> agentTask) {
                                                if (agentTask.isSuccessful() && !agentTask.getResult().isEmpty()) {
                                                    String firstName = agentTask.getResult().getDocuments().get(0).getString("firstName");
                                                    String lastName = agentTask.getResult().getDocuments().get(0).getString("lastName");
                                                    // 在 UI 中显示代理人的名字
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
                                    finish();

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
                        updateMatchingTimeUI();
                    } else {
                        Log.e("Firestore", "Failed to fetch selected times");
                        tvNoMatchTime.setVisibility(View.VISIBLE);
                        btnSendDecline.setVisibility(View.VISIBLE);
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
        layoutMatchingTime.addView(tvNoMatchTime);

        if (matchingTimes.isEmpty()) {
            tvNoMatchTime.setVisibility(View.VISIBLE);
            btnSendDecline.setVisibility(View.VISIBLE);
            layoutMatchingTime.setVisibility(View.VISIBLE);
        } else {
            tvNoMatchTime.setVisibility(View.GONE);
            btnSendDecline.setVisibility(View.GONE);
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
        layoutConfirmTime.removeAllViews();

        // 添加标题
        TextView titleTextView = new TextView(this);
        titleTextView.setText("Confirmed Time");
        titleTextView.setTextSize(20);
        titleTextView.setTextColor(getResources().getColor(R.color.black));
        titleTextView.setTypeface(null, Typeface.BOLD);
        titleTextView.setPadding(0, 16, 0, 8);
        layoutConfirmTime.addView(titleTextView);

        if (confirmedTimes.isEmpty()) {
            layoutConfirmTime.setVisibility(View.GONE);
            btnBook.setVisibility(View.GONE);
        } else {
            layoutConfirmTime.setVisibility(View.VISIBLE);
            btnBook.setVisibility(View.VISIBLE);

            for (String date : confirmedTimes.keySet()) {
                // 添加日期 TextView
                TextView dateTextView = new TextView(this);
                dateTextView.setText(date);
                dateTextView.setTextSize(18);
                dateTextView.setTextColor(getResources().getColor(R.color.black));
                dateTextView.setPadding(0, 16, 0, 8);
                titleTextView.setGravity(Gravity.CENTER);
                dateTextView.setTypeface(null, Typeface.BOLD);
                layoutConfirmTime.addView(dateTextView);
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

        // 如果移除了最后一个确认时间，重置 isTimeConfirmed
        if (confirmedTimes.isEmpty()) {
            isTimeConfirmed = false;
        }

        // 更新 UI
        updateConfirmedTimeUI(); // 移除 Confirmed Time 的时间段
        updateMatchingTimeUI(); // 重新显示 Matching Time 的 Yes 按钮
    }
}