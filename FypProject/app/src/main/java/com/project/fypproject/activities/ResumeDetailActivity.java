package com.project.fypproject.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;
import com.project.fypproject.activities.employer.JobDetailActivity;
import com.project.fypproject.models.ChatModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ResumeDetailActivity extends AppCompatActivity {

    ImageView imgBack, imgBookmark, imgShare,imgBookInt;
    TextView tvName,tvAge,tvGender,tvMaritalStatus,tvNumOfKids,tvNationality,tvReligion,tvZodiac,tvRemark,tvNoExperience;
    Toolbar toolbar;
    LinearLayout layWorkExp,layWorkSkill;

    Boolean isBookMarked = false;

    Resources res;
    Drawable icon_BookMarked_Outline, icon_BookMarked_Filled;

    FirebaseAuth auth;
    FirebaseUser user;
    String email;
    FirebaseFirestore db;

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

        imgBack = findViewById(R.id.img_back);
        imgBookmark = findViewById(R.id.img_book);
        //imgShare = findViewById(R.id.img_share);
        imgBookInt = findViewById(R.id.btnBookInterview);

        res = getResources();
        icon_BookMarked_Outline = ResourcesCompat.getDrawable(res, R.drawable.icon_bookmarks_write, null);
        icon_BookMarked_Filled = ResourcesCompat.getDrawable(res, R.drawable.icon_bookmarks_filled_write, null);

        email = getIntent().getStringExtra("email");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        imgBookInt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               checkAgent();
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        db.collection("MaidInfo").whereEqualTo("email",email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                //--Book Marks Function--
                //To Check user's marked records
                DocumentReference userRef = db.collection("users").document(user.getEmail());
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userDocument = task.getResult();
                            if (userDocument.exists()) {
                                //Get the string array from firebase as an object
                                ArrayList<String> MarkedRef = (ArrayList<String>)userDocument.get("bookMarks");

                                if(MarkedRef == null){
                                    return;
                                }

                                for (String MarkedId: MarkedRef) {
                                    if(MarkedId.equals(doc.getId())) {
                                        imgBookmark.setImageDrawable(icon_BookMarked_Filled);
                                        isBookMarked = true;
                                        return;
                                    } else {
                                    }
                                }
                            }
                        }
                    }
                });


                tvName.setText(doc.getString("name"));

                Map<String, Object> oExperience = (Map<String, Object>) doc.get("overseas_experience");
                if (oExperience == null || oExperience.isEmpty()) {
                    tvNoExperience.setVisibility(View.VISIBLE);
                    tvNoExperience.setText("No Experience");
                }
                else {
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

                if (sonNoAge != null && !sonNoAge.isEmpty()) {
                    String[] sonParts = sonNoAge.split("/");
                    if (sonParts.length > 0) {
                        try {
                            sonNo = Integer.parseInt(sonParts[0].trim());
                        } catch (NumberFormatException e) {
                            Log.e("Error", "Invalid son number format: " + sonNoAge, e);
                            sonNo = 0;
                        }
                    }
                } else {
                    Log.e("Error", "son_no_age is null or empty");
                }

                if (daughterNoAge != null && !daughterNoAge.isEmpty()) {
                    String[] daughterParts = daughterNoAge.split("/");
                    if (daughterParts.length > 0) {
                        try {
                            daughterNo = Integer.parseInt(daughterParts[0].trim());
                        } catch (NumberFormatException e) {
                            Log.e("Error", "Invalid daughter number format: " + daughterNoAge, e);
                            daughterNo = 0;
                        }
                    }
                } else {
                    Log.e("Error", "daughter_no_age is null or empty");
                }

                Map<String, Object> workingExperience = (Map<String, Object>) doc.get("working_experience");

                if (workingExperience != null && !workingExperience.isEmpty()) {
                    List<String> experienceList = new ArrayList<>();

                    for (Map.Entry<String, Object> entry : workingExperience.entrySet()) {
                        String skill = entry.getKey();
                        Object value = entry.getValue();

                        if (!value.equals(false)) {
                            String formattedSkill = formatSkillName(skill);
                            experienceList.add(formattedSkill);
                        }
                    }

                    int count = 0;
                    LinearLayout rowLayout = null;

                    for (String experience : experienceList) {
                        if (count % 2 == 0) {
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

        imgBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("MaidInfo").whereEqualTo("email",email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                        if(!isBookMarked){
                            db.collection("users").document(user.getEmail()).update(
                                    "bookMarks", FieldValue.arrayUnion(doc.getId())
                            );
                            imgBookmark.setImageDrawable(icon_BookMarked_Filled);
                            isBookMarked = true;
                        } else {
                            db.collection("users").document(user.getEmail()).update(
                                    "bookMarks", FieldValue.arrayRemove(doc.getId())
                            );
                            imgBookmark.setImageDrawable(icon_BookMarked_Outline);
                            isBookMarked = false;
                        }
                    }
                });
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void checkAgent() {

        // Step 1: Query MaidInfo to find the document with the given employeeEmail
        db.collection("MaidInfo")
                .whereEqualTo("email", email) // Search for the email in the collection
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            QuerySnapshot maidInfoDocs = task.getResult();

                            if (!maidInfoDocs.isEmpty()) {
                                // If a document with the given employeeEmail exists
                                DocumentSnapshot maidInfoDoc = maidInfoDocs.getDocuments().get(0);

                                // Check if the document has an agentEmail field
                                String agentEmail = maidInfoDoc.getString("agentEmail");

                                if (agentEmail != null && !agentEmail.isEmpty()) {
                                    // If agentEmail already exists, no further action needed
                                    db.collection("chatrooms")
                                                    .whereArrayContains("userEmails",agentEmail)
                                                            .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                            if(task.isSuccessful() && task.getResult()!=null) {
                                                                                boolean chatRoom = false;
                                                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                                                    List<String> userEmails = (List<String>) document.get("userEmails");
                                                                                    if (userEmails != null && userEmails.contains(user.getEmail()) && userEmails.contains(agentEmail)) {
                                                                                        chatRoom = true;
                                                                                        break;
                                                                                    }
                                                                                }
                                                                                if (chatRoom) {
                                                                                    openChatRoom(agentEmail, "yes");
                                                                                } else {
                                                                                    openChatRoom(agentEmail, "no");
                                                                                }
                                                                            }else{
                                                                                Log.e("message","no found chatroom");
                                                                            }
                                                                        }
                                                                    });
                                } else {
                                    assignAgent(db, maidInfoDoc.getId());
                                }
                            } else {
                                // If no document contains the employeeEmail, log it
                                Log.e("Firestore", "No document found for employeeEmail: " + email);
                            }
                        } else {
                            Log.e("Firestore", "Failed to query MaidInfo collection", task.getException());
                        }
                    }
                });
    }

    // Randomly selects a user with userType 'agent' and assigns their email to the specified MaidInfo document
    private void assignAgent(FirebaseFirestore db, String maidInfoDocId) {
        db.collection("users")
                .whereEqualTo("userType", "Agent") // Query for users with userType = 'agent'
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            QuerySnapshot querySnapshot = task.getResult();

                            if (!querySnapshot.isEmpty()) {
                                // Randomly select one of the agents
                                List<DocumentSnapshot> agents = querySnapshot.getDocuments();
                                DocumentSnapshot randomAgent = agents.get(new Random().nextInt(agents.size()));

                                // Get the agent's email
                                String agentEmail = randomAgent.getString("email");

                                if (agentEmail != null) {
                                    // Step 2: Assign the agent's email to the specified MaidInfo document
                                    db.collection("MaidInfo")
                                            .document(maidInfoDocId)
                                            .update("agentEmail", agentEmail)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    openChatRoom(agentEmail,"no");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("Firestore", "Failed to assign agentEmail", e);
                                                }
                                            });
                                } else {
                                    Log.e("Firestore", "Selected agent does not have an email field");
                                }
                            } else {
                                Log.e("Firestore", "No users with userType = 'agent' found");
                            }
                        } else {
                            Log.e("Firestore", "Failed to query users collection", task.getException());
                        }
                    }
                });
    }
    private void openChatRoom(String agentEmail,String type) {
        // Step 3: Query users collection for the agent's details using agentEmail
        db.collection("users")
                .whereEqualTo("email", agentEmail) // Search for the agent by email
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            QuerySnapshot Docs = task.getResult();

                            if (!Docs.isEmpty()) {
                                // Get the agent document
                                DocumentSnapshot Doc = Docs.getDocuments().get(0);

                                // Retrieve agent's details
                                String firstName = Doc.getString("firstName");
                                String lastName = Doc.getString("lastName");
                                String userType = Doc.getString("userType");

                                if (firstName != null && lastName != null) {
                                    // Step 4: Create and set ChatModel
                                    ChatModel chatModel = new ChatModel();
                                    chatModel.setEmail(agentEmail);
                                    chatModel.setFirstName(firstName);
                                    chatModel.setLastName(lastName);
                                    chatModel.setUserType(userType);

                                        Intent intent = new Intent(ResumeDetailActivity.this, ChatActivity.class);
                                        ChatUtil.passUserIntent(intent, chatModel); // Pass ChatModel via ChatUtil
                                        startActivity(intent); // Start ChatActivity

                                    // Log the information for verification
                                    Log.d("Firestore", "ChatModel set with agent details: " +
                                            "FirstName: " + firstName + ", LastName: " + lastName +
                                            ", AgentEmail: " + agentEmail);

                                    // Optionally, you can save ChatModel to Firestore or proceed to the next action
                                } else {
                                    Log.e("Firestore", "Agent details are incomplete for email: " + email);
                                }
                            } else {
                                Log.e("Firestore", "No document found for agentEmail: " + email);
                            }
                        } else {
                            Log.e("Firestore", "Failed to query users collection", task.getException());
                        }
                    }
                });
    }
}