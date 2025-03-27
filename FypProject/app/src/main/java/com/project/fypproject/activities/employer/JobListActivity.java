package com.project.fypproject.activities.employer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.project.fypproject.R;
import com.project.fypproject.models.HelperInfo;

public class JobListActivity extends AppCompatActivity {
    FirebaseFirestore firestore;
    RecyclerView recyclerView;
    JobListAdapter jobListAdapter;
    List<HelperInfo> helperInfoList;
    Toolbar toolbar;
    EditText search_bar;

    private TextView labelNationalityFilipino, labelNationalityThailand, labelNationalityIndonesia, labelNationalityMyanmar, labelNationalitySriLanka;
    private TextView labelZodiacAries, labelZodiacTaurus, labelZodiacGemini, labelZodiacCancer, labelZodiacLeo, labelZodiacVirgo, labelZodiacLibra, labelZodiacScorpio, labelZodiacSagittarius, labelZodiacCapricorn, labelZodiacAquarius, labelZodiacPisces;
    private TextView labelGenderMale, labelGenderFemale;
    private TextView labelEducationJuniorHigh, labelEducationHighSchool;
    private TextView labelLanguageMandarin, labelLanguageCantonese, labelLanguageEnglish;
    private TextView labelWorkingExperienceCareOfBabies,labelWorkingExperienceCareOfToddler,labelWorkingExperienceCareOfChildren,labelWorkingExperienceCareOfElderly,labelWorkingExperienceCareOfDisabled,labelWorkingExperienceCareOfBedridden,labelWorkingExperienceCareOfPet,labelWorkingExperienceHouseholdWorks,labelWorkingExperienceCarWashing,labelWorkingExperienceGardening,labelWorkingExperienceCooking,labelWorkingExperienceDriving;
    private TextView labelAgeUnder30, labelAge30to50, labelAgeOver50;
    private TextView labelOverseasMacau,labelOverseasOther,labelOverseasHomeCountry,labelOverseasHongKong, labelOverseasMalaysia, labelOverseasMiddleEast, labelOverseasSingapore, labelOverseasTaiwan,noResultText;

    private FrameLayout filterLayout;


    private Map<String, List<String>> selectedFilters = new HashMap<>();

    private Button btnApplyFilter,btnFilter,btnClose;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_job_list);

        labelNationalityFilipino = findViewById(R.id.label_nationality_filipino);
        labelNationalityThailand = findViewById(R.id.label_nationality_thailand);
        labelNationalityIndonesia = findViewById(R.id.label_nationality_indonesia);
        labelNationalityMyanmar = findViewById(R.id.label_nationality_myanmar);
        labelNationalitySriLanka = findViewById(R.id.label_nationality_sri_lanka);
        labelZodiacAries = findViewById(R.id.labelZodiacAries);
        labelZodiacTaurus = findViewById(R.id.labelZodiacTaurus);
        labelZodiacGemini = findViewById(R.id.labelZodiacGemini);
        labelZodiacCancer = findViewById(R.id.labelZodiacCancer);
        labelZodiacLeo = findViewById(R.id.labelZodiacLeo);
        labelZodiacVirgo = findViewById(R.id.labelZodiacVirgo);
        labelZodiacLibra = findViewById(R.id.labelZodiacLibra);
        labelZodiacScorpio = findViewById(R.id.labelZodiacScorpio);
        labelZodiacSagittarius = findViewById(R.id.labelZodiacSagittarius);
        labelZodiacCapricorn = findViewById(R.id.labelZodiacCapricorn);
        labelZodiacAquarius = findViewById(R.id.labelZodiacAquarius);
        labelZodiacPisces = findViewById(R.id.labelZodiacPisces);
        labelGenderMale = findViewById(R.id.labelGenderMale);
        labelGenderFemale = findViewById(R.id.labelGenderFemale);
        labelEducationJuniorHigh = findViewById(R.id.labelEducationJuniorHigh);
        labelEducationHighSchool = findViewById(R.id.labelEducationHighSchool);
        labelLanguageMandarin = findViewById(R.id.labelLanguageMandarin);
        labelLanguageCantonese = findViewById(R.id.labelLanguageCantonese);
        labelLanguageEnglish = findViewById(R.id.labelLanguageEnglish);
        labelWorkingExperienceCareOfBabies= findViewById(R.id.labelWorkingExperienceCareOfBabies);
        labelWorkingExperienceCareOfToddler= findViewById(R.id.labelWorkingExperienceCareOfToddler);
        labelWorkingExperienceCareOfChildren= findViewById(R.id.labelWorkingExperienceCareOfChildren);
        labelWorkingExperienceCareOfElderly= findViewById(R.id.labelWorkingExperienceCareOfElderly);
        labelWorkingExperienceCareOfDisabled= findViewById(R.id.labelWorkingExperienceCareOfDisabled);
        labelWorkingExperienceCareOfBedridden= findViewById(R.id.labelWorkingExperienceCareOfBedridden);
        labelWorkingExperienceCareOfPet= findViewById(R.id.labelWorkingExperienceCareOfPet);
        labelWorkingExperienceHouseholdWorks= findViewById(R.id.labelWorkingExperienceHouseholdWorks);
        labelWorkingExperienceCarWashing= findViewById(R.id.labelWorkingExperienceCarWashing);
        labelWorkingExperienceGardening= findViewById(R.id. labelWorkingExperienceGardening);
        labelWorkingExperienceCooking= findViewById(R.id.labelWorkingExperienceCooking);
        labelWorkingExperienceDriving= findViewById(R.id.labelWorkingExperienceDriving);
