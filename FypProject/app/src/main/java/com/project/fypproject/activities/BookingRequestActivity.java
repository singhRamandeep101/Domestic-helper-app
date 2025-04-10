package com.project.fypproject.activities;

import android.os.Bundle;
import android.view.View;
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookingRequests = new ArrayList<>();
        adapter = new BookingRequestAdapter(bookingRequests);
        recyclerView.setAdapter(adapter);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        checkUserType(user.getEmail());
    }

    private void checkUserType(final String userEmail) {
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
                                loadBookingRequests(field, userEmail, userType);
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
                                final String employeeEmail = document.getString("employeeEmail");
                                final String employerEmail = document.getString("employerEmail");
                                final String userStatus;

                                if("Employer".equals(userType)){
                                    userStatus = document.getString("employerState");
                                }else{
                                    userStatus= document.getString("internalState");
                                }

                                getName(employeeEmail, employerEmail, userStatus);
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

    private void getName(final String employeeEmail, final String employerEmail, final String userStatus) {
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

                                        requestData.put("userStatus", userStatus);
                                        bookingRequests.add(requestData);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }
}