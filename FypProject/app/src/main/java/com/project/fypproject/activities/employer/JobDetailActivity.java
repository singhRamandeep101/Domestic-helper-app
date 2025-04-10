//package com.project.fypproject.activities.employer;
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.GridLayout;
//import android.widget.TextView;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QuerySnapshot;
//
//import java.util.List;
//import java.util.Map;
//import com.project.fypproject.R;
//
//public class JobDetailActivity extends AppCompatActivity {
//    TextView tvUserName,tvTotWorkExpYear,tvWorkExpYear,tvExpSalary,tvAbout,tvNation,
//            tvGender,tvEducation,tvReligion,tvRankAge,tvNoOfBrother,tvNoOfSister,tvPostTime,tvAge,
//            tvDateOfBirth,tvMaritalStatus,tvHeight,tvWeight,tvSonNo,tvDaughtNo,tvConstellation,
//            tvQ1,tvQ2,tvQ3,tvQ4,tvQ5,tvQ6,tvQ7,tvQ7Ans;
//    Toolbar toolbar;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_job_detail);
//
//        tvUserName = findViewById(R.id.tvUserName);
//        tvTotWorkExpYear = findViewById(R.id.tvTotWorkExpYear);
//        tvWorkExpYear = findViewById(R.id.tvWorkExpYear);
//        tvExpSalary = findViewById(R.id.tvExpSalary);
//        tvAbout = findViewById(R.id.tvAbout);
//        tvNation = findViewById(R.id.tvNation);
//        tvGender = findViewById(R.id.tvGender);
//        tvEducation = findViewById(R.id.tvEducation);
//        tvReligion = findViewById(R.id.tvReligion);
//        tvRankAge = findViewById(R.id.tvRankAge);
//        tvNoOfBrother = findViewById(R.id.tvNoOfBrother);
//        tvNoOfSister = findViewById(R.id.tvNoOfSister);
//        tvPostTime = findViewById(R.id.tvPostTime);
//        tvAge = findViewById(R.id.tvAge);
//        tvDateOfBirth = findViewById(R.id.tvDateOfBirth);
//        tvMaritalStatus = findViewById(R.id.tvMaritalStatus);
//        tvHeight = findViewById(R.id.tvHeight);
//        tvWeight = findViewById(R.id.tvWeight);
//        tvSonNo = findViewById(R.id.tvSonNo);
//        tvDaughtNo = findViewById(R.id.tvDaughtNo);
//        tvConstellation = findViewById(R.id.tvConstellation);
//        tvQ1 = findViewById(R.id.tvQ1);
//        tvQ2 = findViewById(R.id.tvQ2);
//        tvQ3 = findViewById(R.id.tvQ3);
//        tvQ4 = findViewById(R.id.tvQ4);
//        tvQ5 = findViewById(R.id.tvQ5);
//        tvQ6 = findViewById(R.id.tvQ6);
//        tvQ7 = findViewById(R.id.tvQ7);
//        tvQ7Ans = findViewById(R.id.tvQ7Ans);
//
//        String userEmail = getIntent().getStringExtra("userEmail");
//
//        RecyclerView recyclerView = findViewById(R.id.rv_previousDuties);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("HelperInfo").whereEqualTo("userEmail",userEmail).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
//
//                List<Map<String,Object>> previousDuties = (List<Map<String,Object>>) doc.get("previousDuties");
//
//                if(previousDuties.isEmpty()){
//                    recyclerView.setVisibility(View.GONE);
//                }else{
//                    JobDetailAdapter jobDetailAdapter = new JobDetailAdapter(JobDetailActivity.this,previousDuties);
//                    recyclerView.setAdapter(jobDetailAdapter);
//                }
//
//                tvUserName.setText(doc.getString("lastName") + " " + doc.getString("firstName"));
//
//                String totalExperience = doc.getString("totalExperienceDuration");
//                String[] tExp = totalExperience.split(" ");
//                String year = tExp[0];
//                String month = tExp[1];
//
//                int years = Integer.parseInt(year.replace("year", ""));
//                int months = Integer.parseInt(month.replace("month", ""));
//
//                if (years > 0 && months > 0) {
//                    tvTotWorkExpYear.setText("Work Experience:" + years + " Year " + months + " Month");
//                } else if (years > 0) {
//                    tvTotWorkExpYear.setText("Work Experience:" + years + " Year");
//                } else if (months > 0) {
//                    tvTotWorkExpYear.setText("Work Experience:" + months + " Month");
//                } else {
//                    tvTotWorkExpYear.setText("No experience");
//                }
//
//                Map<String, Object> oExperience = (Map<String, Object>) doc.get("overseasExperience");
//                if (oExperience.isEmpty()) {
//                    tvWorkExpYear.setVisibility(View.GONE);
//                } else {
//                    String oText = "";
//                    for (Map.Entry<String, Object> entry : oExperience.entrySet()) {
//                        String oTExperience = entry.getValue().toString();
//                        String[] oTExp = oTExperience.split(" ");
//
//                        String oYear = oTExp[0];
//                        String oMonth = oTExp[1];
//
//                        int oYears = Integer.parseInt(oYear.replace("year", "").trim());
//                        int oMonths = Integer.parseInt(oMonth.replace("month", "").trim());
//
//                        if (oYears > 0 && oMonths > 0) {
//                            oText += entry.getKey() + ":" + oYears + " Year " + oMonths + " Month\n";
//                        } else if (oYears > 0) {
//                            oText += entry.getKey() + ":" + oYears + " Year\n";
//                        } else{
//                            oText += entry.getKey() + ":" + oMonths + " Month\n";
//                        }
//                    }
//                    tvWorkExpYear.setText(oText.trim());
//                }
//
//                tvExpSalary.setText("Expected Salary:$"+ doc.getString("expectedSalary"));
//                tvAbout.setText(doc.getString("about"));
//
//                Map<String,Object> strengths = (Map<String, Object>) doc.get("strengths");
//                GridLayout glStrengths =findViewById(R.id.glStrengths);
//                for(Map.Entry<String,Object> entry: strengths.entrySet()) {
//                    if(entry.getValue() instanceof Boolean && (Boolean) entry.getValue()){
//                        TextView textView = new TextView(JobDetailActivity.this);
//                        textView.setText(entry.getKey());
//                        textView.setTextSize(18);
//                        textView.setTextColor(Color.BLACK);
//                        textView.setBackgroundColor(Color.parseColor("#DEA1A1"));
//                        textView.setPadding(30, 30, 30, 30);
//
//                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
//                        params.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
//                        params.setMargins(60, 10, 60, 10);
//                        textView.setLayoutParams(params);
//
//                        glStrengths.addView(textView);
//                    }
//                }
//
//                if((Boolean) strengths.get("OtherStrengths")){
//                    TextView textView = new TextView(JobDetailActivity.this);
//                    textView.setText(doc.getString("OtherText"));
//                    textView.setTextSize(18);
//                    textView.setTextColor(Color.BLACK);
//                    textView.setBackgroundColor(Color.parseColor("#DEA1A1"));
//                    textView.setPadding(30, 30, 30, 30);
//
//                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
//                    params.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
//                    params.setMargins(60, 10, 60, 10);
//                    textView.setLayoutParams(params);
//
//                    glStrengths.addView(textView);
//                }
//
//                Map<String,Object> lSkill = (Map<String, Object>) doc.get("languageSkills");
//                GridLayout gl =findViewById(R.id.glLSkill);
//                for(Map.Entry<String,Object> entry: lSkill.entrySet()){
//                    TextView textView = new TextView(JobDetailActivity.this);
//                    textView.setText(entry.getKey()+": "+entry.getValue().toString());
//                    textView.setTextSize(18);
//                    textView.setTextColor(Color.BLACK);
//                    textView.setBackgroundColor(Color.parseColor("#DEA1A1"));
//                    textView.setPadding(30,30,30,30);
//
//                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
//                    params.setMargins(10,10,10,10);
//                    textView.setLayoutParams(params);
//
//                    gl.addView(textView);
//                }
//                tvNation.setText(doc.getString("nationality"));
//                tvAge.setText(doc.getString("age"));
//                tvGender.setText(doc.getString("gender"));
//                tvDateOfBirth.setText(doc.getString("birthDate"));
//                tvEducation.setText(doc.getString("education"));
//                tvMaritalStatus.setText(doc.getString("maritalStatus"));
//                tvReligion.setText(doc.getString("religion"));
//                tvHeight.setText(doc.getString("height")+"CM");
//                tvRankAge.setText(doc.getString("rankingByAge"));
//                tvWeight.setText(doc.getString("weight")+"KG");
//                tvNoOfBrother.setText(doc.getString("noOfBrothers"));
//                tvNoOfSister.setText(doc.getString("noOfSisters"));
//                tvPostTime.setText(doc.getString("postTime"));
//                tvConstellation.setText(doc.getString("constellation"));
//
//                List<String> sonAges = (List<String>) doc.get("sonAges");
//                if(sonAges.isEmpty()){
//                    tvSonNo.setText(doc.getString("sonNo")+" / 0 ");
//                }else{
//                    String sonAgesS = String.join(", ",sonAges);
//                    tvSonNo.setText(doc.getString("sonNo")+" / " + sonAgesS);
//                }
//
//                List<String> daughtAges = (List<String>) doc.get("daughterAges");
//                if(daughtAges.isEmpty()){
//                    tvDaughtNo.setText(doc.getString("daughterNo")+" / 0");
//                }else{
//                    String daughtAgesS = String.join(", ",daughtAges);
//                    tvDaughtNo.setText(doc.getString("daughterNo")+" / " + daughtAgesS);
//                }
//
//                Map<String,Object> otherQuestion = (Map<String, Object>) doc.get("otherQuestions");
//                tvQ1.setText(otherQuestion.get("Q1.Do you eat pork?").toString());
//                tvQ2.setText(otherQuestion.get("Q2.Accept Day-off not on Sunday?").toString());
//                tvQ3.setText(otherQuestion.get("Q3.Sharing a room with babies / children / elder?").toString());
//                tvQ4.setText(otherQuestion.get("Q4.Are you afraid of dog or cat?").toString());
//                tvQ5.setText(otherQuestion.get("Q5.Do you smoke?").toString());
//                tvQ6.setText(otherQuestion.get("Q6.Do you drink alcohol?").toString());
//                tvQ7.setText(otherQuestion.get("Q7.Have you any prolonged illnesses/undergone surgery?").toString());
//                if(doc.contains("illnessDetails")){
//                    tvQ7Ans.setText("7.Ans: " + doc.getString("illnessDetails"));
//                    tvQ7Ans.setVisibility(View.VISIBLE);
//                }
//
//            }
//        });
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }
//}