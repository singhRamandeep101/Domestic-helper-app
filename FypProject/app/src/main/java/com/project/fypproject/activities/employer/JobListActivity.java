package com.project.fypproject.activities.employer;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
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
    FirebaseAuth auth;
    FirebaseUser user;

    private TextView labelNationalityFilipino, labelNationalityThailand, labelNationalityIndonesia, labelNationalityMyanmar, labelNationalitySriLanka;
    private TextView labelZodiacAries, labelZodiacTaurus, labelZodiacGemini, labelZodiacCancer, labelZodiacLeo, labelZodiacVirgo, labelZodiacLibra, labelZodiacScorpio, labelZodiacSagittarius, labelZodiacCapricorn, labelZodiacAquarius, labelZodiacPisces;
    private TextView labelGenderMale, labelGenderFemale;
    private TextView labelEducationJuniorHigh, labelEducationHighSchool;
    private TextView labelLanguageMandarin, labelLanguageCantonese, labelLanguageEnglish;
    private TextView labelWorkingExperienceCareOfBabies,labelWorkingExperienceCareOfToddler,labelWorkingExperienceCareOfChildren,labelWorkingExperienceCareOfElderly,labelWorkingExperienceCareOfDisabled,labelWorkingExperienceCareOfBedridden,labelWorkingExperienceCareOfPet,labelWorkingExperienceHouseholdWorks,labelWorkingExperienceCarWashing,labelWorkingExperienceGardening,labelWorkingExperienceCooking,labelWorkingExperienceDriving;
    private TextView labelAgeUnder30, labelAge30to50, labelAgeOver50;
    private TextView labelOverseasMacau,labelOverseasOther,labelOverseasHomeCountry,labelOverseasHongKong, labelOverseasMalaysia, labelOverseasMiddleEast, labelOverseasSingapore, labelOverseasTaiwan,noResultText;

    private ImageView imgBookmarkFilter;
    private FrameLayout filterLayout;


    private Map<String, List<String>> selectedFilters = new HashMap<>();

    private Button btnApplyFilter,btnFilter,btnClose;
    private boolean isApplyBookmark;

    private TextView lastFilterHistory;

    private List<View> filterLabels = new ArrayList<>();

    private Button btnClearAll;

    Resources res;
    Drawable icon_BookMarked_Outline, icon_BookMarked_Filled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_job_list);

        btnClearAll = findViewById(R.id.btn_clear_all);

        btnClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllFilters();
            }
        });

        lastFilterHistory = findViewById(R.id.last_filter_history);

        imgBookmarkFilter = findViewById(R.id.btn_bookmark_filter);

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

        filterLabels.add(findViewById(R.id.label_nationality_filipino));
        filterLabels.add(findViewById(R.id.label_nationality_thailand));
        filterLabels.add(findViewById(R.id.label_nationality_indonesia));
        filterLabels.add(findViewById(R.id.label_nationality_myanmar));
        filterLabels.add(findViewById(R.id.label_nationality_sri_lanka));

        filterLabels.add(findViewById(R.id.labelZodiacAries));
        filterLabels.add(findViewById(R.id.labelZodiacTaurus));
        filterLabels.add(findViewById(R.id.labelZodiacGemini));
        filterLabels.add(findViewById(R.id.labelZodiacCancer));
        filterLabels.add(findViewById(R.id.labelZodiacLeo));
        filterLabels.add(findViewById(R.id.labelZodiacVirgo));
        filterLabels.add(findViewById(R.id.labelZodiacLibra));
        filterLabels.add(findViewById(R.id.labelZodiacScorpio));
        filterLabels.add(findViewById(R.id.labelZodiacSagittarius));
        filterLabels.add(findViewById(R.id.labelZodiacCapricorn));
        filterLabels.add(findViewById(R.id.labelZodiacAquarius));
        filterLabels.add(findViewById(R.id.labelZodiacPisces));

        filterLabels.add(findViewById(R.id.labelGenderMale));
        filterLabels.add(findViewById(R.id.labelGenderFemale));

        filterLabels.add(findViewById(R.id.labelEducationJuniorHigh));
        filterLabels.add(findViewById(R.id.labelEducationHighSchool));

        filterLabels.add(findViewById(R.id.labelLanguageMandarin));
        filterLabels.add(findViewById(R.id.labelLanguageCantonese));
        filterLabels.add(findViewById(R.id.labelLanguageEnglish));

        filterLabels.add(findViewById(R.id.labelWorkingExperienceCareOfBabies));
        filterLabels.add(findViewById(R.id.labelWorkingExperienceCareOfToddler));
        filterLabels.add(findViewById(R.id.labelWorkingExperienceCareOfChildren));
        filterLabels.add(findViewById(R.id.labelWorkingExperienceCareOfElderly));
        filterLabels.add(findViewById(R.id.labelWorkingExperienceCareOfDisabled));
        filterLabels.add(findViewById(R.id.labelWorkingExperienceCareOfBedridden));
        filterLabels.add(findViewById(R.id.labelWorkingExperienceCareOfPet));
        filterLabels.add(findViewById(R.id.labelWorkingExperienceHouseholdWorks));
        filterLabels.add(findViewById(R.id.labelWorkingExperienceCarWashing));
        filterLabels.add(findViewById(R.id.labelWorkingExperienceGardening));
        filterLabels.add(findViewById(R.id.labelWorkingExperienceCooking));
        filterLabels.add(findViewById(R.id.labelWorkingExperienceDriving));

        filterLabels.add(findViewById(R.id.labelOverseasHongKong));
        filterLabels.add(findViewById(R.id.labelOverseasMalaysia));
        filterLabels.add(findViewById(R.id.labelOverseasMiddleEast));
        filterLabels.add(findViewById(R.id.labelOverseasSingapore));
        filterLabels.add(findViewById(R.id.labelOverseasTaiwan));
        filterLabels.add(findViewById(R.id.labelOverseasMacau));
        filterLabels.add(findViewById(R.id.labelOverseasOther));
        filterLabels.add(findViewById(R.id.labelOverseasHomeCountry));

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

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

        isApplyBookmark = false;

        res = getResources();
        icon_BookMarked_Outline = ResourcesCompat.getDrawable(res, R.drawable.icon_bookmark_outline, null);
        icon_BookMarked_Filled = ResourcesCompat.getDrawable(res, R.drawable.icon_bookmark_filled, null);


        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterLayout.setAlpha(0f);
                filterLayout.setVisibility(View.VISIBLE);
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(filterLayout, "alpha", 0f, 1f);
                fadeIn.setDuration(300);
                fadeIn.start();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(filterLayout, "alpha", 1f, 0f);
                fadeOut.setDuration(300);
                fadeOut.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        filterLayout.setVisibility(View.GONE);
                    }
                });
                fadeOut.start();
            }
        });

        imgBookmarkFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isApplyBookmark){
                    isApplyBookmark = false;
                    imgBookmarkFilter.setImageDrawable(icon_BookMarked_Outline);
                } else{
                    isApplyBookmark = true;
                    imgBookmarkFilter.setImageDrawable(icon_BookMarked_Filled);
                }
            }
        });

        btnApplyFilter = findViewById(R.id.btn_apply_filter);
        btnApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyFilters();
                updateFilterHistory();
                // 加入透明度動畫
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(filterLayout, "alpha", 1f, 0f);
                fadeOut.setDuration(300);
                fadeOut.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        filterLayout.setVisibility(View.GONE);
                    }
                });
                fadeOut.start();
        
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

    private void clearAllFilters() {
        selectedFilters.clear(); // 清空所有已选筛选条件
        resetLabelSelections();  // 重置所有筛选标签的选中状态

        // 更新 RecyclerView 和历史记录显示
        jobListAdapter.notifyDataSetChanged();
        noResultText.setVisibility(View.GONE); // 隐藏无结果文本
        lastFilterHistory.setText("Last applied filters: None"); // 恢复默认的历史记录文本
    }

    private void resetLabelSelections() {
        for (View label : filterLabels) { // filterLabels 是所有篩選項 TextView 的集合
            if (label instanceof TextView) {
                label.setSelected(false);
            }
        }
    }

    private void updateFilterHistory() {
        if (selectedFilters.isEmpty()) {
            lastFilterHistory.setText("Last applied filters: None");
        } else {
            StringBuilder filterText = new StringBuilder("Last applied filters: ");
            for (Map.Entry<String, List<String>> entry : selectedFilters.entrySet()) {
                filterText.append(entry.getKey()).append(": ").append(entry.getValue()).append("; ");
            }
            lastFilterHistory.setText(filterText.toString());
        }
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
        if(isApplyBookmark){
            Log.d("Filter", "Applying bookmark filter");
            DocumentReference userRef = firestore.collection("users").document(user.getEmail());
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot userDocument = task.getResult();
                    if (userDocument != null && userDocument.exists()) {
                        List<String> MarkedRef = (List<String>) userDocument.get("bookMarks");

                        if(MarkedRef == null || MarkedRef.isEmpty()){
                            noResultText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            return;
                        }

                        noResultText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        helperInfoList.clear();

                        // 使用單個查詢獲取所有書籤數據
                        firestore.collection("MaidInfo")
                                .whereIn(FieldPath.documentId(), MarkedRef)
                                .get()
                                .addOnCompleteListener(queryTask -> {
                                    if (queryTask.isSuccessful()) {
                                        helperInfoList.clear();
                                        for (DocumentSnapshot document : queryTask.getResult()) {
                                            HelperInfo helperInfo = document.toObject(HelperInfo.class);
                                            if (helperInfo != null) {
                                                helperInfoList.add(helperInfo);
                                            }
                                        }
                                        jobListAdapter.notifyDataSetChanged();

                                        if (helperInfoList.isEmpty()) {
                                            noResultText.setVisibility(View.VISIBLE);
                                            recyclerView.setVisibility(View.GONE);
                                        }
                                    } else {
                                        Log.e("Firestore", "Error getting bookmarked documents", queryTask.getException());
                                    }
                                });
                    }
                } else {
                    Log.e("Firestore", "Error getting user document", task.getException());
                }
            });
            return;
        }

        Log.d("Filter", "Applying regular filters");
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
                    // 處理將在下面進行
                    break;

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

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                helperInfoList.clear();
                List<DocumentSnapshot> documents = task.getResult().getDocuments();

                if (selectedFilters.containsKey("overseas_experience")) {
                    List<String> overseasFilters = selectedFilters.get("overseas_experience");

                    for (DocumentSnapshot document : documents) {
                        Map<String, Object> data = document.getData();
                        if (data != null) {
                            Map<String, Object> overseasExperience = (Map<String, Object>) data.get("overseas_experience");
                            boolean matchesAll = overseasExperience != null;

                            if (matchesAll) {
                                for (String filter : overseasFilters) {
                                    if (!overseasExperience.containsKey(filter) || overseasExperience.get(filter) == null) {
                                        matchesAll = false;
                                        break;
                                    }
                                }
                            }

                            if (matchesAll) {
                                HelperInfo helperInfo = document.toObject(HelperInfo.class);
                                if (helperInfo != null) {
                                    helperInfoList.add(helperInfo);
                                }
                            }
                        }
                    }
                } else {
                    for (DocumentSnapshot document : documents) {
                        HelperInfo helperInfo = document.toObject(HelperInfo.class);
                        if (helperInfo != null) {
                            helperInfoList.add(helperInfo);
                        }
                    }
                }

                jobListAdapter.notifyDataSetChanged();

                if (helperInfoList.isEmpty()) {
                    noResultText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    noResultText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            } else {
                Log.e("Firestore", "Error getting filtered documents", task.getException());
            }
        });
    }
}