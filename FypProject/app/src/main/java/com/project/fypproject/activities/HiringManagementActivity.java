package com.project.fypproject.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;

import java.util.Locale;

public class HiringManagementActivity extends AppCompatActivity {


    public static class newHiringRecord {
        public String agent;
        public Boolean body_check;
        public String domestic_helper;
        public String employer;
        public Boolean insurance;
        public Boolean ready;
        public int status;

        public newHiringRecord(String agent, Boolean body_check, String domestic_helper, String employer, Boolean insurance, Boolean ready, int status) {
            this.agent = agent;
            this.body_check = body_check;
            this.domestic_helper = domestic_helper;
            this.employer = employer;
            this.insurance = insurance;
            this.ready = ready;
            this.status = status;
        }
    }

    ImageView btnBack;
    LinearLayout llRegistration, llUpdate;
    EditText DH_Email, Agent_Email, Employer_Email;
    Button btn_register, btn_update;
    CheckBox cbBodyCheck, cbInsurance;
    Spinner statusSpinner;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hiring_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //<editor-fold desc="Declaration of UI Element">
        btnBack = findViewById(R.id.btnBack);
        llRegistration = findViewById(R.id.llRegistration);
        DH_Email = findViewById(R.id.DH_Email);
        Agent_Email = findViewById(R.id.Agent_Email);
        Employer_Email = findViewById(R.id.Employer_Email);
        btn_register = findViewById(R.id.btn_register);
        llUpdate = findViewById(R.id.llUpdate);
        cbBodyCheck = findViewById(R.id.cbBodyCheck);
        cbInsurance = findViewById(R.id.cbInsurance);
        statusSpinner = findViewById(R.id.statusSpinner);
        btn_update = findViewById(R.id.btn_update);
        //</editor-fold>

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String helperEmail = getIntent().getStringExtra("email");

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        llRegistration.setVisibility(View.GONE);
        llUpdate.setVisibility(View.GONE);

        db.collection("HiringStatus")
                .whereEqualTo("domestic_helper", helperEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // 有對應記錄，顯示註冊區
                                llUpdate.setAlpha(0f);
                                llUpdate.setVisibility(View.VISIBLE);
                                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(llUpdate, "alpha", 0f, 1f);
                                fadeIn.setDuration(300);
                                fadeIn.start();

                            } else {
                                // 冇對應記錄，顯示更新區
                                llRegistration.setAlpha(0f);
                                llRegistration.setVisibility(View.VISIBLE);
                                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(llRegistration, "alpha", 0f, 1f);
                                fadeIn.setDuration(300);
                                fadeIn.start();

                                DH_Email.setText(helperEmail);
                                DH_Email.setEnabled(false);

                                Agent_Email.setText(user.getEmail());
                                Agent_Email.setEnabled(false);

                                btn_register.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String agent, domestic_helper, employer;
                                        employer = String.valueOf(Employer_Email.getText());
                                        agent = user.getEmail();
                                        domestic_helper = helperEmail;

                                        if(TextUtils.isEmpty(employer)){
                                            Toast.makeText(HiringManagementActivity.this, "Enter the employer email", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        newHiringRecord newHiringRecord = new newHiringRecord(agent, false, domestic_helper, employer, false, false, 2);
                                        db.collection("HiringStatus").add(newHiringRecord)
                                                .addOnSuccessListener(aVoid -> {
                                                    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(llRegistration, "alpha", 1f, 0f);
                                                    fadeOut.setDuration(300);
                                                    fadeOut.addListener(new AnimatorListenerAdapter() {
                                                        @Override
                                                        public void onAnimationEnd(Animator animation) {
                                                            llRegistration.setVisibility(View.GONE);
                                                            llUpdate.setAlpha(0f);
                                                            llUpdate.setVisibility(View.VISIBLE);
                                                            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(llUpdate, "alpha", 0f, 1f);
                                                            fadeIn.setDuration(300);
                                                            fadeIn.start();
                                                        }
                                                    });
                                                    fadeOut.start();

                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(HiringManagementActivity.this, "Error creating agent: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                });
                            }
                        } else {
                            Log.d("Firestore", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}