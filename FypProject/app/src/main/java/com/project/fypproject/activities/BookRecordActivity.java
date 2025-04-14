package com.project.fypproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.project.fypproject.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookRecordActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookRecordAdapter adapter;
    private List<QueryDocumentSnapshot> bookingList = new ArrayList<>();
    private FirebaseFirestore db;
    private String userType, emailField, userEmail;
    private View tvNoRecord;

    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_record);

        recyclerView = findViewById(R.id.rv_bookRecord);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvNoRecord = findViewById(R.id.tv_no_data);
        btnBack = findViewById(R.id.btnBack);
        tvNoRecord = findViewById(R.id.tv_no_data);

        db = FirebaseFirestore.getInstance();

        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        fetchUserType();

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (emailField == null || userEmail == null) {
                    Log.e("Error", "emailField or userEmail is null. Skipping tab action.");
                    return;
                }

                switch (tab.getPosition()) {
                    case 0:
                        fetchBookingsForUpcoming();
                        break;
                    case 1:
                        fetchAllBookings();
                        break;
                    case 2:
                        fetchBookingsForNextMonth();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void fetchUserType() {
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        userType = task.getResult().getDocuments().get(0).getString("userType");
                        setEmailField();

                        adapter = new BookRecordAdapter(bookingList, userType);
                        recyclerView.setAdapter(adapter);

                        fetchBookingsForUpcoming();
                    } else {
                        Log.e("Firestore Error", "Failed to fetch user type");
                    }
                });
    }

    private void setEmailField() {
        switch (userType) {
            case "Employer":
                emailField = "employerEmail";
                break;
            case "Agent":
                emailField = "agentEmail";
                break;
            case "DomesticHelper":
                emailField = "employeeEmail";
                break;
            default:
                emailField = "translatorEmail";
                break;
        }
    }

    private void fetchBookingsForUpcoming() {
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.set(Calendar.HOUR_OF_DAY, 0);
        calendarStart.set(Calendar.MINUTE, 0);
        calendarStart.set(Calendar.SECOND, 0);
        calendarStart.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendarStart.getTime();

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.set(Calendar.HOUR_OF_DAY, 23);
        calendarEnd.set(Calendar.MINUTE, 59);
        calendarEnd.set(Calendar.SECOND, 59);
        calendarEnd.set(Calendar.MILLISECOND, 999);
        calendarEnd.add(Calendar.DAY_OF_YEAR, 7);
        Date sevenDaysLaterEnd = calendarEnd.getTime();

        fetchBookingsByDateRange(todayStart, sevenDaysLaterEnd, true); // true 表示要篩選 meetingStatus = "abc"
    }

    private void fetchAllBookings() {
        fetchBookingsByDateRange(null, null, false); // false 表示不篩選 meetingStatus
    }

    private void fetchBookingsForNextMonth() {
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startOfNextMonth = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        Date endOfNextMonth = calendar.getTime();

        fetchBookingsByDateRange(startOfNextMonth, endOfNextMonth, true); // true 表示要篩選 meetingStatus = "abc"
    }

    private void fetchBookingsByDateRange(Date startDate, Date endDate, boolean filterMeetingStatus) {
        if (emailField == null || userEmail == null) {
            Log.e("Error", "emailField or userEmail is null. Skipping query.");
            return;
        }

        // 構建基礎查詢
        com.google.firebase.firestore.Query query = db.collection("booking")
                .whereEqualTo(emailField, userEmail);

        // 如果需要，加入 meetingStatus 條件
        if (filterMeetingStatus) {
            query = query.whereEqualTo("meetingStatus", "abc");
        }

        query.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e("Firestore Error", "Error fetching bookings", e);
                return;
            }

            if (querySnapshot != null) {
                bookingList.clear();
                for (QueryDocumentSnapshot document : querySnapshot) {
                    try {
                        String dateStr = document.getString("date");
                        Date bookingDate = new SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH).parse(dateStr);

                        if ((startDate == null || !bookingDate.before(startDate)) &&
                                (endDate == null || !bookingDate.after(endDate))) {
                            bookingList.add(document);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (bookingList.isEmpty()) {
                    tvNoRecord.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvNoRecord.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
}