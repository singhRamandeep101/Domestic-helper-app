package com.project.fypproject.activities;

import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.project.fypproject.R;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.models.Receipt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {
    private DocumentReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        // Initialize Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String employer = intent.getStringExtra("EMPLOYER_NAME");
        String employee = intent.getStringExtra("EMPLOYEE_NAME");
        String salary = intent.getStringExtra("SALARY");
        String bonus = intent.getStringExtra("BONUS");
        String total = intent.getStringExtra("TOTAL");
        String holidays = intent.getStringExtra("HOLIDAYS");
        String signedBy = intent.getStringExtra("SIGNED_BY");
        String fromDate = intent.getStringExtra("FROM_DATE");
        String toDate = intent.getStringExtra("TO_DATE");

        String month = intent.getStringExtra("month");
        String year = intent.getStringExtra("year");
        String employeeEmail = intent.getStringExtra("employeeEmail");
        String employerEmail = intent.getStringExtra("employerEmail");
        String userType = intent.getStringExtra("userType");

        String receiptContent = "I, " + employee + " received the following salary in cash from " + employer +
                " for the period from (" + fromDate + " to " + toDate + ").\n\n" +
                "Salary amount = " + salary + "\n" +
                "BONUS = " + bonus + "\n" +
                "TOTAL = " + total + "\n\n" +
                "HOLIDAYS TAKEN = " + holidays + "\n\n" +
                "SIGNED BY " + signedBy + "\n" +
                "DATE SIGNED = " + toDate;

        TextView receiptText = findViewById(R.id.receiptText);
        receiptText.setText(receiptContent);

        Button doneButton = findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Store receipt in Firebase
                Receipt receipt = new Receipt(employerEmail,
                        employeeEmail,
                        holidays,
                        salary,
                        bonus,
                        fromDate,
                        toDate,
                        year,
                        month,
                        "pending");

                //create a record into database
                db.collection("receipt").document().set(receipt);

                Intent intent = new Intent(ReceiptActivity.this, SalaryRecordSelectorActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);


            }
        });

        Button payButton = findViewById(R.id.payButton);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportReceiptAsPDF(receiptContent);
            }
        });
    }

    private void exportReceiptAsPDF(String receiptContent) {
        PrintAttributes printAttributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();

        PrintedPdfDocument document = new PrintedPdfDocument(this, printAttributes);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        TextView receiptText = new TextView(this);
        receiptText.setText(receiptContent);
        receiptText.layout(0, 0, 595, 842);
        receiptText.draw(page.getCanvas());

        document.finishPage(page);

        try {
            File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "receipt.pdf");
            document.writeTo(new FileOutputStream(pdfFile));
            Toast.makeText(this, "Receipt exported as PDF to " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to export receipt as PDF", Toast.LENGTH_LONG).show();
        } finally {
            document.close();
        }
    }
}
