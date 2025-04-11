package com.project.fypproject.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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


    // <editor-fold desc="Declare The Classes of User Type">
    public static class User {
        private String userType;
        private String firstName;
        private String lastName;
        private String email;
        private String telephone;

        // Constructor
        public User(String userType, String firstName, String lastName, String email, String telephone) {
            this.userType = userType;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.telephone = telephone;
        }

        // Getters and Setters
        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTelephone() {
            return telephone;
        }

        public void setTelephone(String telephone) {
            this.telephone = telephone;
        }
    }

    public static class newUser extends User {
        private String availability;

        // Constructor
        public newUser(String userType, String firstName, String lastName, String email, String availability, String telephone) {
            super(userType, firstName, lastName, email, telephone);
            this.availability = availability;
        }

        // Getter and Setter for availability
        public String getAvailability() {
            return availability;
        }

        public void setAvailability(String availability) {
            this.availability = availability;
        }
    }

    public static class newAgent extends User {
        private Boolean isAllowCreateDH;
        private Boolean isAllowCreateAgent;

        // Constructor
        public newAgent(String firstName, String lastName, String email, String telephone, Boolean isAllowCreateDH, Boolean isAllowCreateAgent) {
            super("Agent", firstName, lastName, email, telephone);
            this.isAllowCreateDH = isAllowCreateDH;
            this.isAllowCreateAgent = isAllowCreateAgent;
        }

        // Getters and Setters for Agent-specific properties
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
    }

    public static class newTranslator extends User {
        // Constructor
        public newTranslator(String firstName, String lastName, String email, String telephone) {
            super("Translator", firstName, lastName, email, telephone);
        }
    }
    // </editor-fold>

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

    private void setupAgentUI(String userType) {
        if (!"Agent".equals(userType)) {
            // 唔係 Agent 就唔做野，UI 隱藏
            return;
        }

        RadioGroup radioGroup = findViewById(R.id.rgUserType);
        llAgentOperator = findViewById(R.id.llAgentOperator);

        // Show RadioGroup，LinearLayout is hide by default
        radioGroup.setVisibility(View.VISIBLE);
        llAgentOperator.setVisibility(View.GONE);

        // 整個監聽器 - 視乎個用家係唔係揀咗agent 而去決定show唔show下面嗰啲agent operator
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbtnAgent) {
                    // Set show animation LinearLayout
                    ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(llAgentOperator, "alpha", 0f, 1f);
                    alphaAnimator.setDuration(300);

                    ValueAnimator heightAnimator = ValueAnimator.ofInt(0, (int) getResources().getDimension(R.dimen.agent_operator_height)); // 自定義高度
                    heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int value = (int) animation.getAnimatedValue();
                            llAgentOperator.getLayoutParams().height = value;
                            llAgentOperator.requestLayout();
                        }
                    });
                    heightAnimator.setDuration(300);

                    //Animation to show LinearLayout
                    alphaAnimator.start();
                    heightAnimator.start();

                    llAgentOperator.setVisibility(View.VISIBLE);
                } else {
                    // Set hide animation LinearLayout
                    ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(llAgentOperator, "alpha", 1f, 0f);
                    alphaAnimator.setDuration(300);

                    ValueAnimator heightAnimator = ValueAnimator.ofInt((int) getResources().getDimension(R.dimen.agent_operator_height), 0);
                    heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int value = (int) animation.getAnimatedValue();
                            llAgentOperator.getLayoutParams().height = value;
                            llAgentOperator.requestLayout();
                        }
                    });
                    heightAnimator.setDuration(300);

                    // Hide LinearLayout
                    heightAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            llAgentOperator.setVisibility(View.GONE);
                        }
                    });

                    alphaAnimator.start();
                    heightAnimator.start();
                }
            }
        });
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

        // initialising the UI element
        editTextFirstName = findViewById(R.id.FirstName);
        editTextLastName = findViewById(R.id.LastName);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.ConfirmPassword);

        buttonReg = findViewById(R.id.btn_register);
        mAuth = FirebaseAuth.getInstance();
        textView = findViewById(R.id.loginNow);

        //喺上一個 Activity 撈返個 User Type 返嚟
        // <editor-fold desc="Get User Type">
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
        // </editor-fold>

        setupAgentUI(Type);

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

                if (((RadioGroup) findViewById(R.id.rgUserType)).getCheckedRadioButtonId() == R.id.rbtnAgent) {
                    Type = "Agent";
                    isAllowCreateDH = switchDH.isChecked();
                    isAllowCreateAgent = switchAgent.isChecked();
                }

                if (((RadioGroup) findViewById(R.id.rgUserType)).getCheckedRadioButtonId() == R.id.rbtnTranslator) {
                    Type = "Translator";
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Account Created.",
                                            Toast.LENGTH_SHORT).show();

                                    if (Objects.equals(Type, "Agent")) {
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
                                    } else if (Objects.equals(Type, "Translator")) {
                                        newTranslator newTranslator = new newTranslator(firstName, lastName, email, "");

                                        // 將Agent用戶資料存入 Firestore
                                        db.collection("users").document(email.toLowerCase(Locale.ROOT)).set(newTranslator)
                                                .addOnSuccessListener(aVoid -> {
                                                    mAuth.signOut(); // 登出
                                                    Toast.makeText(Register.this, "A new agent account created successfully", Toast.LENGTH_SHORT).show();
                                                    navigateToLoginPage();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(Register.this, "Error creating agent: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        newUser newUserData = new newUser(Type, firstName, lastName, email, "available", "'");

                                        // 將普通用戶資料存入 Firestore
                                        db.collection("users").document(email.toLowerCase(Locale.ROOT)).set(newUserData)
                                                .addOnSuccessListener(aVoid -> {
                                                    navigateToLoginPage();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(Register.this, "Error creating user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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