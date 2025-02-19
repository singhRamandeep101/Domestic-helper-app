package com.project.fypproject.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateReceiptActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_receipt);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        final EditText employerName = findViewById(R.id.employerName);
        final EditText employeeName = findViewById(R.id.employeeName);
        final EditText holidays = findViewById(R.id.holidays);
        final EditText salary = findViewById(R.id.salary);
        final EditText bonus = findViewById(R.id.bonus);
        final EditText fromDate = findViewById(R.id.fromDate);
        final EditText toDate = findViewById(R.id.toDate);
        final TextView totalSalary = findViewById(R.id.totalSalary);

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

                    // Create a receipt object
                    Map<String, Object> receipt = new HashMap<>();
                    receipt.put("employerName", employerName.getText().toString());
                    receipt.put("employeeName", employeeName.getText().toString());
                    receipt.put("holidays", holidays.getText().toString());
                    receipt.put("salary", salary.getText().toString());
                    receipt.put("bonus", bonus.getText().toString());
                    receipt.put("fromDate", fromDate.getText().toString());
                    receipt.put("toDate", toDate.getText().toString());

                    // Save the receipt to Firestore
                    firestore.collection("receipts")
                            .add(receipt)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(CreateReceiptActivity.this, "Receipt saved successfully!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CreateReceiptActivity.this, "Error saving receipt", Toast.LENGTH_SHORT).show();
                                }
                            });

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
