package com.project.fypproject.activities.employer;

import com.project.fypproject.R;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class OldReceiptsActivity extends AppCompatActivity {

    private Spinner monthSpinner;
    private Button retrieveButton;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewoldreceipts);

        monthSpinner = findViewById(R.id.monthSpinner);
        retrieveButton = findViewById(R.id.retrieveButton);
        firestore = FirebaseFirestore.getInstance();

        List<String> months = new ArrayList<>();
        months.add("January");
        months.add("February");
        months.add("March");
        months.add("April");
        months.add("May");
        months.add("June");
        months.add("July");
        months.add("August");
        months.add("September");
        months.add("October");
        months.add("November");
        months.add("December");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);

        retrieveButton.setOnClickListener(v -> {
            int selectedMonth = monthSpinner.getSelectedItemPosition() + 1;
            getReceiptsForMonth(selectedMonth);
        });
    }

    private void getReceiptsForMonth(int month) {
        firestore.collection("receipts")
                .whereEqualTo("month", month)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder receiptInfo = new StringBuilder();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String receiptData = document.getString("data"); // Adjust key as needed
                        receiptInfo.append(receiptData).append("\n");
                    }

                    if (receiptInfo.length() == 0) {
                        receiptInfo.append("No receipts found for the selected month.");
                    }

                    showReceiptsDialog(receiptInfo.toString());
                })
                .addOnFailureListener(e -> {
                    showErrorDialog("Error retrieving receipts: " + e.getMessage());
                });
    }

    private void showReceiptsDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Receipts for Selected Month")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}