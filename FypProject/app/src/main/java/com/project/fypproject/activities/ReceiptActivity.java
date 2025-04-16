package com.project.fypproject.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project.fypproject.R;
import com.project.fypproject.models.Receipt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class ReceiptActivity extends AppCompatActivity {
    private StorageReference storageReference;

    TextView txtEmployer, txtDomesticHelper, txtSalary, txtExtra, txtTotleAmount, txtPeriod, txtHolidayTaken, txtSignDate;
    RelativeLayout rlReceiptVoucher;
    Button payButton, importPhotoButton;
    ImageView importedPhoto;

    String month, year;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private Bitmap importedBitmap;
    private Uri importedImageUri;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    importedImageUri = uri;
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

        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference();

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

        if (documentId != null) {
            loadProofImage(documentId);
        }

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

            // Upload image first if exists
            if (importedImageUri != null) {
                uploadImageToFirebase(importedImageUri, new ImageUploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        // Update receipt with image URL
                        updateReceipt(db, receipt, userType, status, documentId, imageUrl);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ReceiptActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        // Update receipt without image URL
                        updateReceipt(db, receipt, userType, status, documentId, null);
                    }
                });
            } else {
                // Update receipt without image URL
                updateReceipt(db, receipt, userType, status, documentId, null);
            }
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

    private void updateReceipt(FirebaseFirestore db, Receipt receipt, String userType, String status, String documentId, String imageUrl) {
        if (status == null) {
            DocumentReference docRef = db.collection("receipt").document();
            if (imageUrl != null) {
                receipt.setProofImageUrl(imageUrl);
            }
            docRef.set(receipt);
        } else if (status.equals("pending")) {
            DocumentReference docRef = db.collection("receipt").document(documentId);
            docRef.update(
                    "numOfHoliday", receipt.getNumOfHoliday(),
                    "salary", receipt.getSalary(),
                    "bonus", receipt.getBonus(),
                    "fromDate", receipt.getFromDate(),
                    "toDate", receipt.getToDate()
            ).addOnSuccessListener(aVoid -> {
                if ("DomesticHelper".equals(userType)) {
                    // For domestic helper, also update status to confirmed
                    docRef.update("status", "confirmed");
                }
                if (imageUrl != null) {
                    docRef.update("proofImageUrl", imageUrl);
                }
                Log.d("Dennis", "Update Successful");
            }).addOnFailureListener(e -> {
                Log.e("Dennis", "Update Failed", e);
            });
        }

        Intent backIntent = new Intent(ReceiptActivity.this, SalaryRecordSelectorActivity.class);
        backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(backIntent);
    }

    private void loadProofImage(String documentId) {
        FirebaseFirestore.getInstance().collection("receipt")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("proofImageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            loadImageFromUrl(imageUrl);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ReceiptActivity", "Error loading proof image", e);
                });
    }

    private void loadImageFromUrl(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .into(importedPhoto);
        importedPhoto.setVisibility(View.VISIBLE);
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

    private void uploadImageToFirebase(Uri imageUri, ImageUploadCallback callback) {
        if (imageUri != null) {
            String filename = "receipt_proofs/" + UUID.randomUUID().toString() + ".jpg";
            StorageReference fileRef = storageReference.child(filename);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] imageData = baos.toByteArray();

                UploadTask uploadTask = fileRef.putBytes(imageData);
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Toast.makeText(ReceiptActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        callback.onSuccess(imageUrl);
                    }).addOnFailureListener(e -> {
                        callback.onFailure(e);
                    });
                }).addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
            } catch (IOException e) {
                callback.onFailure(e);
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

    interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }
}