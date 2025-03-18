package com.project.fypproject.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookRecordActivity extends AppCompatActivity {
    List<Map<String, Object>> records;
    FirebaseFirestore db;
    RecyclerView recyclerView;
    FirebaseAuth auth;
    FirebaseUser user;
    String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_record);

        String employerEmail = getIntent().getStringExtra("employerEmail");

        recyclerView = findViewById(R.id.rv_bookRecord);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        records = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        db.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                          @Override
                                          public void onSuccess(QuerySnapshot maidSnapshots) {
                                              if (!maidSnapshots.isEmpty()) {
                                                  DocumentSnapshot userDocument = maidSnapshots.getDocuments().get(0);
                                                  userType = userDocument.getString("userType");
                                                  upcoming();
                                              }
                                          }
                                          ;
                                      });

        TabLayout tabLayout = findViewById(R.id.tabLayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabText = tab.getText().toString();

                if (tabText.equalsIgnoreCase("UPCOMING")) {
                    upcoming();
                } else if (tabText.equalsIgnoreCase("NEXT MONTH")) {
                    nextMonth();
                } else{
                    allData();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void allData() {
        records.clear();
        db.collection("bookings")
                .whereEqualTo("employeeEmail", "a")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                Map<String, Object> record = document.getData();
                                record.put("documentId", document.getId());

                                String employeeEmail = (String) record.get("employeeEmail");

                                if (employeeEmail != null) {
                                    db.collection("MaidInfo")
                                            .whereEqualTo("email", employeeEmail)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot maidSnapshots) {
                                                    if (!maidSnapshots.isEmpty()) {
                                                        DocumentSnapshot maidDocument = maidSnapshots.getDocuments().get(0);
                                                        String employeeName = (String) maidDocument.get("name");

                                                        record.put("employeeName", employeeName);
                                                    } else {
                                                        record.put("employeeName", "Unknown");
                                                    }
                                                    records.add(record);

                                                    if (records.size() == queryDocumentSnapshots.size()) {
                                                        BookRecordlAdapter bookRecordlAdapter = new BookRecordlAdapter(BookRecordActivity.this, records,userType);
                                                        recyclerView.setAdapter(bookRecordlAdapter);
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("Firestore", "Error fetching MaidInfo", e);
                                                }
                                            });
                                } else {
                                    record.put("employeeName", "Unknown");
                                    records.add(record);
                                }
                            }
                        } else {
                            Log.d("Firestore", "No booking found.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error fetching MaidInfo", e);
                    }
                });
    }

    private void upcoming() {
        records.clear();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        String today = dateFormat.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_YEAR, 7);
        String sevenDaysLater = dateFormat.format(calendar.getTime());

        db.collection("bookings")
                .whereEqualTo("employeeEmail", "a")
                .whereGreaterThanOrEqualTo("date", today)
                .whereLessThanOrEqualTo("date", sevenDaysLater)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                Map<String, Object> record = document.getData();
                                record.put("documentId", document.getId());

                                String employeeEmail = (String) record.get("employeeEmail");

                                if (employeeEmail != null) {
                                    db.collection("MaidInfo")
                                            .whereEqualTo("email", "a")
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot maidSnapshots) {
                                                    if (!maidSnapshots.isEmpty()) {
                                                        DocumentSnapshot maidDocument = maidSnapshots.getDocuments().get(0);
                                                        String employeeName = (String) maidDocument.get("name");

                                                        record.put("employeeName", employeeName);
                                                    } else {
                                                        record.put("employeeName", "Unknown");
                                                    }

                                                    records.add(record);

                                                    if (records.size() == queryDocumentSnapshots.size()) {
                                                        BookRecordlAdapter bookRecordlAdapter = new BookRecordlAdapter(BookRecordActivity.this, records,userType);
                                                        recyclerView.setAdapter(bookRecordlAdapter);
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("Firestore", "Error fetching MaidInfo", e);
                                                }
                                            });
                                } else {
                                    record.put("employeeName", "Unknown");
                                    records.add(record);
                                }
                            }
                        } else {
                            Log.d("Firestore", "No upcoming bookings found.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error fetching bookings", e);
                    }
                });
    }
    private void nextMonth() {
        records.clear();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        String today = dateFormat.format(calendar.getTime());

        calendar.add(Calendar.MONTH, 1);
        String oneMonthLater = dateFormat.format(calendar.getTime());


        db.collection("bookings")
                .whereEqualTo("employeeEmail", "a")
                .whereGreaterThanOrEqualTo("date", today)
                .whereLessThanOrEqualTo("date", oneMonthLater)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                Map<String, Object> record = document.getData();
                                record.put("documentId", document.getId());

                                db.collection("MaidInfo")
                                        .whereEqualTo("email", "a")
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot maidSnapshots) {
                                                if (!maidSnapshots.isEmpty()) {
                                                    DocumentSnapshot maidDocument = maidSnapshots.getDocuments().get(0);
                                                    String employeeName = (String) maidDocument.get("name");

                                                    record.put("employeeName", employeeName);
                                                } else {
                                                    record.put("employeeName", "Unknown");
                                                }

                                                records.add(record);

                                                if (records.size() == queryDocumentSnapshots.size()) {
                                                    BookRecordlAdapter bookRecordlAdapter = new BookRecordlAdapter(BookRecordActivity.this, records,userType);
                                                    recyclerView.setAdapter(bookRecordlAdapter);
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("Firestore", "Error fetching MaidInfo", e);
                                            }
                                        });
                            }
                        } else {
                            Log.d("Firestore", "No bookings found for the next month.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error fetching bookings", e);
                    }
                });
    }
}