//        labelAgeUnder30 = findViewById(R.id.labelAgeUnder30);
//        labelAge30to50 = findViewById(R.id.labelAge30to50);
//        labelAgeOver50 = findViewById(R.id.labelAgeOver50);
        labelOverseasHongKong = findViewById(R.id.labelOverseasHongKong);
        labelOverseasMalaysia = findViewById(R.id.labelOverseasMalaysia);
        labelOverseasMiddleEast = findViewById(R.id.labelOverseasMiddleEast);
        labelOverseasSingapore = findViewById(R.id.labelOverseasSingapore);
        labelOverseasTaiwan = findViewById(R.id.labelOverseasTaiwan);
        labelOverseasMacau = findViewById(R.id.labelOverseasMacau);
        labelOverseasOther = findViewById(R.id.labelOverseasOther);
        labelOverseasHomeCountry = findViewById(R.id.labelOverseasHomeCountry);


        noResultText = findViewById(R.id.no_result_text);

        firestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.rv_job_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        helperInfoList = new ArrayList<>();
        jobListAdapter = new JobListAdapter(this, helperInfoList);
        recyclerView.setAdapter(jobListAdapter);
        AllData();

        btnFilter = findViewById(R.id.btn_filter);
        filterLayout = findViewById(R.id.filter_layout_container);
        btnClose = findViewById(R.id.btn_close_filter);

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterLayout.setVisibility(View.VISIBLE);
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterLayout.setVisibility(View.GONE);
            }
        });



        btnApplyFilter = findViewById(R.id.btn_apply_filter);
        btnApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyFilters();
                filterLayout.setVisibility(View.GONE);
            }
        });



        setupLabelClick(labelNationalityFilipino, "nationality", "FILIPINO");
        setupLabelClick(labelNationalityThailand, "nationality", "THAILAND");
        setupLabelClick(labelNationalityIndonesia, "nationality", "INDONESIA");
        setupLabelClick(labelNationalityMyanmar, "nationality", "MYANMAR");
        setupLabelClick(labelNationalitySriLanka, "nationality", "SRI LANKA");

        setupLabelClick(labelZodiacAries, "zodiac", "ARIES");
        setupLabelClick(labelZodiacTaurus, "zodiac", "TAURUS");
        setupLabelClick(labelZodiacGemini, "zodiac", "GEMINI");
        setupLabelClick(labelZodiacCancer, "zodiac", "CANCER");
        setupLabelClick(labelZodiacLeo, "zodiac", "LEO");
        setupLabelClick(labelZodiacVirgo, "zodiac", "VIRGO");
        setupLabelClick(labelZodiacLibra, "zodiac", "LIBRA");
        setupLabelClick(labelZodiacScorpio, "zodiac", "SCORPIO");
        setupLabelClick(labelZodiacSagittarius, "zodiac", "SAGITTARIUS");
        setupLabelClick(labelZodiacCapricorn, "zodiac", "CAPRICORN");
        setupLabelClick(labelZodiacAquarius, "zodiac", "AQUARIUS");
        setupLabelClick(labelZodiacPisces, "zodiac", "PISCES");

        setupLabelClick(labelGenderMale, "gender", "M");
        setupLabelClick(labelGenderFemale, "gender", "F");

        setupLabelClick(labelEducationJuniorHigh, "education", "JUNIORHIGH");
        setupLabelClick(labelEducationHighSchool, "education", "HIGHSCHOOL");

        setupLabelClick(labelLanguageMandarin, "language_skills", "Mandarin");
        setupLabelClick(labelLanguageCantonese, "language_skills", "Cantonese");
        setupLabelClick(labelLanguageEnglish, "language_skills", "English");

        setupLabelClick(labelWorkingExperienceCareOfBabies, "working_experience", "Care of Babies");
        setupLabelClick(labelWorkingExperienceCareOfToddler, "working_experience", "Care of Toddler");
        setupLabelClick(labelWorkingExperienceCareOfChildren, "working_experience", "Care of Children");
        setupLabelClick(labelWorkingExperienceCareOfElderly, "working_experience", "Care of Elderly");
        setupLabelClick(labelWorkingExperienceCareOfDisabled, "working_experience", "Care of Disabled");
        setupLabelClick(labelWorkingExperienceCareOfBedridden, "working_experience", "Care of Bedridden");
        setupLabelClick(labelWorkingExperienceCareOfPet, "working_experience", "Care of Pet");
        setupLabelClick(labelWorkingExperienceHouseholdWorks, "working_experience", "Household Works");
        setupLabelClick(labelWorkingExperienceCarWashing, "working_experience", "Car Washing");
        setupLabelClick(labelWorkingExperienceGardening, "working_experience", "Gardening");
        setupLabelClick(labelWorkingExperienceCooking, "working_experience", "Cooking");
        setupLabelClick(labelWorkingExperienceDriving, "working_experience", "Driving");


        setupLabelClick(labelAgeUnder30, "age", "Under 30");
        setupLabelClick(labelAge30to50, "age", "30 - 50");
        setupLabelClick(labelAgeOver50, "age", "Over 50");

        setupLabelClick(labelOverseasHongKong, "overseas_experience", "Hong Kong");
        setupLabelClick(labelOverseasMalaysia, "overseas_experience", "Malaysia");
        setupLabelClick(labelOverseasMiddleEast, "overseas_experience", "Middle East");
        setupLabelClick(labelOverseasSingapore, "overseas_experience", "Singapore");
        setupLabelClick(labelOverseasTaiwan, "overseas_experience", "Taiwan");
        setupLabelClick(labelOverseasMacau, "overseas_experience", "Macau");
        setupLabelClick(labelOverseasOther, "overseas_experience", "Other");
        setupLabelClick(labelOverseasHomeCountry, "overseas_experience", "Home Country");



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void AllData() {
        firestore.collection("MaidInfo").whereEqualTo("availability","Available")
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
                            Log.e("FireStore", "FireStore get error ", task.getException());
                        }
                    }
                });
    }

    private void setupLabelClick(TextView label, String key, String value) {
        if (label != null) {
            label.setOnClickListener(v -> {
                if (label.isSelected()) {

                    label.setSelected(false);
                    List<String> values = selectedFilters.get(key);
                    if (values != null) {
                        values.remove(value);
                        if (values.isEmpty()) {
                            selectedFilters.remove(key);
                        } else {
                            selectedFilters.put(key, values);
                        }
                    }
                } else {

                    label.setSelected(true);
                    List<String> values = selectedFilters.getOrDefault(key, new ArrayList<>());
                    if (!values.contains(value)) {
                        values.add(value);
                    }
                    selectedFilters.put(key, values);
                }
            });
        }
    }

    private void applyFilters() {
        Query query = firestore.collection("MaidInfo");

        for (Map.Entry<String, List<String>> filter : selectedFilters.entrySet()) {
            String key = filter.getKey();
            List<String> values = filter.getValue();

            switch (key) {
                case "language_skills":
                    for (String value : values) {
                        query = query.whereEqualTo("language_skills." + value, "Good");
                    }
                    break;

                case "working_experience":
                    for (String value : values) {
                        query = query.whereEqualTo("working_experience." + value, true);
                    }
                    break;

                case "overseas_experience":

                    continue;

                case "age":
                    for (String value : values) {
                        if (value.equals("Under 30")) {
                            query = query.whereLessThan("age", "30");
                        } else if (value.equals("30 - 50")) {
                            query = query.whereGreaterThanOrEqualTo("age", "30").whereLessThanOrEqualTo("age", "50");
                        } else if (value.equals("Over 50")) {
                            query = query.whereGreaterThan("age", "50");
                        }
                    }
                    break;

                default:
                    for (String value : values) {
                        query = query.whereEqualTo(key, value);
                    }
                    break;
            }
        }


        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            helperInfoList.clear();
            if (queryDocumentSnapshots.isEmpty()) {
                noResultText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                noResultText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    Map<String, Object> data = document.getData();


                    if (selectedFilters.containsKey("overseas_experience")) {
                        boolean matchesAll = true;
                        List<String> overseasFilters = selectedFilters.get("overseas_experience");
                        Map<String, Object> overseasExperience = (Map<String, Object>) data.get("overseas_experience");

                        if (overseasExperience != null) {
                            for (String filter : overseasFilters) {

                                if (!overseasExperience.containsKey(filter) || overseasExperience.get(filter) == null) {
                                    matchesAll = false;
                                    noResultText.setVisibility(View.VISIBLE);
                                    break;
                                }
                            }
                        } else {
                            matchesAll = false;
                            noResultText.setVisibility(View.VISIBLE);
                        }


                        if (!matchesAll) {
                            continue;
                        }
                    }


                    HelperInfo helperInfo = new HelperInfo(
                            (String) data.get("name"),
                            (String) data.get("nationality"),
                            (String) data.get("zodiac"),
                            (String) data.get("img_url"),
                            (String) data.get("email"),
                            (String) data.get("age")
                    );

                    helperInfoList.add(helperInfo);
                }

                jobListAdapter.notifyDataSetChanged();
            }
        });
    }
}