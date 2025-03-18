package com.project.fypproject.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookRecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_record);

        String employerEmail = getIntent().getStringExtra("employerEmail");

        RecyclerView recyclerView = findViewById(R.id.rv_bookRecord);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Map<String, Object>> records = new ArrayList<>();

        db.collection("bookings")
                .whereEqualTo("employeeEmail", "a") // 替換為您的查詢條件
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // 遍歷 HelperInfo 集合中的數據
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                Map<String, Object> record = document.getData();

                                // 獲取 employeeEmail
                                String employeeEmail = (String) record.get("employeeEmail");

                                if (employeeEmail != null) {
                                    // 查詢 MaidInfo 集合以獲取對應的員工名稱
                                    db.collection("MaidInfo")
                                            .whereEqualTo("email", employeeEmail)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot maidSnapshots) {
                                                    if (!maidSnapshots.isEmpty()) {
                                                        // 假設每個 email 只對應一名員工
                                                        DocumentSnapshot maidDocument = maidSnapshots.getDocuments().get(0);
                                                        String employeeName = (String) maidDocument.get("name");

                                                        // 將員工名稱添加到 jobDetail
                                                        record.put("employeeName", employeeName);
                                                    } else {
                                                        // 如果找不到對應的員工名稱，設置為 "Unknown"
                                                        record.put("employeeName", "Unknown");
                                                    }

                                                    // 最後將 jobDetail 添加到 previousDuties 列表
                                                    records.add(record);

                                                    // 如果所有數據加載完成，創建適配器
                                                    if (records.size() == queryDocumentSnapshots.size()) {
                                                        BookRecordlAdapter bookRecordlAdapter = new BookRecordlAdapter(BookRecordActivity.this, records);
                                                        recyclerView.setAdapter(bookRecordlAdapter);
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // MaidInfo 查詢失敗處理
                                                    Log.e("Firestore", "Error fetching MaidInfo", e);
                                                }
                                            });
                                } else {
                                    // 如果 employeeEmail 為空，直接添加到列表
                                    record.put("employeeName", "Unknown");
                                    records.add(record);
                                }
                            }
                        } else {
                            // 如果沒有匹配的文檔
                            Log.d("Firestore", "No booking found.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // HelperInfo 查詢失敗處理
                        Log.e("Firestore", "Error fetching MaidInfo", e);
                    }
                });
    }
}