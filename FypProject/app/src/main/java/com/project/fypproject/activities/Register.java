package com.project.fypproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.R;

import java.util.Locale;
import java.util.Objects;

public class Register extends AppCompatActivity {

    public static class newUser{

        public newUser(String userType, String firstName, String lastName, String email, String availability, String telephone) {
            this.userType = userType;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.availability = availability;
            this.telephone = telephone;
        }

        public String getUserType() {
            return userType;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }

        public String getAvailability() {
            return availability;
        }
        public String getTelephone() {
            return telephone;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setAvailability(String availability) {
            this.availability = availability;
        }
        public void setTelephone(String telephone) {
            this.telephone = telephone;
        }
        private String userType;

        private String firstName;

        private String lastName;
        private String email;
        private String availability;
        private String telephone;
    }

    public static class newAgent{

        public newAgent(String firstName, String lastName, String email, String telephone, boolean isAllowCreateDH, boolean isAllowCreateAgent) {
            this.userType = userType;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.telephone = telephone;
            this.isAllowCreateDH = isAllowCreateDH;
            this.isAllowCreateAgent = isAllowCreateAgent;
        }

        public String getUserType() {
            return userType;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }
        public String getTelephone() {
            return telephone;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setEmail(String email) {
            this.email = email;
        }
        public void setTelephone(String telephone) {
            this.telephone = telephone;
        }
        private String userType = "Agent";

        private String firstName;

        private String lastName;
        private String email;
        private String telephone;

        public Boolean getAllowCreateDH() {
            return isAllowCreateDH;
        }

        public void setAllowCreateDH(Boolean allowCreateDH) {
            isAllowCreateDH = allowCreateDH;
        }

        public Boolean getAllowCreateAgent() {
            return isAllowCreateAgent;
        }

        public void setAllowCreateAgent(Boolean allowCreateAgent) {
            isAllowCreateAgent = allowCreateAgent;
        }

        private Boolean isAllowCreateDH;
        private Boolean isAllowCreateAgent;
    }
    EditText editTextEmail, editTextPassword,editTextConfirmPassword ,editTextFirstName, editTextLastName;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    LinearLayout llAgentOperator;

    Boolean isAllowCreateDH, isAllowCreateAgent;
    Switch switchDH, switchAgent;
    String Type = "hello";

    private void navigateToLoginPage() {
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        progressBar = findViewById(R.id.progressBar);
//        progressBar.setVisibility(View.VISIBLE);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle b = getIntent().getExtras();

        if(b != null){
            Type = b.getString("userType"); //Get the user type from last activity
        }

        if (Objects.equals(Type, "Agent")){
            llAgentOperator = findViewById(R.id.llAgentOperator);
            llAgentOperator.setVisibility(View.VISIBLE);

            switchDH = findViewById(R.id.switchDH);
            switchAgent = findViewById(R.id.switchAgent);
        }

        editTextFirstName = findViewById(R.id.FirstName);
        editTextLastName = findViewById(R.id.LastName);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.ConfirmPassword);

        buttonReg = findViewById(R.id.btn_register);
        mAuth = FirebaseAuth.getInstance();
        textView = findViewById(R.id.loginNow);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //click the text to go back to login page.
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName, lastName, email, password, confirmPassword;
                firstName = String.valueOf(editTextFirstName.getText());
                lastName = String.valueOf(editTextLastName.getText());
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                confirmPassword = String.valueOf(editTextConfirmPassword.getText());
                isAllowCreateDH = false;
                isAllowCreateAgent = false;

                if(TextUtils.isEmpty(firstName)){
                    Toast.makeText(Register.this, "Enter your first name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(lastName)){
                    Toast.makeText(Register.this, "Enter your last name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(confirmPassword)){
                    Toast.makeText(Register.this, "Confirm your password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!confirmPassword.equals(password)){
                    Toast.makeText(Register.this, "Password & confirm password must be same", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(Objects.equals(Type, "Agent")) {
                    isAllowCreateDH = switchDH.isChecked();
                    isAllowCreateAgent = switchAgent.isChecked();
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Account Created.",
                                            Toast.LENGTH_SHORT).show();

                                    if (!Objects.equals(Type, "Agent")) {
                                        newUser newUserData = new newUser(Type, firstName, lastName, email, "available", "'");

                                        // 將普通用戶資料存入 Firestore
                                        db.collection("users").document(email.toLowerCase(Locale.ROOT)).set(newUserData)
                                                .addOnSuccessListener(aVoid -> {
                                                    navigateToLoginPage();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(Register.this, "Error creating user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        newAgent newAgentData = new newAgent(firstName, lastName, email, "", isAllowCreateDH, isAllowCreateAgent);

                                        // 將Agent用戶資料存入 Firestore
                                        db.collection("users").document(email.toLowerCase(Locale.ROOT)).set(newAgentData)
                                                .addOnSuccessListener(aVoid -> {
                                                    mAuth.signOut(); // 登出
                                                    Toast.makeText(Register.this, "A new agent account created successfully", Toast.LENGTH_SHORT).show();
                                                    navigateToLoginPage();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(Register.this, "Error creating agent: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Register.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}