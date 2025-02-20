package com.project.fypproject.activities;

import static com.iab.omid.library.giphy.walking.c.b;

import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;
import com.project.fypproject.activities.employer.JobDetailActivity;
import com.project.fypproject.activities.employer.JobDetailAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SalaryRecordSelectorActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    ImageView btnLastYear, btnNextyear;
    LinearLayout[] llMonthButtons = new LinearLayout[12];
    ImageView[] imgStatus = new ImageView[12];
    TextView txtYear;
    int year;
    String numberStr, MyDHEmail;
    String[] Months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private Map<String, Integer> monthStatusMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_salary_record_selector);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        monthStatusMap = new HashMap<>();
        monthStatusMap.put("Jan", R.id.month_status_1);
        monthStatusMap.put("Feb", R.id.month_status_2);
        monthStatusMap.put("Mar", R.id.month_status_3);
        monthStatusMap.put("Apr", R.id.month_status_4);
        monthStatusMap.put("May", R.id.month_status_5);
        monthStatusMap.put("Jun", R.id.month_status_6);
        monthStatusMap.put("Jul", R.id.month_status_7);
        monthStatusMap.put("Aug", R.id.month_status_8);
        monthStatusMap.put("Sep", R.id.month_status_9);
        monthStatusMap.put("Oct", R.id.month_status_10);
        monthStatusMap.put("Nov", R.id.month_status_11);
        monthStatusMap.put("Dec", R.id.month_status_12);

        Bundle b = getIntent().getExtras();
        if(b != null){
            MyDHEmail = b.getString("MyDHEmail"); //Get the user type from last activity
        }


        btnLastYear = findViewById(R.id.btnLastYear);
        btnNextyear = findViewById(R.id.btnNextYear);
        txtYear = findViewById(R.id.year);
        numberStr = txtYear.getText().toString().trim();
        try {
            year = Integer.parseInt(numberStr);
        } catch (NumberFormatException e) {
            year = 2025;
            e.printStackTrace();
        }

        btnNextyear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                year += 1;
                numberStr = Integer.toString(year);
                txtYear.setText(numberStr);
            }
        });

        btnLastYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                year -= 1;
                numberStr = Integer.toString(year);
                txtYear.setText(numberStr);
            }
        });

        for (int i = 0; i < 12; i++) {
            int resID = getResources().getIdentifier("month_" + (i + 1), "id", getPackageName());
            llMonthButtons[i] = findViewById(resID);
            resID = getResources().getIdentifier("month_status_" + (i + 1), "id", getPackageName());
            imgStatus[i] = findViewById(resID);

            llMonthButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), CreateReceiptActivity.class);
                    startActivity(intent);
                }
            });
        }

        // 獲取當前月份（1 ~ 12）
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("receipt")
                .whereEqualTo("employeeEmail", "oliphia@gmail.com")
                .whereEqualTo("employerEmail", user)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String month = document.getString("month");
                            String status = document.getString("status");

                            // 更新對應月份的圖標
                            updateMonthStatusIcon(month, status);
                        }
                    } else {
                        // 處理錯誤
                        task.getException().printStackTrace();
                    }
                });
    }

    // 禁用過去的月份按鈕
//        for (int i = 0; i < currentMonth - 1; i++) {
//            llMonthButtons[i].setEnabled(false);
//        }

    private void updateMonthStatusIcon(String month, String status) {
        if (monthStatusMap.containsKey(month)) {
            // 獲取對應的 ImageView ID
            int imageViewId = monthStatusMap.get(month);
            ImageView imageView = findViewById(imageViewId);

            // 根據狀態設置圖標
            switch (status) {
                case "pending":
                    imageView.setImageResource(R.drawable.icon_horizontal_rule);
                    break;
                case "abc":
//                    imageView.setImageResource(R.drawable.icon_horizontal);
                    break;
                case "done":
                    imageView.setImageResource(R.drawable.baseline_done_24);
                    break;
                default:
//                    imageView.setImageResource(R.drawable.icon_unknown); // 默認圖標
                    break;
            }
        }
    }
}