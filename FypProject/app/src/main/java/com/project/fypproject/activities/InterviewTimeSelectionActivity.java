package com.project.fypproject.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.project.fypproject.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class InterviewTimeSelectionActivity extends AppCompatActivity {

    private TextView tvSelectDate;
    private Button btnToday;
    private Button btnSelectAll;
    private Button btnCancelAll;
    private Button btnInvite;
    private HashMap<String, HashMap<String, Boolean>> selectedTime = new HashMap<>();
    private HashMap<String, HashMap<String, Boolean>> bookedTime = new HashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy (EEE)", Locale.ENGLISH);
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private FirebaseFirestore db;
    private String currentSelectedDate;
    private String employeeEmail;
    FirebaseAuth auth;
    FirebaseUser user;
    ImageView img_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview_time_selection);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        tvSelectDate = findViewById(R.id.selectDate);
        btnToday = findViewById(R.id.btnToday);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnCancelAll = findViewById(R.id.btnCancelAll);
        btnInvite = findViewById(R.id.Confirm);
        img_back = findViewById(R.id.img_back);

        // Get employeeEmail from the previous activity
        employeeEmail = getIntent().getStringExtra("employeeEmail");

        // Initialize current date
        Calendar calendar = Calendar.getInstance();
        currentSelectedDate = dateFormat.format(calendar.getTime());
        tvSelectDate.setText(currentSelectedDate);

        // Load booked slots from Firestore
        checkBooked();

        // Initialize time slot buttons
        final int[] timeSlotIds = {
                R.id.btn1000TO1100, R.id.btn1100TO1200,
                R.id.btn1200TO1300, R.id.btn1300TO1400,
                R.id.btn1400TO1500, R.id.btn1500TO1600,
                R.id.btn1600TO1700, R.id.btn1700TO1800
        };

        for (int id : timeSlotIds) {
            final Button timeSlotButton = findViewById(id);
            timeSlotButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeButtonSelect(timeSlotButton);
                }
            });
        }

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Set today's date
        btnToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setToday();
            }
        });

        // Open date picker
        tvSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePicker();
            }
        });

        // Select all time slots
        btnSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectAllTime(timeSlotIds);
            }
        });

        // Cancel all time slots
        btnCancelAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAllTime(timeSlotIds);
            }
        });

        // Save to Firestore
        btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkTimeSelect()) {
                    save();
                } else {
                    Toast.makeText(InterviewTimeSelectionActivity.this, "Please select at least one time slot!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkBooked() {
        db.collection("booking")
                .whereEqualTo("employeeEmail", employeeEmail)
                .get()
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<com.google.firebase.firestore.QuerySnapshot>() {
                    @Override
                    public void onSuccess(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String bookedDate = document.getString("date");
                            String bookedTimeSlot = document.getString("TimeSlot");

                            if (bookedDate != null && bookedTimeSlot != null) {
                                HashMap<String, Boolean> timeSlots = bookedTime.getOrDefault(bookedDate, new HashMap<>());
                                timeSlots.put(bookedTimeSlot, true);
                                bookedTime.put(bookedDate, timeSlots);
                            }
                        }
                        updateSelectedDate(); // Update the UI after loading booked slots
                    }
                });
    }

    private void setToday() {
        Calendar calendar = Calendar.getInstance();
        currentSelectedDate = dateFormat.format(calendar.getTime());
        tvSelectDate.setText(currentSelectedDate);
        updateSelectedDate();
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(currentSelectedDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        currentSelectedDate = dateFormat.format(selectedDate.getTime());
                        tvSelectDate.setText(currentSelectedDate);
                        updateSelectedDate();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        datePickerDialog.show();
    }

    private void changeButtonSelect(Button timeSlotButton) {
        String timeSlot = timeSlotButton.getText().toString();
        HashMap<String, Boolean> timeSlots = selectedTime.getOrDefault(currentSelectedDate, new HashMap<>());

        if (timeSlots.containsKey(timeSlot)) {
            timeSlots.remove(timeSlot);
            timeSlotButton.setBackgroundResource(R.drawable.button_outlined);
            timeSlotButton.setTextColor(getResources().getColor(R.color.surface_tint));
        } else {
            timeSlots.put(timeSlot, true);
            timeSlotButton.setBackgroundResource(R.drawable.button_outlined_selected);
            timeSlotButton.setTextColor(getResources().getColor(android.R.color.white));
        }

        if (timeSlots.isEmpty()) {
            selectedTime.remove(currentSelectedDate);
        } else {
            selectedTime.put(currentSelectedDate, timeSlots);
        }
    }

    private void selectAllTime(int[] timeSlotIds) {
        HashMap<String, Boolean> timeSlots = selectedTime.getOrDefault(currentSelectedDate, new HashMap<>());

        for (int id : timeSlotIds) {
            Button timeSlotButton = findViewById(id);
            String timeSlot = timeSlotButton.getText().toString();
            if (!isBooked(timeSlot)) {
                timeSlots.put(timeSlot, true);
                timeSlotButton.setBackgroundResource(R.drawable.button_outlined_selected);
                timeSlotButton.setTextColor(getResources().getColor(android.R.color.white));
            }
        }

        selectedTime.put(currentSelectedDate, timeSlots);
    }

    private void cancelAllTime(int[] timeSlotIds) {
        selectedTime.remove(currentSelectedDate); // Remove all slots for the current date

        for (int id : timeSlotIds) {
            Button timeSlotButton = findViewById(id);

            if (!timeSlotButton.isEnabled()) {
                continue;
            }

            timeSlotButton.setBackgroundResource(R.drawable.button_outlined);
            timeSlotButton.setTextColor(getResources().getColor(R.color.surface_tint));
        }
    }

    private void updateSelectedDate() {
        HashMap<String, Boolean> bookedSlots = bookedTime.getOrDefault(currentSelectedDate, new HashMap<>());
        HashMap<String, Boolean> timeSlots = selectedTime.getOrDefault(currentSelectedDate, new HashMap<>());

        int[] timeSlotIds = {
                R.id.btn1000TO1100, R.id.btn1100TO1200,
                R.id.btn1200TO1300, R.id.btn1300TO1400,
                R.id.btn1400TO1500, R.id.btn1500TO1600,
                R.id.btn1600TO1700, R.id.btn1700TO1800
        };

        for (int id : timeSlotIds) {
            Button timeSlotButton = findViewById(id);
            String timeSlot = timeSlotButton.getText().toString();

            if (bookedSlots.containsKey(timeSlot) && bookedSlots.get(timeSlot)) {
                timeSlotButton.setEnabled(false);
                timeSlotButton.setBackgroundResource(R.drawable.button_outlined_disabled);
                timeSlotButton.setTextColor(getResources().getColor(android.R.color.darker_gray));
            } else if (timeSlots.containsKey(timeSlot) && timeSlots.get(timeSlot)) {
                timeSlotButton.setEnabled(true);
                timeSlotButton.setBackgroundResource(R.drawable.button_outlined_selected);
                timeSlotButton.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                timeSlotButton.setEnabled(true);
                timeSlotButton.setBackgroundResource(R.drawable.button_outlined);
                timeSlotButton.setTextColor(getResources().getColor(R.color.surface_tint));
            }
        }
    }

    private boolean isBooked(String timeSlot) {
        HashMap<String, Boolean> bookedSlots = bookedTime.getOrDefault(currentSelectedDate, new HashMap<>());
        return bookedSlots.containsKey(timeSlot) && bookedSlots.get(timeSlot);
    }

    private boolean checkTimeSelect() {
        for (HashMap<String, Boolean> timeSlots : selectedTime.values()) {
            for (boolean isSelected : timeSlots.values()) {
                if (isSelected) return true; // At least one slot is selected
            }
        }
        return false;
    }

    private void save() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("employerEmail", user.getEmail());
        data.put("employeeEmail", employeeEmail);
        data.put("employerSelectedTime", selectedTime);
        data.put("employerState", "Pending Confirmation");
        data.put("internalState", "Awaiting Action");
        data.put("postTime", dateTimeFormat.format(Calendar.getInstance().getTime()));

        db.collection("users").whereEqualTo("userType", "Agent").get()
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<com.google.firebase.firestore.QuerySnapshot>() {
                    @Override
                    public void onSuccess(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots) {
                        String agentEmail = getRandomEmail(queryDocumentSnapshots);
                        data.put("agentEmail", agentEmail);

                        db.collection("users").whereEqualTo("userType", "Translator").get()
                                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<com.google.firebase.firestore.QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(com.google.firebase.firestore.QuerySnapshot translatorSnapshots) {
                                        String translatorEmail = getRandomEmail(translatorSnapshots);
                                        data.put("translatorEmail", translatorEmail);

                                        db.collection("interview_request").add(data)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Intent intent = new Intent(InterviewTimeSelectionActivity.this, InterviewTimeInvitedActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(InterviewTimeSelectionActivity.this, "Failed to save!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private String getRandomEmail(Iterable<QueryDocumentSnapshot> snapshots) {
        int count = 0;
        String randomEmail = null;
        for (QueryDocumentSnapshot snapshot : snapshots) {
            if (new Random().nextInt(++count) == 0) {
                randomEmail = snapshot.getString("email");
            }
        }
        return randomEmail;
    }
}