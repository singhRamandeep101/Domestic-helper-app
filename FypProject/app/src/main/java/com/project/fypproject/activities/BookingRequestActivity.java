package com.project.fypproject.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.firebase.firestore.Query;
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
        adapter = new BookingRequestAdapter(this, bookingRequests);
        recyclerView.setAdapter(adapter);

        etRequestId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                keyword = charSequence.toString().trim();
                if (keyword.isEmpty()) {
                    checkUserType(user.getEmail(), "b");
                } else {
                    checkUserType(user.getEmail(), "a");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        img_back.setOnClickListener(view -> finish());

        checkUserType(user.getEmail(), "b");
    }

    private void checkUserType(final String userEmail, String type) {
        db.collection("users").whereEqualTo("email", userEmail).get()
                .addOnCompleteListener(task -> {
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
                            if (type.equals("a")) {
                                searchBookingRequests(keyword, userType);
                            } else {
                                loadBookingRequests(field, userEmail, userType);
                            }
                        } else {
                            Toast.makeText(this, "Invalid userType", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadBookingRequests(final String field, final String userEmail, final String userType) {
        db.collection("interview_request")
                .whereEqualTo(field, userEmail)
                .orderBy("postTime", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        if (!value.isEmpty()) {
                            tvNoRecord.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                            final List<HashMap<String, String>> tempBookingRequests = new ArrayList<>();

                            for (QueryDocumentSnapshot document : value) {
                                getName(
                                        document.getString("docId"),
                                        document.getString("employeeEmail"),
                                        document.getString("employerEmail"),
                                        userType,
                                        document.getString("agentState"),
                                        document.getString("translatorState"),
                                        document.getString("employeeState"),
                                        document.getString("employerState"),
                                        tempBookingRequests,
                                        value.size()
                                );
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
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Search failed.", error);
                        return;
                    }

                    if (value != null) {
                        if (!value.isEmpty()) {
                            tvNoRecord.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                            final List<HashMap<String, String>> tempBookingRequests = new ArrayList<>();

                            for (QueryDocumentSnapshot document : value) {
                                getName(
                                        document.getString("docId"),
                                        document.getString("employeeEmail"),
                                        document.getString("employerEmail"),
                                        userType,
                                        document.getString("agentState"),
                                        document.getString("translatorState"),
                                        document.getString("employeeState"),
                                        document.getString("employerState"),
                                        tempBookingRequests,
                                        value.size()
                                );
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

    private void getName(final String docId, final String employeeEmail, final String employerEmail, final String userType,
                         final String agentState, final String translatorState, final String employeeState, final String employerState,
                         final List<HashMap<String, String>> tempList, final int totalCount) {

        final HashMap<String, String> requestData = new HashMap<>();
        if (docId == null || docId.isEmpty()) return;

        db.collection("MaidInfo").whereEqualTo("email", employeeEmail).get()
                .addOnCompleteListener(task -> {
                    requestData.put("helperName", task.isSuccessful() && !task.getResult().isEmpty()
                            ? task.getResult().getDocuments().get(0).getString("name") : "Unknown");

                    db.collection("users").whereEqualTo("email", employerEmail).get()
                            .addOnCompleteListener(userTask -> {
                                requestData.put("userName", userTask.isSuccessful() && !userTask.getResult().isEmpty()
                                        ? userTask.getResult().getDocuments().get(0).getString("lastName") + " " + userTask.getResult().getDocuments().get(0).getString("firstName") : "Unknown");

                                requestData.put("docId", docId);
                                requestData.put("userType", userType);
                                requestData.put("agentState", agentState);
                                requestData.put("translatorState", translatorState);
                                requestData.put("employeeState", employeeState);
                                requestData.put("employerState", employerState);

                                tempList.add(requestData);

                                if (tempList.size() == totalCount) {
                                    bookingRequests.clear();
                                    bookingRequests.addAll(tempList);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                });
    }
}