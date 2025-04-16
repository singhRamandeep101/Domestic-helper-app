package com.project.fypproject.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.R;
import com.project.fypproject.models.Receipt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ReceiptActivity extends AppCompatActivity {
    private DocumentReference databaseReference;

    TextView txtEmployer, txtDomesticHelper, txtSalary, txtExtra, txtTotleAmount, txtPeriod, txtHolidayTaken, txtSignDate;
    RelativeLayout rlReceiptVoucher;
    Button payButton, importPhotoButton;
    ImageView importedPhoto;

    String month, year;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private Bitmap importedBitmap;
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        importedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        importedPhoto.setImageBitmap(importedBitmap);
                        importedPhoto.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

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
        importPhotoButton = findViewById(R.id.importPhotoButton);
        importedPhoto = findViewById(R.id.importedPhoto);

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

        month = intent.getStringExtra("month");
        year = intent.getStringExtra("year");
        String employeeEmail = intent.getStringExtra("employeeEmail");
        String employerEmail = intent.getStringExtra("employerEmail");
        String userType = intent.getStringExtra("userType");
        String status = intent.getStringExtra("status");
        String documentId = intent.getStringExtra("documentId");

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

        if (Objects.equals(userType, "DomesticHelper")) {
            doneButton.setText("Confirm!");
        }

        if (Objects.equals(status, "confirmed")) {
            doneButton.setVisibility(View.GONE);
            payButton.setVisibility(View.VISIBLE);
        }

        doneButton.setOnClickListener(v -> {
            Log.d("Dennis", receipt.getYear() + " " + receipt.getMonth());

            if (status == null) {
                db.collection("receipt").document().set(receipt);
            } else if (status.equals("pending")) {
                db.collection("receipt").document(documentId)
                        .update("numOfHoliday", receipt.getNumOfHoliday(),
                                "salary", receipt.getSalary(),
                                "bonus", receipt.getBonus(),
                                "fromDate", receipt.getFromDate(),
                                "toDate", receipt.getToDate())
                        .addOnSuccessListener(aVoid -> {
                            if ("DomesticHelper".equals(userType)) {
                                db.collection("receipt").document(documentId)
                                        .update("status", "confirmed")
                                        .addOnSuccessListener(aVoid1 -> {})
                                        .addOnFailureListener(e -> {});
                            }
                            Log.d("Dennis", "Update Successful");
                        })
                        .addOnFailureListener(e -> {});
            }

            Intent backIntent = new Intent(ReceiptActivity.this, SalaryRecordSelectorActivity.class);
            backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(backIntent);
        });

        payButton.setOnClickListener(v -> exportReceiptAsPDF());

        importPhotoButton.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                pickImageLauncher.launch("image/*");
            } else {
                requestStoragePermission();
            }
        });
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageLauncher.launch("image/*");
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void exportReceiptAsPDF() {
        rlReceiptVoucher.setDrawingCacheEnabled(true);
        rlReceiptVoucher.buildDrawingCache();
        Bitmap receiptBitmap = Bitmap.createBitmap(rlReceiptVoucher.getDrawingCache());
        rlReceiptVoucher.setDrawingCacheEnabled(false);

        int totalHeight = receiptBitmap.getHeight();
        int width = receiptBitmap.getWidth();
        if (importedBitmap != null) {
            totalHeight += importedBitmap.getHeight();
            width = Math.max(width, importedBitmap.getWidth());
        }

        PrintAttributes printAttributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();

        PrintedPdfDocument document = new PrintedPdfDocument(this, printAttributes);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, totalHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(receiptBitmap, 0, 0, null);

        if (importedBitmap != null) {
            canvas.drawBitmap(importedBitmap, 0, receiptBitmap.getHeight(), null);
        }

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