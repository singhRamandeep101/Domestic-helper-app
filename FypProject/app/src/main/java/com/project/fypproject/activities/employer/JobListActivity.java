package com.project.fypproject.activities.employer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import com.project.fypproject.R;
import com.project.fypproject.models.HelperInfo;

public class JobListActivity extends AppCompatActivity {
    FirebaseFirestore firestore;
    RecyclerView recyclerView;
    JobListAdapter jobListAdapter;
    List<HelperInfo> helperInfoList;
    Toolbar toolbar;
    EditText search_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_job_list);

        firestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.rv_job_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        helperInfoList = new ArrayList<>();
        jobListAdapter = new JobListAdapter(this, helperInfoList);
        recyclerView.setAdapter(jobListAdapter);
        AllData();

        search_bar = findViewById(R.id.search_bar);
        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().toUpperCase();
                if(text.isEmpty()){
                    AllData();
                }else{
                    searchData(text);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void AllData() {
        firestore.collection("HelperInfo").whereEqualTo("availability","Available").orderBy("postTime", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            helperInfoList.clear();
                            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                HelperInfo helperInfo = documentSnapshot.toObject(HelperInfo.class);
                                helperInfoList.add(helperInfo);
                            }
                            jobListAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("FireStore", "FireStore get error", task.getException());
                        }
                    }
                });
    }

    private void searchData(String s) {
        firestore.collection("HelperInfo").whereEqualTo("availability","Available").orderBy("nationality").startAt(s).endAt(s + "\uf8ff")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            helperInfoList.clear();
                            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                HelperInfo helperInfo = documentSnapshot.toObject(HelperInfo.class);
                                helperInfoList.add(helperInfo);
                            }
                            jobListAdapter.notifyDataSetChanged();
                        }else{
                            Log.e("FireStore", "FireStore get error", task.getException());
                        }
                    }
                });
    }
}