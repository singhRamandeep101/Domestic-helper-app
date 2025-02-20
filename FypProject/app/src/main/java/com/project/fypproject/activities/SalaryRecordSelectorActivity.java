package com.project.fypproject.activities;

import static com.iab.omid.library.giphy.walking.c.b;

import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;
import com.project.fypproject.activities.employer.JobDetailActivity;
import com.project.fypproject.activities.employer.JobDetailAdapter;

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
        }

        // 獲取當前月份（1 ~ 12）
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

        // 禁用過去的月份按鈕
//        for (int i = 0; i < currentMonth - 1; i++) {
//            llMonthButtons[i].setEnabled(false);
//        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (int i = 0; i < 12; i++) {
            final int i1 = i;
            db.collection("receipts")
                .whereEqualTo("employerEmail", user.getEmail())
                .whereEqualTo("employee", MyDHEmail)
                .whereEqualTo("year", year)
                .whereEqualTo("month", Months[i])
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        if (Objects.equals(doc.getString("status"), "pending")){
                            imgStatus[i].setImageResource(R.drawable.icon_edit_light_primary);
                        } else if (Objects.equals(doc.getString("status"), "confirmed")){
                            imgStatus[i1].setImageResource(R.drawable.baseline_done_24);
                        }
                    }
                });
        }



    }
}