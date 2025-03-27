package com.project.fypproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;
import com.project.fypproject.models.ChatModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EmployerSelectTimeActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Spinner spinnerStartTime, spinnerEndTime;
    private Button btnBack, btnContinue;
    private FirebaseFirestore db;
    private ArrayList<String> bookedTimes;

    private String selectedDate = "";
    private String startTime = "";
    private String endTime = "";
    String employeeEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_book_time);

        db = FirebaseFirestore.getInstance();
        bookedTimes = new ArrayList<>();
        employeeEmail = getIntent().getStringExtra("employeeEmail");

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
                    Toast.makeText(EmployerSelectTimeActivity.this, "Please select a valid date.", Toast.LENGTH_SHORT).show();
                    return;
                }

                checkAgent();
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
                .whereEqualTo("employeeEmail", employeeEmail)
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

                            ArrayAdapter<String> startAdapter = new ArrayAdapter<>(EmployerSelectTimeActivity.this, android.R.layout.simple_spinner_item, availableTimes);
                            startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerStartTime.setAdapter(startAdapter);

                            spinnerEndTime.setAdapter(null);

                        } else {
                            Toast.makeText(EmployerSelectTimeActivity.this, "Error fetching select for the selected date.", Toast.LENGTH_SHORT).show();
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

        ArrayAdapter<String> endAdapter = new ArrayAdapter<>(EmployerSelectTimeActivity.this, android.R.layout.simple_spinner_item, availableEndTimes);
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
    private void checkAgent() {

        // Step 1: Query MaidInfo to find the document with the given employeeEmail
        db.collection("MaidInfo")
                .whereEqualTo("email", employeeEmail) // Search for the email in the collection
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            QuerySnapshot maidInfoDocs = task.getResult();

                            if (!maidInfoDocs.isEmpty()) {
                                // If a document with the given employeeEmail exists
                                DocumentSnapshot maidInfoDoc = maidInfoDocs.getDocuments().get(0);

                                // Check if the document has an agentEmail field
                                String agentEmail = maidInfoDoc.getString("agentEmail");

                                if (agentEmail != null && !agentEmail.isEmpty()) {
                                    // If agentEmail already exists, no further action needed
                                    openChatRoom(agentEmail);
                                } else {
                                    // If agentEmail is missing, assign a new agent
                                    assignAgent(db, maidInfoDoc.getId());
                                }
                            } else {
                                // If no document contains the employeeEmail, log it
                                Log.e("Firestore", "No document found for employeeEmail: " + employeeEmail);
                            }
                        } else {
                            Log.e("Firestore", "Failed to query MaidInfo collection", task.getException());
                        }
                    }
                });
    }

    // Randomly selects a user with userType 'agent' and assigns their email to the specified MaidInfo document
    private void assignAgent(FirebaseFirestore db, String maidInfoDocId) {
        db.collection("users")
                .whereEqualTo("userType", "Agent") // Query for users with userType = 'agent'
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            QuerySnapshot querySnapshot = task.getResult();

                            if (!querySnapshot.isEmpty()) {
                                // Randomly select one of the agents
                                List<DocumentSnapshot> agents = querySnapshot.getDocuments();
                                DocumentSnapshot randomAgent = agents.get(new Random().nextInt(agents.size()));

                                // Get the agent's email
                                String agentEmail = randomAgent.getString("email");

                                if (agentEmail != null) {
                                    // Step 2: Assign the agent's email to the specified MaidInfo document
                                    db.collection("MaidInfo")
                                            .document(maidInfoDocId)
                                            .update("agentEmail", agentEmail)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    openChatRoom(agentEmail);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("Firestore", "Failed to assign agentEmail", e);
                                                }
                                            });
                                } else {
                                    Log.e("Firestore", "Selected agent does not have an email field");
                                }
                            } else {
                                Log.e("Firestore", "No users with userType = 'agent' found");
                            }
                        } else {
                            Log.e("Firestore", "Failed to query users collection", task.getException());
                        }
                    }
                });
    }
    private void openChatRoom(String agentEmail) {
        // Step 3: Query users collection for the agent's details using agentEmail
        db.collection("users")
                .whereEqualTo("email", employeeEmail) // Search for the agent by email
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            QuerySnapshot Docs = task.getResult();

                            if (!Docs.isEmpty()) {
                                // Get the agent document
                                DocumentSnapshot Doc = Docs.getDocuments().get(0);

                                // Retrieve agent's details
                                String firstName = Doc.getString("firstName");
                                String lastName = Doc.getString("lastName");

                                if (firstName != null && lastName != null) {
                                    // Step 4: Create and set ChatModel
                                    ChatModel chatModel = new ChatModel();
                                    chatModel.setEmail(agentEmail);
                                    chatModel.setFirstName(firstName);
                                    chatModel.setLastName(lastName);

                                    Intent intent = new Intent(EmployerSelectTimeActivity.this, ChatActivity.class);
                                    ChatUtil.passUserIntent(intent, chatModel); // Pass ChatModel via ChatUtil
                                    intent.putExtra("message", "Ideal Booking Detail\nDate:" + selectedDate +"\nTime:" + startTime + " to " + endTime);
                                    startActivity(intent); // Start ChatActivity

                                    // Log the information for verification
                                    Log.d("Firestore", "ChatModel set with agent details: " +
                                            "FirstName: " + firstName + ", LastName: " + lastName +
                                            ", AgentEmail: " + agentEmail);

                                    // Optionally, you can save ChatModel to Firestore or proceed to the next action
                                } else {
                                    Log.e("Firestore", "Agent details are incomplete for email: " + employeeEmail);
                                }
                            } else {
                                Log.e("Firestore", "No document found for agentEmail: " + employeeEmail);
                            }
                        } else {
                            Log.e("Firestore", "Failed to query users collection", task.getException());
                        }
                    }
                });
    }
}