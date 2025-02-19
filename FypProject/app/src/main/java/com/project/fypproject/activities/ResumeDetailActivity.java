package com.project.fypproject.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;
import com.project.fypproject.activities.employer.JobDetailActivity;

import java.util.Map;

public class ResumeDetailActivity extends AppCompatActivity {

    TextView tvName,tvAge,tvGender,tvMaritalStatus,tvNumOfKids,tvNationality,tvReligion,tvZodiac,tvRemark,tvNoExperience;
    Toolbar toolbar;
    LinearLayout layWorkExp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resume_detail);

        tvName = findViewById(R.id.dhName);
        tvAge = findViewById(R.id.Age);
        tvGender = findViewById(R.id.Gender);
        tvMaritalStatus = findViewById(R.id.MaritalStatus);
        tvNumOfKids = findViewById(R.id.NumOfKids);
        tvNationality = findViewById(R.id.Nationality);
        tvReligion = findViewById(R.id.Religion);
        tvZodiac = findViewById(R.id.Zodiac);
        tvRemark = findViewById(R.id.Remark);
        tvNoExperience = findViewById(R.id.tvNoExperience);
        layWorkExp = findViewById(R.id.layWorkExp);


//        String email = getIntent().getStringExtra("email");


        String email = "abcd@gmail.com";


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("MaidInfo").whereEqualTo("email",email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                tvName.setText(doc.getString("name"));

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
                Map<String, Object> oExperience = (Map<String, Object>) doc.get("overseas_experience");
                if (oExperience == null || oExperience.isEmpty()) {
                    tvNoExperience.setVisibility(View.VISIBLE);
                    tvNoExperience.setText("No Experience");
                } else {
                    boolean hasExperience = false;

                    for (Map.Entry<String, Object> entry : oExperience.entrySet()) {
                        String loc = entry.getKey();
                        Object exp = entry.getValue();
                        if (exp != null) {
                            hasExperience = true;
                            String duration = exp.toString();

                            TextView expTV = new TextView(ResumeDetailActivity.this);
                            expTV.setText(loc.replace("_", " ") + ": " + exp);
                            expTV.setTextSize(16);
                            expTV.setTextColor(getResources().getColor(R.color.black));
                            expTV.setPadding(20, 10, 20, 10);
                            expTV.setTypeface(null, Typeface.BOLD);
                            expTV.setGravity(Gravity.CENTER);

                            layWorkExp.addView(expTV);
                        }
                    }

                    if (!hasExperience) {
                        tvNoExperience.setVisibility(View.VISIBLE);
                        tvNoExperience.setText("No Experience");
                    } else {
                        tvNoExperience.setVisibility(View.GONE);
                    }
                }
//
//                tvExpSalary.setText("Expected Salary:$"+ doc.getString("expectedSalary"));
                tvAge.setText("(" + doc.getString("age")+"years)");
                tvGender.setText(doc.getString("gender"));
                tvMaritalStatus.setText(doc.getString("marital_status"));
                tvNationality.setText(doc.getString("nationality"));
                tvReligion.setText(doc.getString("religion"));
                tvZodiac.setText(doc.getString("zodiac"));
                tvRemark.setText(doc.getString("remark"));

                String sonNoAge = doc.getString("son_no_age");
                String daughterNoAge = doc.getString("daughter_no_age");

                int sonNo = 0;
                int daughterNo = 0;

                if (sonNoAge != null && sonNoAge.contains("/")) {
                    String sonNoStr = sonNoAge.split("/")[0];
                    try {
                        sonNo = Integer.parseInt(sonNoStr);
                    } catch (NumberFormatException e) {
                        sonNo = 0;
                    }
                }

                if (daughterNoAge != null && daughterNoAge.contains("/")) {
                    String daughterNoStr = daughterNoAge.split("/")[0];
                    try {
                        daughterNo = Integer.parseInt(daughterNoStr);
                    } catch (NumberFormatException e) {
                        daughterNo = 0;
                    }
                }

                int totalKids = sonNo + daughterNo;
                tvNumOfKids.setText(String.valueOf(totalKids));

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

            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}