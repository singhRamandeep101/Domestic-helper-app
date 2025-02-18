package com.project.fypproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

    EditText editTextEmail, editTextPassword,editTextConfirmPassword ,editTextFirstName, editTextLastName;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    String Type = "hello";

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

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Account Created.",
                                            Toast.LENGTH_SHORT).show();

                                    newUser data = new newUser(Type, firstName, lastName, email, "available", "'");

                                    db.collection("users").document(email.toLowerCase(Locale.ROOT)).set(data);

                                    //Go Back to the login page.
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();

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