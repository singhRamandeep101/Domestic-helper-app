package com.project.fypproject.activities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookingHelper {

    private static final String TAG = "BookingHelper";
    private final FirebaseFirestore db;
    private final Context context;

    // Callback interface to return the bookingID to the caller
    public interface BookingCallback {
        void onBookingFound(String bookingId);
        void onNoBookingsFound();
        void onError(String errorMessage);
    }

    public BookingHelper(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public void fetchBookingForUser(String userEmail, BookingCallback callback) {
        if (userEmail == null || userEmail.isEmpty()) {
            callback.onError("User email is null or empty.");
            return;
        }

        List<String> fields = new ArrayList<>();
        fields.add("agentEmail");
        fields.add("translatorEmail");
        fields.add("employeeEmail");
        fields.add("employerEmail");

        List<String> bookingIds = new ArrayList<>();
        fetchBookingsForField(fields, 0, userEmail, bookingIds, callback);
    }

    private void fetchBookingsForField(List<String> fields, int index, String userEmail, List<String> bookingIds, BookingCallback callback) {
        if (index >= fields.size()) {
            if (bookingIds.isEmpty()) {
                Log.e(TAG, "No bookings found for email: " + userEmail);
                callback.onNoBookingsFound();
                return;
            }

            String bookingId = bookingIds.get(0);
            Log.d(TAG, "Found booking with ID: " + bookingId + " for email: " + userEmail);
            callback.onBookingFound(bookingId);
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
                    fetchBookingsForField(fields, index + 1, userEmail, bookingIds, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch bookings for " + field, e);
                    callback.onError("Failed to fetch bookings.");
                    fetchBookingsForField(fields, index + 1, userEmail, bookingIds, callback);
                });
    }
}