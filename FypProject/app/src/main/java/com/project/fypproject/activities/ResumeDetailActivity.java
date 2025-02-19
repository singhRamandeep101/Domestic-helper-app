package com.project.fypproject.activities;

import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResumeDetailActivity extends AppCompatActivity {

    TextView tvName,tvAge,tvGender,tvMaritalStatus,tvNumOfKids,tvNationality,tvReligion,tvZodiac,tvRemark,tvNoExperience;
    Toolbar toolbar;
    LinearLayout layWorkExp,layWorkSkill;

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
        layWorkSkill = findViewById(R.id.layWorkSkill);


        String email = getIntent().getStringExtra("email");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("MaidInfo").whereEqualTo("email",email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                tvName.setText(doc.getString("name"));

                Map<String, Object> oExperience = (Map<String, Object>) doc.get("overseas_experience");
                if (oExperience == null || oExperience.isEmpty()) {
                    tvNoExperience.setVisibility(View.VISIBLE);
                    tvNoExperience.setText("No Experience");
                } else {
                    boolean hasExperience = false;

                    for (Map.Entry<String, Object> entry : oExperience.entrySet()) {
                        String loc = entry.getKey().toUpperCase();
                        Object exp = entry.getValue();
                        if (exp != null) {
                            hasExperience = true;
                            String duration = exp.toString();

                            TextView expTV = new TextView(ResumeDetailActivity.this);
                            expTV.setText(loc.replace("_", " ") + ": " + exp);
                            expTV.setTextSize(16);
                            expTV.setTextColor(getResources().getColor(R.color.black));
                            expTV.setPadding(20, 10, 20, 10);
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

                tvAge.setText("(" + doc.getString("age")+"years)");
                String gender = doc.getString("gender");
                if (gender.equals("M")){
                    tvGender.setText("Male");
                }else{
                    tvGender.setText("Female");
                }
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

                Map<String, Object> workingExperience = (Map<String, Object>) doc.get("working_experience");

                if (workingExperience != null && !workingExperience.isEmpty()) {
                    List<String> experienceList = new ArrayList<>();

                    for (Map.Entry<String, Object> entry : workingExperience.entrySet()) {
                        String skill = entry.getKey();
                        Object value = entry.getValue();

                        if (value != null) {
                            String formattedSkill = formatSkillName(skill);
                            experienceList.add(formattedSkill);
                        }
                    }

                    int count = 0;
                    LinearLayout rowLayout = null;

                    for (String experience : experienceList) {
                        if (count % 3 == 0) {
                            rowLayout = new LinearLayout(ResumeDetailActivity.this);
                            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));
                            rowLayout.setPadding(0, 30, 0, 16);
                            layWorkSkill.addView(rowLayout);
                        }

                        TextView skillView = new TextView(ResumeDetailActivity.this);
                        skillView.setText(experience);
                        skillView.setTextSize(16);
                        skillView.setTextColor(getResources().getColor(R.color.black));
                        skillView.setPadding(8, 8, 8, 8);
                        skillView.setBackground(getResources().getDrawable(R.drawable.label_background));
                        skillView.setTypeface(Typeface.DEFAULT_BOLD);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(8, 0, 8, 0);
                        skillView.setLayoutParams(params);

                        if (rowLayout != null) {
                            rowLayout.addView(skillView);
                        }

                        count++;
                    }
                } else {
                    TextView noExperienceView = new TextView(ResumeDetailActivity.this);
                    noExperienceView.setText("No Working Skll");
                    noExperienceView.setTextSize(18);
                    noExperienceView.setTextColor(getResources().getColor(R.color.black));
                    noExperienceView.setTypeface(Typeface.DEFAULT_BOLD);
                    noExperienceView.setGravity(Gravity.CENTER);

                    layWorkSkill.addView(noExperienceView);
                }

                int totalKids = sonNo + daughterNo;
                tvNumOfKids.setText(String.valueOf(totalKids));
                    Map<String, Object> languageSkills = (Map<String, Object>) doc.get("language_skills");
                    if (languageSkills != null) {
                        for (Map.Entry<String, Object> entry : languageSkills.entrySet()) {
                            String language = entry.getKey();
                            String level = formatSkillName(entry.getValue().toString());

                            if (language.equalsIgnoreCase("Mandarin")) {
                                TextView mandarinTextView = findViewById(R.id.MandarinLevel);
                                mandarinTextView.setText(level);
                                updateLanguageBackground(mandarinTextView, level);
                            } else if (language.equalsIgnoreCase("Cantonese")) {
                                TextView cantoneseTextView = findViewById(R.id.CantoneseLevel);
                                cantoneseTextView.setText(level);
                                updateLanguageBackground(cantoneseTextView, level);
                            }else{
                                TextView EnglishTextView = findViewById(R.id.EnglishLevel);
                                EnglishTextView.setText(level);
                                updateLanguageBackground(EnglishTextView, level);
                            }
                        }
                    }
            }

                private void updateLanguageBackground(TextView textView, String level) {
                    if (level.equalsIgnoreCase("Poor")) {
                        textView.setBackground(getResources().getDrawable(R.drawable.language_level_poor));
                    } else if (level.equalsIgnoreCase("Fair")) {
                        textView.setBackground(getResources().getDrawable(R.drawable.language_level_fair));
                    } else{
                        textView.setBackground(getResources().getDrawable(R.drawable.language_level_good));

                    }
                }


                    private String formatSkillName(String skillName) {
                        String[] words = skillName.split("_");
                        StringBuilder formattedName = new StringBuilder();

                        for (String word : words) {
                            if (!word.isEmpty()) {
                                formattedName.append(Character.toUpperCase(word.charAt(0)))
                                        .append(word.substring(1).toLowerCase())
                                        .append(" ");
                            }
                        }

                        return formattedName.toString().trim();
                    }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}