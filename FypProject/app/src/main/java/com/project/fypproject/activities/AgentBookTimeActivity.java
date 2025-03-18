package com.project.fypproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AgentBookTimeActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Spinner spinnerStartTime, spinnerEndTime;
    private Button btnBack, btnContinue;
    private FirebaseFirestore db;
    private ArrayList<String> bookedTimes;

    private String selectedDate = "";
    private String startTime = "";
    private String endTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_book_time);

        db = FirebaseFirestore.getInstance();
        bookedTimes = new ArrayList<>();

        calendarView = findViewById(R.id.calendar_view);
        spinnerStartTime = findViewById(R.id.spinner_start_time);
        spinnerEndTime = findViewById(R.id.spinner_end_time);
        btnBack = findViewById(R.id.btn_back);
        btnContinue = findViewById(R.id.btn_continue);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        selectedDate = year + "-" + (month + 1) + "-" + day;

        calendarView.setMinDate(System.currentTimeMillis());

        updateAvailableTime();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;

                updateAvailableTime();
            }
        });

        spinnerStartTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                startTime = spinnerStartTime.getSelectedItem().toString();
                updateEndTime(startTime);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = spinnerStartTime.getSelectedItem().toString();
                endTime = spinnerEndTime.getSelectedItem().toString();

                if (selectedDate.isEmpty()) {
                    Toast.makeText(AgentBookTimeActivity.this, "Please select a valid date.", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveBooking();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updateAvailableTime() {
        bookedTimes.clear();

        db.collection("bookings")
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String stime = document.getString("startTime");
                                String etime = document.getString("endTime");
                                addBookedTimeRange(stime, etime);
                            }

                            String[] allTimes = {"8 am", "9 am", "10 am", "11 am", "12 pm", "1 pm", "2 pm", "3 pm", "4 pm", "5 pm", "6 pm", "7 pm", "8 pm", "9 pm", "10 pm", "11 pm"};

                            ArrayList<String> availableTimes = new ArrayList<>();
                            for (String time : allTimes) {
                                if (!bookedTimes.contains(time)) {
                                    availableTimes.add(time);
                                }
                            }

                            if (availableTimes.isEmpty()) {
                                availableTimes.add("No available time");
                                btnContinue.setVisibility(View.GONE); // Hide Continue button
                                btnBack.setBackgroundColor(android.graphics.Color.parseColor("#6A1B9A"));
                            } else {
                                btnContinue.setVisibility(View.VISIBLE); // Show Continue button
                                btnBack.setBackgroundColor(android.graphics.Color.parseColor("#757575"));
                            }

                            ArrayAdapter<String> startAdapter = new ArrayAdapter<>(AgentBookTimeActivity.this, android.R.layout.simple_spinner_item, availableTimes);
                            startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerStartTime.setAdapter(startAdapter);

                            spinnerEndTime.setAdapter(null);

                        } else {
                            Toast.makeText(AgentBookTimeActivity.this, "Error fetching bookings for the selected date.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateEndTime(String selectedStartTime) {
        String[] allTimes = {"8 am", "9 am", "10 am", "11 am", "12 pm", "1 pm", "2 pm", "3 pm", "4 pm", "5 pm", "6 pm", "7 pm", "8 pm", "9 pm", "10 pm", "11 pm"};

        int startIndex = -1;
        for (int i = 0; i < allTimes.length; i++) {
            if (allTimes[i].equals(selectedStartTime)) {
                startIndex = i;
                break;
            }
        }

        ArrayList<String> availableEndTimes = new ArrayList<>();
        if (startIndex != -1) {
            for (int i = startIndex + 1; i < allTimes.length; i++) {
                if (!bookedTimes.contains(allTimes[i])) {
                    availableEndTimes.add(allTimes[i]);
                }
            }
        }

        if (availableEndTimes.isEmpty()) {
            availableEndTimes.add("No available time");
            btnContinue.setVisibility(View.GONE);
        } else {
            btnContinue.setVisibility(View.VISIBLE);
        }

        ArrayAdapter<String> endAdapter = new ArrayAdapter<>(AgentBookTimeActivity.this, android.R.layout.simple_spinner_item, availableEndTimes);
        endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEndTime.setAdapter(endAdapter);
    }

    private void addBookedTimeRange(String startTime, String endTime) {
        String[] allTimes = {"8 am", "9 am", "10 am", "11 am", "12 pm", "1 pm", "2 pm", "3 pm", "4 pm", "5 pm", "6 pm", "7 pm", "8 pm", "9 pm", "10 pm", "11 pm"};
        int startIndex = -1, endIndex = -1;

        for (int i = 0; i < allTimes.length; i++) {
            if (allTimes[i].equals(startTime)) {
                startIndex = i;
            }
            if (allTimes[i].equals(endTime)) {
                endIndex = i;
            }
        }

        if (startIndex != -1 && endIndex != -1) {
            for (int i = startIndex; i <= endIndex; i++) {
                bookedTimes.add(allTimes[i]);
            }
        }
    }

    private void saveBooking() {
        String employeeEmail = getIntent().getStringExtra("employeeEmail");
        String employerEmail = getIntent().getStringExtra("employerEmail");

        Map<String, String> booking = new HashMap<>();
        booking.put("date", selectedDate);
        booking.put("startTime", startTime);
        booking.put("endTime", endTime);
        booking.put("employeeEmail", employeeEmail);
        booking.put("employerEmail", employerEmail);
        booking.put("state", "Waiting for interview");

        db.collection("bookings")
                .add(booking)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(AgentBookTimeActivity.this, AgentBookSuccessActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(AgentBookTimeActivity.this, "Error saving booking.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}