package com.project.fypproject.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.project.fypproject.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.models.Receipt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ReceiptActivity extends AppCompatActivity {
    private DocumentReference databaseReference;

    TextView txtEmployer, txtDomesticHelper, txtSalary, txtExtra, txtTotleAmount, txtPeriod, txtHolidayTaken, txtSignDate;
    RelativeLayout rlReceiptVoucher;
    Button payButton;

    String month, year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);


        txtEmployer = findViewById(R.id.employer);
        txtDomesticHelper = findViewById(R.id.domesticHelper);
        txtSalary = findViewById(R.id.Salary);
        txtExtra = findViewById(R.id.Extra);
        txtTotleAmount = findViewById(R.id.TotleAmount);
        txtPeriod = findViewById(R.id.period);
        txtHolidayTaken = findViewById(R.id.holidayTaken);
        txtSignDate = findViewById(R.id.SignDate);

        rlReceiptVoucher = findViewById(R.id.rlReceiptVoucher);
        payButton = findViewById(R.id.payButton);

        // Initialize Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //<editor-fold Get Parameter>
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

        month = intent.getStringExtra("month");
        year = intent.getStringExtra("year");
        String employeeEmail = intent.getStringExtra("employeeEmail");
        String employerEmail = intent.getStringExtra("employerEmail");
        String userType = intent.getStringExtra("userType");
        String status = intent.getStringExtra("status");
        String documentId = intent.getStringExtra("documentId");
        //</editor-fold>


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

        txtEmployer.setText(employer);
        txtDomesticHelper.setText(employee);
        txtSalary.setText(salary);
        txtExtra.setText(bonus);
        txtTotleAmount.setText(total);
        txtPeriod.setText(fromDate + " to " + toDate);
        txtHolidayTaken.setText(holidays + " days");
        txtSignDate.setText(toDate);

        Button doneButton = findViewById(R.id.doneButton);

        if(Objects.equals(userType, "DomesticHelper")){
            doneButton.setText("Confirm!");
        }

        if (Objects.equals(status, "confirmed")){
            doneButton.setVisibility(View.GONE);
            payButton.setVisibility(View.VISIBLE);

        }
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Store receipt in Firebase
                Log.d("Dennis", receipt.getYear() + " " + receipt.getMonth());

                if(status == null){
                    //create a record into database
                    db.collection("receipt").document().set(receipt);

                }else if (status.equals("pending")){
                    db.collection("receipt").document(documentId)
                            .update("numOfHoliday", receipt.getNumOfHoliday(),
                                    "salary", receipt.getSalary(),
                                    "bonus", receipt.getBonus(),
                                    "fromDate", receipt.getFromDate(),
                                    "toDate", receipt.getToDate()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if ("DomesticHelper".equals(userType)){
                                        db.collection("receipt").document(documentId)
                                                .update("status", "confirmed").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                     @Override
                                                     public void onSuccess(Void aVoid) {

                                                     }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                    }
                                                });
                                    }
                                    Log.d("Dennis", "Update Successful");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                }

                Intent intent = new Intent(ReceiptActivity.this, SalaryRecordSelectorActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);


            }
        });

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportReceiptAsPDF();
            }
        });
    }

    private void exportReceiptAsPDF() {
        rlReceiptVoucher.setDrawingCacheEnabled(true);
        rlReceiptVoucher.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(rlReceiptVoucher.getDrawingCache());
        rlReceiptVoucher.setDrawingCacheEnabled(false);

        PrintAttributes printAttributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();

        PrintedPdfDocument document = new PrintedPdfDocument(this, printAttributes);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(bitmap, 0, 0, null);

        document.finishPage(page);

        try {
            File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), year + "_" + month + "_receipt.pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            fos.close();
            Toast.makeText(this, "Receipt exported as PDF to " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to export receipt as PDF", Toast.LENGTH_LONG).show();
        } finally {
            document.close();
        }
    }
}
