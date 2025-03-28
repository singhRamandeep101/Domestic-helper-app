package com.project.fypproject.activities.employer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;
import com.project.fypproject.activities.BookRecordActivity;
import com.project.fypproject.activities.BookRecordlAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ApointmentFragment extends Fragment {

    List<Map<String, Object>> records;
    FirebaseFirestore db;
    RecyclerView recyclerView;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView tvNoData;
    String userType;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        view = inflater.inflate(R.layout.fragment_emplyer_home, container, false);
//
//        recyclerView = view.findViewById(R.id.rv_bookRecord);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//
//        TextView tvNoData = view.findViewById(R.id.tv_no_data);
//        recyclerView = view.findViewById(R.id.rv_bookRecord);
//
//        db = FirebaseFirestore.getInstance();
//        records = new ArrayList<>();
//
//        auth = FirebaseAuth.getInstance();
//        user = auth.getCurrentUser();
//
//        db.collection("users")
//                .whereEqualTo("email", user.getEmail())
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot maidSnapshots) {
//                        if (!maidSnapshots.isEmpty()) {
//                            DocumentSnapshot userDocument = maidSnapshots.getDocuments().get(0);
//                            userType = userDocument.getString("userType");
//                            upcoming();
//                        }
//                    }
//                    ;
//                });
//
//        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
//
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                String tabText = tab.getText().toString();
//
//                if (tabText.equalsIgnoreCase("UPCOMING")) {
//                    upcoming();
//                } else if (tabText.equalsIgnoreCase("NEXT MONTH")) {
//                    nextMonth();
//                } else{
//                    allData();
//                }
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {}
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {}
//        });

        return inflater.inflate(R.layout.fragment_apointment, container, false);
    }

    private void allData() {
        records.clear();
        String emailField = "";

        if ("Employer".equals(userType)) {
            emailField = "employerEmail";
        } else if ("Agent".equals(userType)) {
            emailField = "agentEmail";
        } else if ("Employee".equals(userType)){
            emailField = "employeeEmail";
        }

        db.collection("bookings")
                .whereEqualTo(emailField, user.getEmail())
                .orderBy("date", Query.Direction.DESCENDING)
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
                                                        updateUI();
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
                            updateUI();
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-dd", Locale.getDefault());
        String today = dateFormat.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_YEAR, 7);
        String sevenDaysLater = dateFormat.format(calendar.getTime());

        String emailField = "";

        if ("Employer".equals(userType)) {
            emailField = "employerEmail";
        } else if ("Agent".equals(userType)) {
            emailField = "agentEmail";
        } else if ("Employee".equals(userType)){
            emailField = "employeeEmail";
        }

        db.collection("bookings")
                .whereEqualTo(emailField, user.getEmail())
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
                                                        updateUI();
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
                            updateUI();
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-dd", Locale.getDefault());
        String today = dateFormat.format(calendar.getTime());

        calendar.add(Calendar.MONTH, 1);
        String oneMonthLater = dateFormat.format(calendar.getTime());


        String emailField = "";

        if ("Employer".equals(userType)) {
            emailField = "employerEmail";
        } else if ("Agent".equals(userType)) {
            emailField = "agentEmail";
        } else if ("Employee".equals(userType)){
            emailField = "employeeEmail";
        }

        db.collection("bookings")
                .whereEqualTo(emailField, user.getEmail())
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

                                String employeeEmail = (String) record.get("employeeEmail");
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
                                                    updateUI();
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
                            updateUI();
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
    private void updateUI() {

        if (records.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            BookRecordlAdapter bookRecordlAdapter = new BookRecordlAdapter(getActivity(), records, userType,recyclerView,tvNoData);
            recyclerView.setAdapter(bookRecordlAdapter);
        }
    }
}