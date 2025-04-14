package com.project.fypproject.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project.fypproject.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Pdfbox extends AppCompatActivity {

    private LinearLayout formContainer;
    private Button buttonSave;
    private JSONObject currentJsonData;
    private ScrollView scrollView;
    private TextView textViewName, textViewEmail;

    private Uri currentFileUri;
    private String currentFileName;
    private FirebaseFirestore db;

    private Bitmap extractedImageBitmap;

    private void extractImageFromPdf(String pdfPath, int pageNumber, Rect cropRect) {
        try {
            File pdfFile = new File(pdfPath);
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);

            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
            PdfRenderer.Page page = pdfRenderer.openPage(pageNumber - 1); // Page numbers are 0-based

            Bitmap pageBitmap = Bitmap.createBitmap(
                    page.getWidth(), page.getHeight(),
                    Bitmap.Config.ARGB_8888
            );

            page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            Bitmap croppedBitmap = Bitmap.createBitmap(
                    cropRect.width(),
                    cropRect.height(),
                    Bitmap.Config.ARGB_8888
            );

            Canvas canvas = new Canvas(croppedBitmap);
            canvas.drawBitmap(
                    pageBitmap,
                    new Rect(cropRect.left, cropRect.top, cropRect.right, cropRect.bottom),
                    new Rect(0, 0, cropRect.width(), cropRect.height()),
                    null
            );

            extractedImageBitmap = croppedBitmap;
            showExtractedImage();

            page.close();
            pdfRenderer.close();
            fileDescriptor.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to extract image", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExtractedImage() {
        if (extractedImageBitmap != null) {
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(extractedImageBitmap);
            imageView.setPadding(0, 16, 0, 16);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    500
            );
            imageView.setLayoutParams(layoutParams);

            for (int i = 0; i < formContainer.getChildCount(); i++) {
                View child = formContainer.getChildAt(i);
                if (child instanceof LinearLayout) {
                    LinearLayout layout = (LinearLayout) child;

                    if (layout.getChildCount() > 1) {
                        View field = layout.getChildAt(1);
                        if (field.getTag() != null && field.getTag().equals("name")) {
                            int insertPosition = formContainer.indexOfChild(layout) + 1;
                            formContainer.addView(imageView, insertPosition);
                            return;
                        }
                    }
                }
            }
        }
    }

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    currentFileUri = result.getData().getData();
                    currentFileName = getFileNameFromUri(currentFileUri);

                    String filePath = copyFileToCache(currentFileUri);
                    if (filePath != null) {
                        updateNameAndEmailFields(currentFileName);
                        processPdf(filePath);
                    } else {
                        Toast.makeText(this, "Failed to copy file to cache", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void updateNameAndEmailFields(String name) {

        String cleanedName = name.replaceAll("(?i)AM\\s*\\d*", "").trim();


        for (int i = 0; i < formContainer.getChildCount(); i++) {
            View child = formContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) child;
                if (layout.getChildCount() > 1) {
                    View field = layout.getChildAt(1);
                    if (field.getTag() != null && field.getTag().equals("name") && field instanceof TextView) {
                        ((TextView) field).setText(cleanedName);
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfbox);

        db = FirebaseFirestore.getInstance();


        Button buttonSelectFile = findViewById(R.id.buttonSelectFile);
        buttonSave = findViewById(R.id.buttonSave);
        scrollView = findViewById(R.id.scrollView);
        formContainer = findViewById(R.id.formContainer);

        buttonSelectFile.setOnClickListener(v -> openFilePicker());
        buttonSave.setOnClickListener(v -> saveChanges());
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();

        if (scheme != null && scheme.equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (fileName == null) {
            fileName = uri.getPath();
            int cut = fileName != null ? fileName.lastIndexOf('/') : -1;
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }

        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }

        return fileName;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select PDF"));
    }

    private String copyFileToCache(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            File tempFile = new File(getCacheDir(), "temp_pdf_" + System.currentTimeMillis() + ".pdf");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void processPdf(String filePath) {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        Python py = Python.getInstance();
        PyObject pyObject = py.getModule("process_pdf");

        try {
            PyObject result = pyObject.callAttr("extract_pdf_to_json_with_checkmarks", filePath);
            String jsonResult = result.toString();
            JSONObject originalJson = new JSONObject(jsonResult);

            Log.d("Original JSON", originalJson.toString(4));

            if (currentFileUri != null) {
                currentFileName = getFileNameFromUri(currentFileUri);
            }

            JSONObject formattedJson = processJsonData(originalJson);
            currentJsonData = formattedJson;

            displayEditableForm(formattedJson);

            // Extract the image from Page 1, specific coordinates
            Rect cropRect = new Rect(296, 300, 553, 643); // Adjust to match your coordinates
            extractImageFromPdf(filePath, 1, cropRect);

            Toast.makeText(this, "PDF Processed Successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to process PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONObject processJsonData(JSONObject originalJson) throws JSONException {
        JSONObject formattedJson = new JSONObject();

        JSONObject page = originalJson.getJSONArray("pages").getJSONObject(0);
        JSONArray tables = page.getJSONArray("tables");

        JSONArray basicInfoTable = tables.getJSONArray(0);
        for (int i = 0; i < basicInfoTable.length(); i++) {
            JSONArray row = basicInfoTable.getJSONArray(i);
            if (row.length() >= 4) {
                String key = row.getString(0).trim();
                String value = row.getString(1).trim();
                switch (key) {
                    case "Nationality":
                        formattedJson.put("nationality", value.replace(" ", "").trim());
                        if (row.length() >= 4) {
                            String ageAndZodiac = row.getString(3).trim();
                            ageAndZodiac = ageAndZodiac.replaceAll("\\s+", "");
                            String age = ageAndZodiac.replaceAll("[^0-9]", "");
                            String zodiac = ageAndZodiac.replaceAll("^[A-Za-z]+\\d+", "");
                            if (!age.isEmpty()) formattedJson.put("age", age);
                            if (!zodiac.isEmpty()) formattedJson.put("zodiac", zodiac);
                        }
                        break;
                    case "Gender":
                        formattedJson.put("gender", value);
                        formattedJson.put("date_of_birth", row.getString(3).trim());
                        break;
                    case "Education":
                        formattedJson.put("education", value.replace(" ", ""));
                        formattedJson.put("marital_status", row.getString(3).trim());
                        break;
                    case "Religion":
                        formattedJson.put("religion", value.replace(" ", ""));
                        formattedJson.put("height", row.getString(3).trim());
                        break;
                    case "Ranking by age":
                        formattedJson.put("ranking_by_age", value);
                        formattedJson.put("weight", row.getString(3).trim());
                        break;
                    case "No. of brother":
                        formattedJson.put("no_of_brother", value);
                        formattedJson.put("son_no_age", row.getString(3).trim());
                        break;
                    case "No. of sister":
                        formattedJson.put("no_of_sister", value);
                        formattedJson.put("daughter_no_age", row.getString(3).trim());
                        break;
                }
            }
        }

        JSONObject workingExperience = new JSONObject();
        JSONArray workExpTable = tables.getJSONArray(1);
        for (int i = 1; i < workExpTable.length(); i++) {
            JSONArray row = workExpTable.getJSONArray(i);
            if (row.length() >= 2) {
                String key = row.getString(0).trim();
                boolean value = row.getString(1).trim().equalsIgnoreCase("true");
                if (key.equalsIgnoreCase("Hong Kong")) break;
                workingExperience.put(key, value);
            }
        }
        formattedJson.put("working_experience", workingExperience);

        JSONObject overseasExperience = new JSONObject();
        JSONArray overseasExpTable = tables.getJSONArray(1);
        for (int i = 14; i < overseasExpTable.length(); i++) {
            JSONArray row = overseasExpTable.getJSONArray(i);
            if (row.length() >= 3) {
                String key = row.getString(0).trim();
                String value = row.getString(2).trim();
                overseasExperience.put(key, !value.isEmpty() ? value : JSONObject.NULL);
            } else if (row.length() >= 1) {
                String key = row.getString(0).trim();
                overseasExperience.put(key, JSONObject.NULL);
            }
        }
        formattedJson.put("overseas_experience", overseasExperience);

        JSONObject languageSkills = page.getJSONObject("language_abilities");
        formattedJson.put("language_skills", languageSkills);

        JSONArray remarkTable = tables.getJSONArray(2);
        if (remarkTable.length() > 1) {
            String remark = remarkTable.getJSONArray(1).getString(0).trim().replace("\n", " ");
            formattedJson.put("remark", remark);
        } else {
            formattedJson.put("remark", "No remark found");
        }

        return formattedJson;
    }

    private void displayEditableForm(JSONObject jsonData) throws JSONException {
        formContainer.removeAllViews();

        addNameAndEmailFields();
        addEditableField("nationality", jsonData.optString("nationality"));
        addEditableField("age", jsonData.optString("age"));
        addEditableField("zodiac", jsonData.optString("zodiac"));
        addEditableField("gender", jsonData.optString("gender"));
        addEditableField("date_of_birth", jsonData.optString("date_of_birth"));
        addEditableField("education", jsonData.optString("education"));
        addEditableField("marital_status", jsonData.optString("marital_status"));
        addEditableField("religion", jsonData.optString("religion"));
        addEditableField("height", jsonData.optString("height"));
        addEditableField("ranking_by_age", jsonData.optString("ranking_by_age"));
        addEditableField("weight", jsonData.optString("weight"));
        addEditableField("no_of_brother", jsonData.optString("no_of_brother"));
        addEditableField("son_no_age", jsonData.optString("son_no_age"));
        addEditableField("no_of_sister", jsonData.optString("no_of_sister"));
        addEditableField("daughter_no_age", jsonData.optString("daughter_no_age"));


        addSectionHeader("Working Experience");
        JSONObject workExp = jsonData.getJSONObject("working_experience");
        Iterator<String> workExpKeys = workExp.keys();
        while (workExpKeys.hasNext()) {
            String key = workExpKeys.next();
            boolean value = workExp.getBoolean(key);
            addCheckboxField("work_exp_" + key, key, value);
        }

        addSectionHeader("Overseas Experience");
        JSONObject overseasExp = jsonData.getJSONObject("overseas_experience");
        Iterator<String> overseasKeys = overseasExp.keys();
        while (overseasKeys.hasNext()) {
            String key = overseasKeys.next();
            String value = overseasExp.isNull(key) ? "" : overseasExp.getString(key);
            addEditableField("overseas_" + key, value);
        }

        addSectionHeader("Language Skills");
        JSONObject langSkills = jsonData.getJSONObject("language_skills");
        Iterator<String> langKeys = langSkills.keys();
        while (langKeys.hasNext()) {
            String key = langKeys.next();
            String value = langSkills.getString(key);
            addSpinnerField("lang_" + key, key, value);
        }

        addEditableField("remark", jsonData.optString("remark"));

        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
    }

    private void addNameAndEmailFields() {
        LinearLayout nameLayout = new LinearLayout(this);
        nameLayout.setOrientation(LinearLayout.VERTICAL);
        nameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        nameLayout.setPadding(0, 8, 0, 8);

        TextView nameLabel = new TextView(this);
        nameLabel.setText("Name");
        nameLabel.setTextSize(16);
        nameLabel.setTextColor(getResources().getColor(android.R.color.black));
        nameLayout.addView(nameLabel);

        TextView nameValue = new TextView(this);
        nameValue.setTag("name");

        if (currentFileName != null && !currentFileName.isEmpty()) {
            String cleanedName = currentFileName.replaceAll("(?i)AM\\s*\\d*", "").trim();
            nameValue.setText(cleanedName);
        } else {
            nameValue.setText("No file selected");
        }

        nameValue.setTextSize(14);
        nameValue.setTextColor(getResources().getColor(android.R.color.black));
        nameLayout.addView(nameValue);

        formContainer.addView(nameLayout);

        LinearLayout emailLayout = new LinearLayout(this);
        emailLayout.setOrientation(LinearLayout.VERTICAL);
        emailLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        emailLayout.setPadding(0, 8, 0, 16);

        TextView emailLabel = new TextView(this);
        emailLabel.setText("Email");
        emailLabel.setTextSize(16);
        emailLabel.setTextColor(getResources().getColor(android.R.color.black));
        emailLayout.addView(emailLabel);

        EditText emailEditText = new EditText(this);
        emailEditText.setTag("email");
        emailEditText.setText("");
        emailEditText.setTextSize(14);
        emailEditText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailLayout.addView(emailEditText);

        formContainer.addView(emailLayout);
    }

    private void addSectionHeader(String title) {
        TextView header = new TextView(this);
        header.setText(title);
        header.setTextSize(18);
        header.setPadding(0, 16, 0, 8);
        formContainer.addView(header);
    }

    private void addEditableField(String fieldName, String value) {
        LinearLayout fieldLayout = new LinearLayout(this);
        fieldLayout.setOrientation(LinearLayout.VERTICAL);
        fieldLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        fieldLayout.setPadding(0, 8, 0, 8);

        TextView label = new TextView(this);
        label.setText(fieldName.replace("_", " "));
        label.setTextSize(16);
        fieldLayout.addView(label);

        EditText editText = new EditText(this);
        editText.setTag(fieldName);
        editText.setText(value);
        editText.setTextSize(14);
        fieldLayout.addView(editText);

        formContainer.addView(fieldLayout);
    }

    private void addCheckboxField(String fieldName, String labelText, boolean isChecked) {
        LinearLayout fieldLayout = new LinearLayout(this);
        fieldLayout.setOrientation(LinearLayout.HORIZONTAL);
        fieldLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        fieldLayout.setPadding(0, 8, 0, 8);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setTag(fieldName);
        checkBox.setChecked(isChecked);

        TextView label = new TextView(this);
        label.setText(labelText);
        label.setTextSize(16);
        label.setPadding(16, 0, 0, 0);

        fieldLayout.addView(checkBox);
        fieldLayout.addView(label);

        formContainer.addView(fieldLayout);
    }

    private void addSpinnerField(String fieldName, String labelText, String currentValue) {
        LinearLayout fieldLayout = new LinearLayout(this);
        fieldLayout.setOrientation(LinearLayout.VERTICAL);
        fieldLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        fieldLayout.setPadding(0, 8, 0, 8);

        TextView label = new TextView(this);
        label.setText(labelText);
        label.setTextSize(16);
        fieldLayout.addView(label);

        Spinner spinner = new Spinner(this);
        spinner.setTag(fieldName);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.language_skill_levels,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int position = adapter.getPosition(currentValue);
        if (position >= 0) {
            spinner.setSelection(position);
        }

        fieldLayout.addView(spinner);
        formContainer.addView(fieldLayout);
    }

    private void saveChanges() {
        try {
            JSONObject updatedJson = new JSONObject();

            for (int i = 0; i < formContainer.getChildCount(); i++) {
                View child = formContainer.getChildAt(i);

                if (child instanceof LinearLayout) {
                    LinearLayout fieldLayout = (LinearLayout) child;

                    if (fieldLayout.getChildCount() > 1) {
                        View field = fieldLayout.getChildAt(1);
                        if (field.getTag() != null) {
                            String tag = (String) field.getTag();

                            if (tag.equals("name") && field instanceof TextView) {
                                String value = ((TextView) field).getText().toString();
                                updatedJson.put(tag, value);
                            } else if (tag.equals("email") && field instanceof EditText) {
                                String value = ((EditText) field).getText().toString();
                                updatedJson.put(tag, value);
                            }
                        }
                    }

                    if (fieldLayout.getChildCount() > 1 && fieldLayout.getChildAt(1) instanceof EditText) {
                        EditText editText = (EditText) fieldLayout.getChildAt(1);
                        String key = (String) editText.getTag();
                        if (key != null && !key.equals("email")) {
                            String value = editText.getText().toString();
                            updatedJson.put(key, value);
                        }
                    } else if (fieldLayout.getChildCount() > 0 && fieldLayout.getChildAt(0) instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) fieldLayout.getChildAt(0);
                        String key = (String) checkBox.getTag();
                        boolean value = checkBox.isChecked();
                        updatedJson.put(key, value);
                    } else if (fieldLayout.getChildCount() > 1 && fieldLayout.getChildAt(1) instanceof Spinner) {
                        Spinner spinner = (Spinner) fieldLayout.getChildAt(1);
                        String key = (String) spinner.getTag();
                        String value = spinner.getSelectedItem().toString();
                        updatedJson.put(key, value);
                    }
                }
            }

            JSONObject workingExperience = new JSONObject();
            JSONObject overseasExperience = new JSONObject();
            JSONObject languageSkills = new JSONObject();

            Iterator<String> keys = updatedJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();

                if (key.startsWith("work_exp_")) {
                    String expKey = key.replace("work_exp_", "");
                    workingExperience.put(expKey, updatedJson.getBoolean(key));
                } else if (key.startsWith("overseas_")) {
                    String expKey = key.replace("overseas_", "");
                    String value = updatedJson.getString(key);
                    overseasExperience.put(expKey, value.isEmpty() ? JSONObject.NULL : value);
                } else if (key.startsWith("lang_")) {
                    String langKey = key.replace("lang_", "");
                    languageSkills.put(langKey, updatedJson.getString(key));
                }
            }

            updatedJson.put("working_experience", workingExperience);
            updatedJson.put("overseas_experience", overseasExperience);
            updatedJson.put("language_skills", languageSkills);

            currentJsonData = updatedJson;

            // Upload image to Firestore
            if (extractedImageBitmap != null) {
                uploadImageToFirestore(updatedJson);
            } else {
                saveToFirestore(updatedJson);
            }
            clearFormData();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving changes", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirestore(JSONObject jsonObject) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        extractedImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageData = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String imageFileName = "images/" + System.currentTimeMillis() + ".png";
        StorageReference imageRef = storageRef.child(imageFileName);

        UploadTask uploadTask = imageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                try {
                    jsonObject.put("image_url", uri.toString());
                    saveToFirestore(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error saving image URL", Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
        });
    }

    private void clearFormData() {
        formContainer.removeAllViews();

        currentFileUri = null;
        currentFileName = null;

        Toast.makeText(this, "Form data cleared successfully!", Toast.LENGTH_SHORT).show();
    }

    private void saveToFirestore(JSONObject jsonObject) {
        try {
            Map<String, Object> dataMap = jsonToMap(jsonObject);

            if (currentFileName != null) {
                dataMap.put("filename", currentFileName);
            }

            db.collection("users").whereEqualTo("userType", "Agent").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            String agentEmail = getRandomEmail(queryDocumentSnapshots);
                            dataMap.put("agentEmail", agentEmail);

                            db.collection("users").whereEqualTo("userType", "Translator").get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot translatorSnapshots) {
                                            String translatorEmail = getRandomEmail(translatorSnapshots);
                                            dataMap.put("translatorEmail", translatorEmail);
                                            dataMap.put("availability", "Available");

                                            db.collection("MaidInfo")
                                                    .add(dataMap)
                                                    .addOnSuccessListener(documentReference -> {
                                                        Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                                                        Toast.makeText(Pdfbox.this, "Data saved to Firestore!", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("Firestore", "Error adding document", e);
                                                        Toast.makeText(Pdfbox.this, "Failed to save to Firestore", Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    });
                        }
                    });

    } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error converting data", Toast.LENGTH_SHORT).show();
        }
    }

    private String getRandomEmail(Iterable<QueryDocumentSnapshot> snapshots) {
        int count = 0;
        String randomEmail = null;
        for (QueryDocumentSnapshot snapshot : snapshots) {
            if (new Random().nextInt(++count) == 0) {
                randomEmail = snapshot.getString("email");
            }
        }
        return randomEmail;
    }

    private Map<String, Object> jsonToMap(JSONObject jsonObject) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);

            if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject) value);
            }
            else if (value instanceof JSONArray) {
                value = jsonArrayToList((JSONArray) value);
            }
            else if (JSONObject.NULL.equals(value)) {
                value = null;
            }

            map.put(key, value);
        }

        return map;
    }

    private List<Object> jsonArrayToList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);

            if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject) value);
            }
            else if (value instanceof JSONArray) {
                value = jsonArrayToList((JSONArray) value);
            }
            else if (JSONObject.NULL.equals(value)) {
                value = null;
            }

            list.add(value);
        }
        return list;
    }
}