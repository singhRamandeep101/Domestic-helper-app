package com.project.fypproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.project.fypproject.R;
import com.project.fypproject.activities.Login;
import com.project.fypproject.activities.MeetingActivity;

import java.util.ArrayList;
import java.util.List;

public class AgentHomeFragment extends Fragment {

    private static final String TAG = "AgentHomeFragment";

    Button btnLogout;
    LinearLayout llPublicHoliday, llJobList, llJobPost;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    String userEmail;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emplyer_home, container, false);

        btnLogout = view.findViewById(R.id.btn_logout);
        llPublicHoliday = view.findViewById(R.id.btn_PublicHoliday);
        llJobList = view.findViewById(R.id.btn_Find);
        llJobPost = view.findViewById(R.id.btn_JobPost);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            changeActivity(Login.class);
            return view;
        }

        userEmail = user.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(getActivity(), "User email not found. Please sign in again.", Toast.LENGTH_LONG).show();
            changeActivity(Login.class);
            return view;
        }

        llJobList.setOnClickListener(v -> fetchBookingAndStartMeeting());

        return view;
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