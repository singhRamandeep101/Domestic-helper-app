package com.project.fypproject.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.project.fypproject.R;
import com.project.fypproject.activities.employer.JobListActivity;

import java.util.ArrayList;
import java.util.List;

public class AgentHomeFragment extends Fragment {

    private static final String TAG = "AgentHomeFragment";
    LinearLayout llAdd_dh, llJobList, llPublicholiday, llHiring, llAddAgent;
    FirebaseAuth auth;
    FirebaseUser user;
    String userEmail;
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_agent_home, container, false);

        db = FirebaseFirestore.getInstance();
        llAdd_dh = view.findViewById(R.id.btn_add_dh);
        llJobList = view.findViewById(R.id.btn_Find);
        llPublicholiday = view.findViewById(R.id.btn_meeting);
        llHiring = view.findViewById(R.id.btn_Hiring);
        llAddAgent = view.findViewById(R.id.btn_add_agent);
        userEmail = user.getEmail();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        llJobList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), JobListActivity.class);
                startActivity(intent);
            }
        });

        llHiring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HandlingHelperActivity.class);
                startActivity(intent);
            }
        });

        llAddAgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference docRef = db.collection("users").document(user.getEmail());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Boolean isAllowCreateAgent = document.getBoolean("allowCreateAgent");
                                if(isAllowCreateAgent){
                                    Intent intent = new Intent(getActivity(), Register.class);
                                    Bundle b = new Bundle();
                                    b.putString("userType", "Agent");
                                    intent.putExtras(b);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(getContext(), "You have no right to create a new Agent account", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.d("Dennis", "No such document");
                            }
                        } else {
                            Log.d("Dennis", "get failed with ", task.getException());
                        }
                    }
                });
            }
        });

        llAdd_dh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference docRef = db.collection("users").document(user.getEmail());
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Boolean isAllowCreateDH = document.getBoolean("allowCreateDH");
                                if(isAllowCreateDH){
                                    Intent intent = new Intent(getActivity(), Pdfbox.class);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(getContext(), "You have no right to create a new Domestic Helper resume", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.d("Dennis", "No such document");
                            }
                        } else {
                            Log.d("Dennis", "get failed with ", task.getException());
                        }
                    }
                });
            }
        });

        llPublicholiday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BookingRequestActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    public void changeActivity(Class<?> cls) {
        Intent intent = new Intent(getActivity(), cls);
        startActivity(intent);
        requireActivity().finish();
    }

    public void changeActivityWithBookingId(Class<?> cls, String bookingId) {
        Intent intent = new Intent(getActivity(), cls);
        intent.putExtra("bookingID", bookingId);
        startActivity(intent);
    }
    private void fetchBookingAndStartMeeting() {
        List<String> fields = new ArrayList<>();
        fields.add("agentEmail");
        fields.add("translatorEmail");
        fields.add("employeeEmail");
        fields.add("employerEmail");


        List<String> bookingIds = new ArrayList<>();
        fetchBookingsForField(fields, 0, bookingIds);
    }

    private void fetchBookingsForField(List<String> fields, int index, List<String> bookingIds) {
        if (index >= fields.size()) {
            if (bookingIds.isEmpty()) {
                Toast.makeText(getActivity(), "No bookings found for this user.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "No bookings found for email: " + userEmail);
                return;
            }

            String bookingId = bookingIds.get(0);
            Log.d(TAG, "Found booking with ID: " + bookingId + " for email: " + userEmail);
            changeActivityWithBookingId(MeetingActivity.class, bookingId);
            return;
        }

        String field = fields.get(index);
        Log.d(TAG, "Querying bookings where " + field + " = " + userEmail);
        db.collection("booking")
                .whereEqualTo(field, userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String bookingId = document.getId();
                        if (!bookingIds.contains(bookingId)) {
                            bookingIds.add(bookingId);
                        }
                    }
                    fetchBookingsForField(fields, index + 1, bookingIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch bookings for " + field, e);
                    Toast.makeText(getActivity(), "Failed to fetch bookings.", Toast.LENGTH_LONG).show();
                    fetchBookingsForField(fields, index + 1, bookingIds);
                });
    }
}