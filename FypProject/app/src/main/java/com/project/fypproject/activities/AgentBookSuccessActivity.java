package com.project.fypproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;
import com.project.fypproject.models.ChatModel;

public class AgentBookSuccessActivity extends AppCompatActivity {

    private Button btnBackHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_book_success);

        btnBackHome = findViewById(R.id.btn_backHome);

        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String agentEmail = getIntent().getStringExtra("agentEmail");
                String employeeEmail = getIntent().getStringExtra("employeeEmail");
                String employerEmail = getIntent().getStringExtra("employerEmail");
                String selectedDate = getIntent().getStringExtra("date");
                String startTime = getIntent().getStringExtra("startTime");
                String endTime = getIntent().getStringExtra("endTime");
                    db.collection("users")
                            .whereEqualTo("email", employerEmail) // Search for the agent by email
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
                                                chatModel.setEmail(employerEmail);
                                                chatModel.setFirstName(firstName);
                                                chatModel.setLastName(lastName);

                                                Intent intent = new Intent(AgentBookSuccessActivity.this, ChatActivity.class);
                                                ChatUtil.passUserIntent(intent, chatModel); // Pass ChatModel via ChatUtil
                                                intent.putExtra("employeeEmail", employeeEmail);
                                                intent.putExtra("agentEmail", agentEmail);

                                                intent.putExtra("message", "Booking Success\n Booking Detail\nDate:" + selectedDate +"\nTime:" + startTime + " to " + endTime);
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
                                            Log.e("Firestore", "No document found for employeetEmail: " + employeeEmail);
                                        }
                                    } else {
                                        Log.e("Firestore", "Failed to query users collection", task.getException());
                                    }
                                }
                            });
            }
        });
    }
}