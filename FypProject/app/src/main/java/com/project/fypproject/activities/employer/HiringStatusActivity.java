package com.project.fypproject.activities.employer;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;
import com.project.fypproject.activities.SalaryRecordSelectorActivity;

public class HiringStatusActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    String employerEmail, employeeEmail;
    boolean boolBodyCheck, boolInsurance, boolReady;
    int status;
    ImageView ImgBodyCheck, ImgInsurance, ImgStatus2, ImgStatus3, ImgStatus4, ImgStatus5, ImgBack;
    ImageView[] statusImages;
    Drawable pending, done, current;
    TextView txtStatus2, txtStatus3, txtStatus4;
    TextView[] statusDescription;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hiring_status);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Find the activity elements:
        ImgBodyCheck = findViewById(R.id.LightBodyCheck);
        ImgInsurance = findViewById(R.id.LightInsurance);
        ImgStatus2 = findViewById(R.id.status2);
        ImgStatus3 = findViewById(R.id.status3);
        ImgStatus4 = findViewById(R.id.status4);
        ImgStatus5 = findViewById(R.id.status5);
        ImgBack = findViewById(R.id.btnBack);

        statusImages = new ImageView[]{ ImgStatus2, ImgStatus3, ImgStatus4, ImgStatus5 };

        context = getApplicationContext();

        pending = ContextCompat.getDrawable(context, R.drawable.status_pending);
        done = ContextCompat.getDrawable(context, R.drawable.status_done);
        current = ContextCompat.getDrawable(context, R.drawable.current_status);

        txtStatus2 = findViewById(R.id.txtStatus2);
        txtStatus3 = findViewById(R.id.txtStatus3);
        txtStatus4 = findViewById(R.id.txtStatus4);
        statusDescription = new TextView[]{txtStatus2, txtStatus3, txtStatus4};

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Get the -employer email- and -employee email- from last activity
        Bundle b = getIntent().getExtras();
        if(b != null){
            employeeEmail = b.getString("employeeEmail"); //Get the user type from last activity
            employerEmail = b.getString("employerEmail");
        }

        db.collection("HiringStatus")
                .whereEqualTo("employer", employerEmail)
                .whereEqualTo("domestic_helper", employeeEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                boolReady = document.getBoolean("ready");
                                if(boolReady){
                                    Intent intent = new Intent(getApplicationContext(), SalaryRecordSelectorActivity.class);
                                    Bundle b = new Bundle();
                                    b.putString("employeeEmail", employeeEmail);
                                    b.putString("employerEmail", employerEmail);
                                    intent.putExtras(b);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    boolBodyCheck = document.getBoolean("body_check");
                                    boolInsurance = document.getBoolean("insurance");
                                    status = document.getLong("status").intValue();

                                    if (boolBodyCheck) {
                                        ImgBodyCheck.setImageDrawable(done);
                                    }

                                    if (boolInsurance) {
                                        ImgInsurance.setImageDrawable(done);
                                    }

                                    for (int i = 0; i < statusImages.length; i++) {
                                        if (status > i + 2) {
                                            statusImages[i].setImageDrawable(done);
                                        } else if (status == i + 2) {
                                            statusImages[i].setImageDrawable(current);
                                            if(status <= 4){
                                                statusDescription[i].setAlpha(0f); // 初始為透明
                                                statusDescription[i].setVisibility(View.VISIBLE);
                                                statusDescription[i].animate()
                                                        .alpha(1f)
                                                        .setDuration(600)
                                                        .start();
                                            }
                                        } else {
                                            statusImages[i].setImageDrawable(pending);
                                        }
                                    }
                                }

                            }
                        } else {
                        }
                    }
                });

        ImgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}