package com.project.fypproject.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import com.project.fypproject.R;

public class UserUpdateProfileActivity extends AppCompatActivity {
    EditText edFirstName, edLastName;
    Button btnUpdate;

    Spinner spAvail;
    TextView tvFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_update_profile);

        edFirstName = findViewById(R.id.edFirstName);
        edLastName = findViewById(R.id.edLastName);
        spAvail = findViewById(R.id.spAvail);
        btnUpdate = findViewById(R.id.btnUpdate);
        tvFullName = findViewById(R.id.tvFullName);

        String firstName = getIntent().getStringExtra("firstName");
        String lastName = getIntent().getStringExtra("lastName");
        String email = getIntent().getStringExtra("email");
        String availability = getIntent().getStringExtra("availability");

        edFirstName.setText(firstName);
        edLastName.setText(lastName);
        tvFullName.setText(lastName + " " + firstName);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Available", "Unavailable"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAvail.setAdapter(adapter);

        int position = adapter.getPosition(availability);
        spAvail.setSelection(position);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updateFirstName = edFirstName.getText().toString().trim();
                String updateLastName = edLastName.getText().toString().trim();
                String updateAvailability = spAvail.getSelectedItem().toString();

                if (updateFirstName.isEmpty()) {
                    Toast.makeText(UserUpdateProfileActivity.this, "Please enter FirstName", Toast.LENGTH_SHORT).show();
                }

                if (updateLastName.isEmpty()) {
                    Toast.makeText(UserUpdateProfileActivity.this, "Please enter LastName", Toast.LENGTH_SHORT).show();
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                Map<String, Object> updateData = new HashMap<>();
                updateData.put("firstName", updateFirstName);
                updateData.put("lastName", updateLastName);
                updateData.put("availability", updateAvailability);

                db.collection("users").document(email).update(updateData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(UserUpdateProfileActivity.this, "Update Success", Toast.LENGTH_SHORT).show();
                        db.collection("HelperInfo").whereEqualTo("userEmail", email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                        String docID = doc.getId();
                                        db.collection("HelperInfo").document(docID).update(updateData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                            }
                                        });
                                    }
                                }
                            }
                        });
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