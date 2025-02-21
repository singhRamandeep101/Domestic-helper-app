package com.project.fypproject.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.project.fypproject.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.models.Receipt;

import java.util.Calendar;

public class CreateReceiptActivity extends AppCompatActivity {
    private DocumentReference databaseReference;

    FirebaseAuth auth;
    FirebaseUser user;
    String userType, employeeEmail, employerEmail, month, year, status, documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_receipt);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        EditText employerName = findViewById(R.id.employerName);
         EditText employeeName = findViewById(R.id.employeeName);
         EditText holidays = findViewById(R.id.holidays);
         EditText salary = findViewById(R.id.salary);
         EditText bonus = findViewById(R.id.bonus);
         EditText fromDate = findViewById(R.id.fromDate);
         EditText toDate = findViewById(R.id.toDate);
         TextView totalSalary = findViewById(R.id.totalSalary);

        Bundle b = getIntent().getExtras();
        if(b != null){
            employeeEmail = b.getString("employeeEmail");
            employerEmail = b.getString("employerEmail");
            userType = b.getString("userType");
            month = b.getString("month");
            year =  String.valueOf(b.getInt("year"));
        }

        // Initialize Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Set Employer Name
        DocumentReference docRef = db.collection("users").document(employerEmail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        employerName.setText(document.getString("firstName") +
                                " " +
                                document.getString("lastName"));
                        employerName.setEnabled(false);
                    }
                }
            }
        });

        //Set Employee Name
        docRef = db.collection("users").document(employeeEmail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        employeeName.setText(document.getString("firstName") +
                                " " +
                                document.getString("lastName"));
                        employeeName.setEnabled(false);
                    }
                }
            }
        });

        //Find the record to fill in the input field
        db.collection("receipt")
                .whereEqualTo("employeeEmail", employeeEmail)
                .whereEqualTo("employerEmail", employerEmail)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            holidays.setText(document.getString("numOfHoliday"));
                            salary.setText(document.getString("salary"));
                            bonus.setText(document.getString("bonus"));
                            fromDate.setText(document.getString("fromDate"));
                            toDate.setText(document.getString("toDate"));
                            status = document.getString("status");
                            documentId = document.getId();
                        }
                    } else {
                        // 處理錯誤
                        task.getException().printStackTrace();
                    }
                });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateAndDisplayTotal(salary, bonus, totalSalary);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        salary.addTextChangedListener(textWatcher);
        bonus.addTextChangedListener(textWatcher);

        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(fromDate);
            }
        });

        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(toDate);
            }
        });

        Button previewButton = findViewById(R.id.previewButton);
        previewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput(employerName, employeeName, holidays, salary, bonus, fromDate, toDate)) {
                    String total = calculateTotal(salary.getText().toString(), bonus.getText().toString());
                    Intent intent = new Intent(CreateReceiptActivity.this, ReceiptActivity.class);
                    intent.putExtra("EMPLOYER_NAME", employerName.getText().toString());
                    intent.putExtra("EMPLOYEE_NAME", employeeName.getText().toString());
                    intent.putExtra("HOLIDAYS", holidays.getText().toString());
                    intent.putExtra("SALARY", salary.getText().toString());
                    intent.putExtra("BONUS", bonus.getText().toString());
                    intent.putExtra("TOTAL", total);
                    intent.putExtra("SIGNED_BY", employerName.getText().toString());
                    intent.putExtra("FROM_DATE", fromDate.getText().toString());
                    intent.putExtra("TO_DATE", toDate.getText().toString());

                    intent.putExtra("month", month);
                    intent.putExtra("year", year);
                    intent.putExtra("employeeEmail", employeeEmail);
                    intent.putExtra("employerEmail", employerEmail);
                    intent.putExtra("userType", userType);
                    intent.putExtra("status", status);
                    intent.putExtra("documentId", documentId);

                    startActivity(intent);
                } else {
                    Toast.makeText(CreateReceiptActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInput(EditText... fields) {
        for (EditText field : fields) {
            if (field.getText().toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String calculateTotal(String salary, String bonus) {
        int total = 0;
        if (!salary.isEmpty()) {
            total += Integer.parseInt(salary);
        }
        if (!bonus.isEmpty()) {
            total += Integer.parseInt(bonus);
        }
        return String.valueOf(total);
    }

    private void calculateAndDisplayTotal(EditText salary, EditText bonus, TextView totalSalary) {
        String total = calculateTotal(salary.getText().toString(), bonus.getText().toString());
        totalSalary.setText("Total Salary: " + total);
    }

    private void showDatePickerDialog(final EditText dateEditText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(CreateReceiptActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                dateEditText.setText(selectedDate);
            }
        }, year, month, day);

        datePickerDialog.show();
    }
}
