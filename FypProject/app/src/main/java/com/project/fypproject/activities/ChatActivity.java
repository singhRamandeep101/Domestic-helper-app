package com.project.fypproject.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;

import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    List<Map<String, Object>> records;
    FirebaseFirestore db;
    RecyclerView recyclerView;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView tvName;

    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        tvName = findViewById(R.id.tvName);

        String employerEmail = getIntent().getStringExtra("employerEmail");

        db = FirebaseFirestore.getInstance();

        db.collection("MaidInfo")
                .whereEqualTo("email", "a")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                          @Override
                                          public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                              if (!queryDocumentSnapshots.isEmpty()) {

                                                  DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                                                  String agentEmail = (String) doc.get("agentEmail");

                                                  if (agentEmail != null) {
                                                      db.collection("users")
                                                              .whereEqualTo("email", "agentd@gmail.com")
                                                              .get()
                                                              .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                  @Override
                                                                  public void onSuccess(QuerySnapshot agentSnapshots) {
                                                                      if (!agentSnapshots.isEmpty()) {
                                                                          DocumentSnapshot agentDoc = agentSnapshots.getDocuments().get(0);
                                                                          String fName = (String) agentDoc.get("firstName");
                                                                          String lName = (String) agentDoc.get("lastName");
                                                                          tvName.setText(lName + " "+fName);
                                                                      } else {
                                                                          Log.e("Firestore", "Error find Agent Name");
                                                                      }
                                                                  }
                                                              })
                                                              .addOnFailureListener(new OnFailureListener() {
                                                                  @Override
                                                                  public void onFailure(@NonNull Exception e) {
                                                                      Log.e("Firestore", "Error find Agent Data", e);
                                                                  }
                                                              });
                                                  } else {
                                                      Log.e("Firestore", "no find agent Email");
                                                  }
                                              }
                                          }
                                      });

                            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                                return insets;
                            });
                        }
                    }