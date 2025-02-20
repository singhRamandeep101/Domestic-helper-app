package com.project.fypproject.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.project.fypproject.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Pdfbox extends AppCompatActivity {

    private static final String TAG = "Pdfbox";


    private ImageView pdfPreview;
    private Button btnUpload,btnSubmit,btnDelete;
    private LinearLayout jsonFormContainer;
    private EditText emailInput;
    private TextView pdfFileName;
    private String selectedPdfName;
    EditText name_input;

    String extractedName;

    private byte[] selectedPdfData;
    private String formattedJson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfbox);


        pdfPreview = findViewById(R.id.pdf_preview);
        btnUpload = findViewById(R.id.upload_button);
        btnSubmit = findViewById(R.id.submit_button);
        btnDelete = findViewById(R.id.delete_button);
        jsonFormContainer = findViewById(R.id.json_form_container);
        emailInput = findViewById(R.id.email_input);
        pdfFileName = findViewById(R.id.pdf_file_name);
        name_input = findViewById(R.id.name_input);

        btnDelete.setVisibility(View.GONE);


        btnUpload.setOnClickListener(view -> openFilePicker());


        btnSubmit.setOnClickListener(view -> {
            if (jsonFormContainer.getChildCount() > 0) {
                Map<String, Object> updatedJsonMap = collectJsonData(jsonFormContainer);


                String email = emailInput.getText().toString().trim();
                if (!email.isEmpty()) {
                    updatedJsonMap.put("email", email);
                }

                Map<String, Object> formattedData = formatDataForFirestore(updatedJsonMap);

                uploadToFirestore(formattedData);
            } else {
                Toast.makeText(Pdfbox.this, "No data to submit!", Toast.LENGTH_SHORT).show();
            }
        });
        btnDelete.setOnClickListener(view -> deletePdf("Delete"));
    }

    private Map<String, Object> collectJsonData(LinearLayout container) {
        Map<String, Object> result = new HashMap<>();


        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);

            if (view instanceof LinearLayout) {

                String tag = (String) view.getTag();
                if (tag != null && tag.equalsIgnoreCase("language_skills")) {
                    HashMap<String, String> languageSkills = new HashMap<>();
                    LinearLayout languageContainer = (LinearLayout) view;

                    for (int j = 0; j < languageContainer.getChildCount(); j++) {
                        View langRow = languageContainer.getChildAt(j);
                        if (langRow instanceof LinearLayout) {
                            LinearLayout langRowLayout = (LinearLayout) langRow;


                            EditText langInput = (EditText) langRowLayout.getChildAt(0);
                            String language = langInput.getText().toString().trim();


                            Spinner proficiencySpinner = (Spinner) langRowLayout.getChildAt(1);
                            String proficiency = proficiencySpinner.getSelectedItem().toString();


                            if (!language.isEmpty() && !proficiency.isEmpty()) {
                                languageSkills.put(language, proficiency);
                            }
                        }
                    }

                    result.put("language_skills", languageSkills);
                } else {

                    Map<String, Object> nestedData = collectJsonData((LinearLayout) view);
                    result.putAll(nestedData);
                }
            } else if (view instanceof EditText) {

                String key = (String) view.getTag();
                if (key != null) {
                    String value = ((EditText) view).getText().toString().trim();
                    result.put(key, value.isEmpty() ? null : value);
                }
            } else if (view instanceof Spinner) {

                String key = (String) view.getTag();
                if (key != null) {
                    String value = ((Spinner) view).getSelectedItem().toString();
                    result.put(key, value.isEmpty() ? null : value);
                }
            }
        }

        return result;
    }

    private void displayJsonForm(Map<String, Object> jsonMap, LinearLayout container) {
        emailInput.setVisibility(View.VISIBLE);
        name_input.setText(extractedName);
        name_input.setVisibility(View.VISIBLE);
        container.removeAllViews();

        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();


            if (value == null) {
                value = "";
            }


            if (key.equalsIgnoreCase("language_skills") && value instanceof List) {
                List<Map<String, Object>> languageSkillsList = (List<Map<String, Object>>) value;


                TextView sectionHeader = new TextView(this);
                sectionHeader.setText("Language Skills:");
                sectionHeader.setTextSize(16);
                sectionHeader.setPadding(0, 10, 0, 10);
                container.addView(sectionHeader);


                LinearLayout skillsContainer = new LinearLayout(this);
                skillsContainer.setOrientation(LinearLayout.VERTICAL);
                skillsContainer.setTag("language_skills");
                container.addView(skillsContainer);


                for (Map<String, Object> skillEntry : languageSkillsList) {
                    for (Map.Entry<String, Object> entryd : skillEntry.entrySet()) {
                        String proficiency = entryd.getKey();
                        String language = entryd.getValue().toString();

                        if (!language.isEmpty() && !proficiency.isEmpty()) {
                            addLanguageSkillRow(skillsContainer, language, proficiency);
                        }
                    }
                }
                continue;
            }


            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 10, 0, 10);


            TextView keyView = new TextView(this);
            keyView.setText(key + ": ");
            keyView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            row.addView(keyView);


            if (key.equalsIgnoreCase("Nationality")) {
                createSpinner(row, key, new String[]{"", "FILIPINO", "THAILAND", "INDONESIA", "MYANMAR", "SRI LANKA"}, value);
            } else if (key.equalsIgnoreCase("Gender")) {
                createSpinner(row, key, new String[]{"", "M", "F"}, value);
            } else if (key.equalsIgnoreCase("Education")) {
                createSpinner(row, key, new String[]{"", "JUNIOR HIGH", "HIGH SCHOOL"}, value);
            } else if (key.equalsIgnoreCase("Zodiac")) {
                createSpinner(row, key, new String[]{"", "ARIES", "TAURUS", "GEMINI", "CANCER", "LEO", "VIRGO", "LIBRA",
                        "SCORPIO", "SAGITTARIUS", "CAPRICORN", "AQUARIUS", "PISCES"}, value);
            } else if (key.equalsIgnoreCase("care_of_babies") || key.equalsIgnoreCase("care_of_toddler") ||
                    key.equalsIgnoreCase("care_of_children") || key.equalsIgnoreCase("care_of_elderly") ||
                    key.equalsIgnoreCase("care_of_disabled") || key.equalsIgnoreCase("care_of_bedridden") ||
                    key.equalsIgnoreCase("care_of_pet") || key.equalsIgnoreCase("household_works") ||
                    key.equalsIgnoreCase("car_washing") || key.equalsIgnoreCase("gardening") ||
                    key.equalsIgnoreCase("cooking") || key.equalsIgnoreCase("driving")) {
                createSpinner(row, key, new String[]{"", "true", "false"}, value);
            } else if (value instanceof String) {

                EditText valueView = new EditText(this);
                valueView.setText(value.toString());
                valueView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
                valueView.setTag(key);
                row.addView(valueView);
            } else if (value instanceof Map) {

                TextView sectionHeader = new TextView(this);
                sectionHeader.setText(key + ":");
                sectionHeader.setTextSize(16);
                sectionHeader.setPadding(0, 10, 0, 10);
                container.addView(sectionHeader);

                LinearLayout nestedContainer = new LinearLayout(this);
                nestedContainer.setOrientation(LinearLayout.VERTICAL);
                container.addView(nestedContainer);

                displayJsonForm((Map<String, Object>) value, nestedContainer);
            } else {

                TextView valueView = new TextView(this);
                valueView.setText(value != null ? value.toString() : "N/A");
                valueView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
                row.addView(valueView);
            }

            container.addView(row);
        }
    }

    private void addLanguageSkillRow(LinearLayout container, String language, String proficiency) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 10, 0, 10);


        EditText langInput = new EditText(this);
        langInput.setHint("Language");
        langInput.setText(language);
        langInput.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(langInput);


        Spinner proficiencySpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"poor", "fair", "good"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        proficiencySpinner.setAdapter(adapter);


        int position = adapter.getPosition(proficiency);
        proficiencySpinner.setSelection(position >= 0 ? position : 0);

        proficiencySpinner.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(proficiencySpinner);



        container.addView(row);
    }

    private void createSpinner(LinearLayout row, String key, String[] options, Object currentValue) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        if (currentValue != null) {
            int position = adapter.getPosition(currentValue.toString());
            spinner.setSelection(position >= 0 ? position : 0);
        }

        spinner.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
        spinner.setTag(key);
        row.addView(spinner);
    }


    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri pdfUri = data.getData();
            if (pdfUri != null) {
                try {

                    String fileName = getFileName(pdfUri);
                    if (fileName != null) {

                       extractedName = extractNameFromFile(fileName);
//                        selectedPdfName = extractedName;
//                        Log.d(TAG, "Extracted file name: " + extractedName);
                       pdfFileName.setText(fileName);
                    }

                    selectedPdfData = loadPdfFile(pdfUri);
                    displayPdf(pdfUri);
                    sendPdfToFormXAI(selectedPdfData);

                    btnUpload.setVisibility(View.GONE);
                    btnDelete.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    Log.e(TAG, "Error loading PDF file: " + e.getMessage());
                    Toast.makeText(this, "Error loading PDF file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {

            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    private String extractNameFromFile(String fileName) {

        if (fileName.toLowerCase().endsWith(".pdf")) {
            fileName = fileName.substring(0, fileName.lastIndexOf(".pdf"));
        }


        String[] parts = fileName.trim().split(" ");
        if (parts.length > 2) {

            StringBuilder extractedName = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                extractedName.append(parts[i]);
                if (i < parts.length - 1) {
                    extractedName.append(" ");
                }
            }
            return extractedName.toString().trim();
        }


        return fileName;
    }


    private byte[] loadPdfFile(Uri pdfUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(pdfUri);
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        inputStream.close();
        return buffer;
    }


    private void displayPdf(Uri pdfUri) {
        try {
            ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(pdfUri, "r");
            if (fileDescriptor != null) {
                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
                PdfRenderer.Page page = pdfRenderer.openPage(0);

                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                pdfPreview.setImageBitmap(bitmap);

                page.close();
                pdfRenderer.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error displaying PDF: " + e.getMessage());
        }
    }

    private void deletePdf(String type) {
        selectedPdfData = null;
        formattedJson = null;

        pdfPreview.setImageBitmap(null);
        jsonFormContainer.removeAllViews();
        pdfFileName.setText("");
        emailInput.setText("");
        name_input.setText("");
        emailInput.setVisibility(View.GONE);
        name_input.setVisibility(View.GONE);

        btnUpload.setVisibility(View.VISIBLE);
        btnDelete.setVisibility(View.GONE);
        if(type.equals("Submit")){
            Toast.makeText(Pdfbox.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
        }else if(type.equals("Delete")) {
            Toast.makeText(this, "PDF deleted successfully!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(Pdfbox.this, "Please Upload Again!", Toast.LENGTH_SHORT).show();
        }
    }


    private void sendPdfToFormXAI(byte[] pdfData) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();


                RequestBody requestBody = RequestBody.create(pdfData, MediaType.parse("application/pdf"));


                Request request = new Request.Builder()
                        .url("https://worker.formextractorai.com/v2/extract")
                        .post(requestBody)
                        .addHeader("accept", "application/json")
                        .addHeader("X-WORKER-EXTRACTOR-ID", "a1ccfe44-18cc-48bf-82c9-94aa89f10b2f")
                        .addHeader("X-WORKER-TOKEN", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyZXNvdXJjZV9vd25lcl9pZCI6IjUwZjU3ZDJkLWJiNmMtNGUzNy04NzdjLTJlNGQ0MjUzMjJlNyIsIndvcmtlcl90b2tlbl9pZCI6ImZkNGFmNTJkLWVjOGMtNGZjYy04ZDgxLTM2NDI1NDVkZTI1OSIsInVzZXJfaWQiOiI1MGY1N2QyZC1iYjZjLTRlMzctODc3Yy0yZTRkNDI1MzIyZTcifQ.jIDtbhIytxLfgxFIHbLNsN0PXGdVCZPv0mKz4mPiwfs")
                        .build();


                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {

                    String jsonResponse = response.body().string();
                    formattedJson = formatJson(jsonResponse);


                    runOnUiThread(() -> {
                        if (formattedJson != null) {
                            Map<String, Object> jsonMap = new Gson().fromJson(formattedJson, HashMap.class);
                            displayJsonForm(jsonMap, jsonFormContainer);
                        }
                    });
                } else {
                    deletePdf("Again");
                }
            } catch (IOException e) {
                deletePdf("Again");
            }
        }).start();
    }

    private String formatJson(String json) {
        Gson gson = new Gson();
        try {

            Map<?, ?> map = gson.fromJson(json, Map.class);


            if (map.containsKey("documents")) {
                List<?> documents = (List<?>) map.get("documents");
                if (!documents.isEmpty()) {
                    Map<?, ?> document = (Map<?, ?>) documents.get(0);
                    if (document.containsKey("data")) {
                        Map<?, ?> data = (Map<?, ?>) document.get("data");


                        Map<String, Object> formattedData = new HashMap<>();
                        formattedData.put("nationality", data.get("nationality"));
                        formattedData.put("age", data.get("age"));
                        formattedData.put("gender", data.get("gender"));
                        formattedData.put("date_of_birth", data.get("date_of_birth"));
                        formattedData.put("education", data.get("education"));
                        formattedData.put("marital_status", data.get("marital_status"));
                        formattedData.put("religion", data.get("religion"));
                        formattedData.put("height", data.get("height"));
                        formattedData.put("weight", data.get("weight"));
                        formattedData.put("ranking_by_age", data.get("ranking_by_age"));
                        formattedData.put("no_of_brother", data.get("no_of_brother"));
                        formattedData.put("no_of_sister", data.get("no_of_sister"));
                        formattedData.put("son_no_age", data.get("son_no_age"));
                        formattedData.put("daughter_no_age", data.get("daughter_no_age"));
                        formattedData.put("zodiac", data.get("zodiac"));


                        Map<String, Object> workingExperience = new HashMap<>();
                        workingExperience.put("care_of_babies", data.get("care_of_babies"));
                        workingExperience.put("care_of_toddler", data.get("care_of_toddler"));
                        workingExperience.put("care_of_children", data.get("care_of_children"));
                        workingExperience.put("care_of_elderly", data.get("care_of_elderly"));
                        workingExperience.put("care_of_disabled", data.get("care_of_disabled"));
                        workingExperience.put("care_of_bedridden", data.get("care_of_bedridden"));
                        workingExperience.put("care_of_pet", data.get("care_of_pet"));
                        workingExperience.put("household_works", data.get("household_works"));
                        workingExperience.put("car_washing", data.get("car_washing"));
                        workingExperience.put("gardening", data.get("gardening"));
                        workingExperience.put("cooking", data.get("cooking"));
                        workingExperience.put("driving", data.get("driving"));


                        formattedData.put("Working Experience", workingExperience);


                        Map<String, Object> overseasExperience = new HashMap<>();
                        overseasExperience.put("hong_kong", data.get("hong_kong"));
                        overseasExperience.put("singapore", data.get("singapore"));
                        overseasExperience.put("taiwan", data.get("taiwan"));
                        overseasExperience.put("malaysia", data.get("malaysia"));
                        overseasExperience.put("middle_east", data.get("middle_east"));
                        overseasExperience.put("macau", data.get("macau"));
                        overseasExperience.put("other", data.get("other"));
                        overseasExperience.put("home_country", data.get("home_country"));


                        formattedData.put("overseasExperience", overseasExperience);


                        formattedData.put("language_skills", data.get("language_skills"));


                        formattedData.put("remark", data.get("remark"));


                        return gson.toJson(formattedData);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting JSON: " + e.getMessage());
        }


        return "Invalid JSON or 'data' key not found.";
    }


    private Map<String, Object> formatDataForFirestore(Map<String, Object> data) {
        Map<String, Object> formattedData = new HashMap<>();


        formattedData.put("nationality", data.get("nationality"));
        formattedData.put("age", data.get("age"));
        formattedData.put("gender", data.get("gender"));
        formattedData.put("date_of_birth", data.get("date_of_birth"));
        formattedData.put("education", data.get("education"));
        formattedData.put("marital_status", data.get("marital_status"));
        formattedData.put("religion", data.get("religion"));
        formattedData.put("height", data.get("height"));
        formattedData.put("weight", data.get("weight"));
        formattedData.put("ranking_by_age", data.get("ranking_by_age"));
        formattedData.put("no_of_brother", data.get("no_of_brother"));
        formattedData.put("no_of_sister", data.get("no_of_sister"));
        formattedData.put("son_no_age", data.get("son_no_age"));
        formattedData.put("daughter_no_age", data.get("daughter_no_age"));
        formattedData.put("zodiac", data.get("zodiac"));


        Map<String, Object> workingExperience = new HashMap<>();
        workingExperience.put("care_of_babies", data.get("care_of_babies"));
        workingExperience.put("care_of_toddler", data.get("care_of_toddler"));
        workingExperience.put("care_of_children", data.get("care_of_children"));
        workingExperience.put("care_of_elderly", data.get("care_of_elderly"));
        workingExperience.put("care_of_disabled", data.get("care_of_disabled"));
        workingExperience.put("care_of_bedridden", data.get("care_of_bedridden"));
        workingExperience.put("care_of_pet", data.get("care_of_pet"));
        workingExperience.put("household_works", data.get("household_works"));
        workingExperience.put("car_washing", data.get("car_washing"));
        workingExperience.put("gardening", data.get("gardening"));
        workingExperience.put("cooking", data.get("cooking"));
        workingExperience.put("driving", data.get("driving"));
        formattedData.put("working_experience", workingExperience);


        Map<String, Object> overseasExperience = new HashMap<>();
        overseasExperience.put("hong_kong", data.get("hong_kong"));
        overseasExperience.put("singapore", data.get("singapore"));
        overseasExperience.put("taiwan", data.get("taiwan"));
        overseasExperience.put("malaysia", data.get("malaysia"));
        overseasExperience.put("middle_east", data.get("middle_east"));
        overseasExperience.put("macau", data.get("macau"));
        overseasExperience.put("other", data.get("other"));
        overseasExperience.put("home_country", data.get("home_country"));
        formattedData.put("overseas_experience", overseasExperience);


        HashMap<String, String> languageSkills = (HashMap<String, String>) data.get("language_skills");
        if (languageSkills != null) {
            formattedData.put("language_skills", languageSkills);
        }


        formattedData.put("remark", data.get("remark"));
        formattedData.put("email", data.get("email"));
        formattedData.put("name", name_input.getText().toString());
        formattedData.put("availability", "Available");

        return formattedData;
    }

    private void uploadToFirestore(Map<String, Object> data) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("MaidInfo")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    deletePdf("Submit");
                    Toast.makeText(Pdfbox.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                })

                .addOnFailureListener(e -> Toast.makeText(Pdfbox.this, "Failed to save data!", Toast.LENGTH_SHORT).show());
    }
}