package com.project.fypproject.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BookingRequestActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private BookingRequestAdapter adapter;
    private List<HashMap<String, String>> bookingRequests;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView tvNoRecord;
    ImageView img_back;
    EditText etRequestId;
    String keyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_request);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();
        tvNoRecord = findViewById(R.id.tvNoRecord);
        img_back = findViewById(R.id.img_back);
        recyclerView = findViewById(R.id.recyclerView);
        etRequestId = findViewById(R.id.etRequestId);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingRequests = new ArrayList<>();
        adapter = new BookingRequestAdapter(this,bookingRequests);
        recyclerView.setAdapter(adapter);

        etRequestId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               keyword = charSequence.toString().trim();

                if(keyword.isEmpty()){
                    checkUserType(user.getEmail(), "b");
                }else{
                    checkUserType(user.getEmail(), "a");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        checkUserType(user.getEmail(),"b");
    }

    private void checkUserType(final String userEmail,String type) {
        db.collection("users").whereEqualTo("email", userEmail).get()
                .addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String userType = task.getResult().getDocuments().get(0).getString("userType");

                            String field = null;
                            if ("Agent".equals(userType)) {
                                field = "agentEmail";
                            } else if ("Translator".equals(userType)) {
                                field = "translatorEmail";
                            } else if ("DomesticHelper".equals(userType)) {
                                field = "employeeEmail";
                            } else if ("Employer".equals(userType)) {
                                field = "employerEmail";
                            }

                            if (field != null) {
                                if(type.equals("a")){
                                    searchBookingRequests(keyword,userType);
                                }else{
                                    loadBookingRequests(field, userEmail, userType);
                                }
                            } else {
                                Toast.makeText(BookingRequestActivity.this, "Invalid userType", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(BookingRequestActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadBookingRequests(final String field, final String userEmail, final String userType) {
        db.collection("interview_request").whereEqualTo(field, userEmail).get()
                .addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            tvNoRecord.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            bookingRequests.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                final String docId = document.getId();
                                final String employeeEmail = document.getString("employeeEmail");
                                final String employerEmail = document.getString("employerEmail");
                                final String userStatus;

                                if("Agent".equals(userType)){
                                    userStatus = document.getString("agentState");
                                }else if("Translator".equals(userType)){
                                    userStatus= document.getString("translatorState");
                                }else if("DomesticHelper".equals(userType)){
                                    userStatus= document.getString("employeeState");
                                }else{
                                    userStatus= document.getString("employerState");
                                }

                                getName(docId,employeeEmail, employerEmail, userStatus,userType);
                            }
                        } else {
                            bookingRequests.clear();
                            adapter.notifyDataSetChanged();
                            tvNoRecord.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void searchBookingRequests(final String keyword, final String userType) {
        db.collection("interview_request")
                .orderBy("docId")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .get()
                .addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            tvNoRecord.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            bookingRequests.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                final String docId = document.getString("docId");
                                final String employeeEmail = document.getString("employeeEmail");
                                final String employerEmail = document.getString("employerEmail");
                                final String userStatus;

                                if("Agent".equals(userType)){
                                    userStatus = document.getString("agentState");
                                }else if("Translator".equals(userType)){
                                    userStatus= document.getString("translatorState");
                                }else if("DomesticHelper".equals(userType)){
                                    userStatus= document.getString("employeeState");
                                }else{
                                    userStatus= document.getString("employerState");
                                }

                                getName(docId,employeeEmail, employerEmail, userStatus,userType);
                            }
                        } else {
                            bookingRequests.clear();
                            adapter.notifyDataSetChanged();
                            tvNoRecord.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void getName(final String docId,final String employeeEmail, final String employerEmail, final String userStatus,String userType) {
        final HashMap<String, String> requestData = new HashMap<>();

        db.collection("MaidInfo").whereEqualTo("email", employeeEmail).get()
                .addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            requestData.put("helperName", task.getResult().getDocuments().get(0).getString("name"));
                        } else {
                            requestData.put("helperName", "Unknown");
                        }

                        db.collection("users").whereEqualTo("email", employerEmail).get()
                                .addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> userTask) {
                                        if (userTask.isSuccessful() && !userTask.getResult().isEmpty()) {
                                            requestData.put("userName", userTask.getResult().getDocuments().get(0).getString("lastName")+ " "+ userTask.getResult().getDocuments().get(0).getString("firstName"));
                                        } else {
                                            requestData.put("userName", "Unknown");
                                        }

                                        requestData.put("docId", docId);
                                        requestData.put("userType", userType);
                                        requestData.put("userStatus", userStatus);
                                        bookingRequests.add(requestData);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }
}