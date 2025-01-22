package com.project.fypproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import com.project.fypproject.R;

public class UserProfileActivity extends AppCompatActivity {
    TextView tvFullName, tvFirstName, tvLastName, tvEmail, tvAvailability;
    Button btnUpdateProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        tvFullName = findViewById(R.id.tvFullName);
        tvFirstName = findViewById(R.id.tvFirstName);
        tvLastName = findViewById(R.id.tvLastName);
        tvEmail = findViewById(R.id.tvEmail);
        tvAvailability = findViewById(R.id.tvAvailability);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);

//        String userEmail = getIntent().getStringExtra("userEmail");
        String userEmail = "jasonp@gmail.com";

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").whereEqualTo("email", userEmail).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                String firstName = doc.getString("firstName");
                String lastName = doc.getString("lastName");
                String email = doc.getString("email");
                String availability = doc.getString("availability");

                tvFullName.setText(lastName + " " + firstName);
                tvFirstName.setText(firstName);
                tvLastName.setText(lastName);
                tvEmail.setText(email);
                tvAvailability.setText(availability);

                btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(UserProfileActivity.this, UserUpdateProfileActivity.class);
                        intent.putExtra("firstName",firstName);
                        intent.putExtra("lastName",lastName);
                        intent.putExtra("email",email);
                        intent.putExtra("availability",availability);

                        startActivity(intent);
                    }
                });
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
