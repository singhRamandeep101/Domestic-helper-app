package com.project.fypproject.activities.employer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.project.fypproject.R;

public class JobPostActivity extends AppCompatActivity {

    private Button btnBirth, btnSubmit, btnAddPre;
    private EditText etSNo, etDNo, edQ7Yes,edOther;
    private LinearLayout layPreCont, laySAge, layDAge;
    private ImageView imUser;
    private Spinner spMS;
    private RadioButton rdQ7Yes, rdQ7No;
    private CheckBox cbOther;
    private Toolbar toolbar;

    private int tCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_job_post);

        btnBirth = findViewById(R.id.btnBirth);
        btnSubmit = findViewById(R.id.btnSubmit);
        imUser = findViewById(R.id.imUser);
        spMS = findViewById(R.id.spMS);
        etSNo = findViewById(R.id.evSonNo);
        etDNo = findViewById(R.id.evDaughterNo);
        laySAge = findViewById(R.id.layoutSAge);
        layDAge = findViewById(R.id.layoutDAge);
        rdQ7Yes = findViewById(R.id.rvQ7Yes);
        rdQ7No = findViewById(R.id.rvQ7No);
        edQ7Yes = findViewById(R.id.edQ7Yes);
        cbOther = findViewById(R.id.cbOther1);
        btnAddPre = findViewById(R.id.btnAddPre);
        layPreCont = findViewById(R.id.layPreCont);
        edOther = findViewById(R.id.edOther1);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnAddPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPreviousDuties();
                btnAddPre.setVisibility(View.GONE);
            }
        });


        imUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        // Initialize Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Single", "Married"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMS.setAdapter(adapter);

        // Set Listeners
        btnBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateSelect(btnBirth);
            }
        });

        etSNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addEditView(laySAge, etSNo, "Son ", " Age");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etDNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addEditView(layDAge, etDNo, "Daughter ", " Age");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        rdQ7Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edQ7Yes.setVisibility(View.VISIBLE);
            }
        });

        rdQ7No.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edQ7Yes.setVisibility(View.GONE);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        cbOther.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edOther.setVisibility(View.VISIBLE);
                } else {
                    edOther.setText("");
                    edOther.setVisibility(View.GONE);

                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                Glide.with(this).load(imageUri).into(imUser);
            }
        }
    }

    private void showDateSelect(final Button btn) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, DatePickerDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                btn.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();

    }

    private void addEditView(LinearLayout layout, EditText et, String text, String text2) {
        layout.removeAllViews();
        String etText = et.getText().toString().trim();

        if (etText.isEmpty()) {
            return;
        }

        int count = Integer.parseInt(et.getText().toString());
        for (int i = 0; i < count; i++) {
            EditText editText = new EditText(this);
            editText.setHint(text + (i + 1) + text2);
            layout.addView(editText);
        }
    }


    private void addPreviousDuties() {
        final LinearLayout layPre = new LinearLayout(this);
        layPre.setOrientation(LinearLayout.VERTICAL);
        layPre.setBackgroundResource(R.drawable.border);
        layPre.setPadding(30, 30, 30, 30);
        layPre.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));


        TextView tvTitle = new TextView(this);
        tvTitle.setText("Previous Duties " + tCount);
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTitle.setTextColor(Color.BLACK);
        tvTitle.setPadding(0, 30, 0, 30);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        tCount++;

        TextView tvWC = new TextView(this);
        tvWC.setText("Working Country");
        tvWC.setTextSize(16);
        tvWC.setTypeface(null, Typeface.BOLD);
        tvWC.setPadding(0, 5, 0, 10);
        tvWC.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        EditText etWC = new EditText(this);
        etWC.setHint("Working Country");
        etWC.setPadding(20, 20, 20, 20);
        etWC.setBackgroundResource(R.drawable.border);
        etWC.setInputType(InputType.TYPE_CLASS_TEXT);
        etWC.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout layDate = new LinearLayout(this);
        layDate.setOrientation(LinearLayout.HORIZONTAL);
        layDate.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button btnStartDate = new Button(this);
        btnStartDate.setText("Start Date");
        btnStartDate.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        btnStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateSelect(btnStartDate);
            }
        });

        TextView tvTo = new TextView(this);
        tvTo.setText("To");
        tvTo.setGravity(Gravity.CENTER);
        tvTo.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button btnEndDate = new Button(this);
        btnEndDate.setText("End Date");
        btnEndDate.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        btnEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateSelect(btnEndDate);
            }
        });

        layDate.addView(btnStartDate);
        layDate.addView(tvTo);
        layDate.addView(btnEndDate);

        TextView tvSalary = new TextView(this);
        tvSalary.setText("Salary");
        tvSalary.setTextSize(16);
        tvSalary.setTypeface(null, Typeface.BOLD);
        tvSalary.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        EditText etSalary = new EditText(this);
        etSalary.setHint("Salary");
        etSalary.setPadding(20, 20, 20, 20);
        etSalary.setBackgroundResource(R.drawable.border);
        etSalary.setInputType(InputType.TYPE_CLASS_NUMBER);
        etSalary.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView tvNoServe = new TextView(this);
        tvNoServe.setText("No. of Serve");
        tvNoServe.setTextSize(16);
        tvNoServe.setTypeface(null, Typeface.BOLD);
        tvNoServe.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        EditText etNoServe = new EditText(this);
        etNoServe.setHint("No. of Serve");
        etNoServe.setPadding(20, 20, 20, 20);
        etNoServe.setBackgroundResource(R.drawable.border);
        etNoServe.setInputType(InputType.TYPE_CLASS_NUMBER);
        etNoServe.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView tvRL = new TextView(this);
        tvRL.setText("Reason to Leave");
        tvRL.setTextSize(16);
        tvRL.setTypeface(null, Typeface.BOLD);
        tvRL.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        EditText etRL = new EditText(this);
        etRL.setHint("Reason to Leave");
        etRL.setPadding(20, 20, 20, 20);
        etRL.setBackgroundResource(R.drawable.border);
        etRL.setInputType(InputType.TYPE_CLASS_TEXT);
        etRL.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        CheckBox cbCareBabies = new CheckBox(this);
        cbCareBabies.setText("Care of Babies");


        EditText etCareBabiesNo = new EditText(this);
        etCareBabiesNo.setHint("Babies No");
        etCareBabiesNo.setVisibility(View.GONE);

        LinearLayout layCareBabies = new LinearLayout(this);
        layCareBabies.setOrientation(LinearLayout.VERTICAL);
        layCareBabies.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        cbCareBabies.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etCareBabiesNo.setVisibility(View.VISIBLE);
                    layCareBabies.setVisibility(View.VISIBLE);
                } else {
                    etCareBabiesNo.setText("");
                    etCareBabiesNo.setVisibility(View.GONE);
                    layCareBabies.setVisibility(View.GONE);

                }
            }
        });

        etCareBabiesNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addEditView(layCareBabies, etCareBabiesNo, "Babies", " Mths");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        CheckBox cbCareToddler = new CheckBox(this);
        cbCareToddler.setText("Care of Toddler(1-3)");


        EditText etCareToddlerNo = new EditText(this);
        etCareToddlerNo.setHint("Care of Toddler");
        etCareToddlerNo.setVisibility(View.GONE);

        LinearLayout layCareToddler = new LinearLayout(this);
        layCareToddler.setOrientation(LinearLayout.VERTICAL);
        layCareToddler.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        cbCareToddler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etCareToddlerNo.setVisibility(View.VISIBLE);
                    layCareToddler.setVisibility(View.VISIBLE);
                } else {
                    etCareToddlerNo.setText("");
                    etCareToddlerNo.setVisibility(View.GONE);
                    layCareToddler.setVisibility(View.GONE);

                }
            }
        });

        etCareToddlerNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addEditView(layCareToddler, etCareToddlerNo, "Toddler", " Yrs");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        CheckBox cbCareChildren = new CheckBox(this);
        cbCareChildren.setText("Care of Children(4-12)");


        EditText etCareChildren = new EditText(this);
        etCareChildren.setHint("Children No");
        etCareChildren.setVisibility(View.GONE);

        LinearLayout layCareChildren = new LinearLayout(this);
        layCareChildren.setOrientation(LinearLayout.VERTICAL);
        layCareChildren.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        cbCareChildren.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etCareChildren.setVisibility(View.VISIBLE);
                    layCareChildren.setVisibility(View.VISIBLE);
                } else {
                    etCareChildren.setText("");
                    etCareChildren.setVisibility(View.GONE);
                    layCareChildren.setVisibility(View.GONE);

                }
            }
        });

        etCareChildren.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addEditView(layCareChildren, etCareChildren, "Children", " Yrs");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        CheckBox cbCareElderly = new CheckBox(this);
        cbCareElderly.setText("Care of Elderly");


        EditText etCareElderly = new EditText(this);
        etCareElderly.setHint("Elderly No");
        etCareElderly.setVisibility(View.GONE);

        LinearLayout layCareElderly = new LinearLayout(this);
        layCareElderly.setOrientation(LinearLayout.VERTICAL);
        layCareElderly.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        cbCareElderly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etCareElderly.setVisibility(View.VISIBLE);
                    layCareElderly.setVisibility(View.VISIBLE);
                } else {
                    etCareElderly.setText("");
                    etCareElderly.setVisibility(View.GONE);
                    layCareElderly.setVisibility(View.GONE);

                }
            }
        });

        etCareElderly.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addEditView(layCareElderly, etCareElderly, "Elderly", " Yrs");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        CheckBox cbCareDisabled = new CheckBox(this);
        cbCareDisabled.setText("Care of Disabled");


        EditText etCareDisabled = new EditText(this);
        etCareDisabled.setHint("Disabled No");
        etCareDisabled.setVisibility(View.GONE);

        LinearLayout layCareDisabled = new LinearLayout(this);
        layCareDisabled.setOrientation(LinearLayout.VERTICAL);
        layCareDisabled.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        cbCareDisabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etCareDisabled.setVisibility(View.VISIBLE);
                    layCareDisabled.setVisibility(View.VISIBLE);
                } else {
                    etCareDisabled.setText("");
                    etCareDisabled.setVisibility(View.GONE);
                    layCareDisabled.setVisibility(View.GONE);

                }
            }
        });

        etCareDisabled.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addEditView(layCareDisabled, etCareDisabled, "Disabled", " Yrs");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        CheckBox cbCareBedridden = new CheckBox(this);
        cbCareBedridden.setText("Care of Bedridden");

        CheckBox cbPet = new CheckBox(this);
        cbPet.setText("Care of Pet");

        CheckBox cbHW = new CheckBox(this);
        cbHW.setText("Household Works");


        CheckBox cbCW = new CheckBox(this);
        cbCW.setText("Car Washing");

        CheckBox cbGard = new CheckBox(this);
        cbGard.setText("Gardening");


        CheckBox cbCook = new CheckBox(this);
        cbCook.setText("Cooking");

        CheckBox cbDriving = new CheckBox(this);
        cbDriving.setText("Driving");

        Button btnDeletePre = new Button(this);
        btnDeletePre.setText("Delete Previous Duties");
        btnDeletePre.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        btnDeletePre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layPreCont.removeView(layPre);
                tCount--;
                updateBtnPre("Title");
                updateBtnPre("btn");
                if (layPreCont.getChildCount() == 0) {
                    btnAddPre.setVisibility(View.VISIBLE);
                    tCount = 1;
                }
            }
        });

        Button btnAddPre1 = new Button(this);
        btnAddPre1.setText("Add Previous Duties");
        btnAddPre1.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        btnAddPre1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPreviousDuties();
            }
        });
        layPre.addView(tvTitle);
        layPre.addView(tvWC);
        layPre.addView(etWC);
        layPre.addView(layDate);
        layPre.addView(tvSalary);
        layPre.addView(etSalary);
        layPre.addView(tvNoServe);
        layPre.addView(etNoServe);
        layPre.addView(tvRL);
        layPre.addView(etRL);
        layPre.addView(cbCareBabies);
        layPre.addView(etCareBabiesNo);
        layPre.addView(layCareBabies);
        layPre.addView(cbCareToddler);
        layPre.addView(etCareToddlerNo);
        layPre.addView(layCareToddler);
        layPre.addView(cbCareChildren);
        layPre.addView(etCareChildren);
        layPre.addView(layCareChildren);
        layPre.addView(cbCareElderly);
        layPre.addView(etCareElderly);
        layPre.addView(layCareElderly);
        layPre.addView(cbCareDisabled);
        layPre.addView(etCareDisabled);
        layPre.addView(layCareDisabled);
        layPre.addView(cbCareBedridden);
        layPre.addView(cbPet);
        layPre.addView(cbHW);
        layPre.addView(cbCW);
        layPre.addView(cbGard);
        layPre.addView(cbCook);
        layPre.addView(cbDriving);
        layPre.addView(btnDeletePre);
        layPre.addView(btnAddPre1);
        layPreCont.addView(layPre);

        updateBtnPre("btn");
    }


    private void updateBtnPre(String type) {
        int childCount = layPreCont.getChildCount();
        for (int i = 0; i < childCount; i++) {
            LinearLayout layPre = (LinearLayout) layPreCont.getChildAt(i);
            if (type.equals("Title")) {
                TextView tvTitle = (TextView) layPre.getChildAt(0);
                tvTitle.setText("Previous Duties " + (i + 1));
            } else {
                Button btnAddPre1 = (Button) layPre.getChildAt(layPre.getChildCount() - 1);
                if (i == childCount - 1) {
                    btnAddPre1.setVisibility(View.VISIBLE);
                } else {
                    btnAddPre1.setVisibility(View.GONE);
                }
            }
        }
    }

    private void submit() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> formData = new HashMap<>();

        formData.put("about", ((EditText) findViewById(R.id.evAbout)).getText().toString());
        formData.put("nationality", ((EditText) findViewById(R.id.edNationality)).getText().toString());
        formData.put("gender", getRdText((RadioGroup) findViewById(R.id.rgGender)));
        formData.put("birthDate", btnBirth.getText().toString());
        formData.put("education", ((EditText) findViewById(R.id.evEducation)).getText().toString());
        formData.put("religion", ((EditText) findViewById(R.id.evReligion)).getText().toString());
        formData.put("maritalStatus", spMS.getSelectedItem().toString());
        formData.put("expectedSalary", ((EditText) findViewById(R.id.evES)).getText().toString());
        formData.put("height", ((EditText) findViewById(R.id.evHeight)).getText().toString());
        formData.put("weight", ((EditText) findViewById(R.id.evWeight)).getText().toString());
        formData.put("rankingByAge", ((EditText) findViewById(R.id.evRkAge)).getText().toString());
        formData.put("noOfBrothers", ((EditText) findViewById(R.id.evNoBrother)).getText().toString());
        formData.put("noOfSisters", ((EditText) findViewById(R.id.evNoSister)).getText().toString());

        formData.put("sonNo", etSNo.getText().toString());
        formData.put("sonAges", getEvNoData(laySAge));
        formData.put("daughterNo", etDNo.getText().toString());
        formData.put("daughterAges", getEvNoData(layDAge));

        Map<String,Object> strengths = new HashMap<>();
        CheckBox cbCareBabies1 = findViewById(R.id.cbCareBabies1);
        if (cbCareBabies1.isChecked()) {
            strengths.put("CareOfBabies", cbCareBabies1.isChecked());
        } else {
            strengths.put("CareOfBabies", cbCareBabies1.isChecked());
        }

        CheckBox cbCareToddler1 = findViewById(R.id.cbCareToddler1);
        if (cbCareToddler1.isChecked()) {
            strengths.put("CareOfToddler(1-3)", cbCareToddler1.isChecked());
        } else {
            strengths.put("CareOfToddler(1-3)", cbCareToddler1.isChecked());
        }

        CheckBox cbCareChildren1 =  findViewById(R.id.cbCareChildren1);
        if (cbCareChildren1.isChecked()) {
            strengths.put("CareOfChildren(4-12)", cbCareChildren1.isChecked());
        } else {
            strengths.put("CareOfChildren(4-12)", cbCareChildren1.isChecked());
        }

        CheckBox cbCareElderly1 = findViewById(R.id.cbCareElderly1);
        if (cbCareElderly1.isChecked()) {
            strengths.put("CareOfElderly", cbCareElderly1.isChecked());
        } else {
            strengths.put("CareOfElderly", cbCareElderly1.isChecked());
        }

        CheckBox cbCareDisabled1 = findViewById(R.id.cbCareDisabled1);
        if (cbCareDisabled1.isChecked()) {
            strengths.put("CareOfDisabled", cbCareDisabled1.isChecked());
        } else {
            strengths.put("CareOfDisabled", cbCareDisabled1.isChecked());
        }

        CheckBox cbBedridden1 = findViewById(R.id.cbCareBedridden1);
        if (cbBedridden1.isChecked()) {
            strengths.put("CareOfBedrieedn", cbBedridden1.isChecked());
        } else {
            strengths.put("CareOfBedrieedn", cbBedridden1.isChecked());
        }

        CheckBox cbCarePet1 = findViewById(R.id.cbCarePet1);
        if (cbCarePet1.isChecked()) {
            strengths.put("CareOfPet", cbCarePet1.isChecked());
        } else {
            strengths.put("CareOfPet", cbCarePet1.isChecked());
        }

        CheckBox cbHW1 = findViewById(R.id.cbHousehold1);
        if (cbHW1.isChecked()) {
            strengths.put("HouseholdWorks", cbHW1.isChecked());
        } else {
            strengths.put("HouseholdWorks", cbHW1.isChecked());
        }

        CheckBox cbCW1 = findViewById(R.id.cbCar1);
        if (cbCW1.isChecked()) {
            strengths.put("CarWashing", cbCW1.isChecked());
        } else {
            strengths.put("CarWashing", cbCW1.isChecked());
        }

        CheckBox cbGardening1 = findViewById(R.id.cbGardening1);
        if (cbGardening1.isChecked()) {
            strengths.put("Gardening", cbGardening1.isChecked());
        } else {
            strengths.put("Gardening", cbGardening1.isChecked());
        }

        CheckBox cbCook1 = findViewById(R.id.cbCooking1);
        if (cbCook1.isChecked()) {
            strengths.put("Cooking", cbCook1.isChecked());
        } else {
            strengths.put("Cooking", cbCook1.isChecked());
        }

        CheckBox cbDriving1 = findViewById(R.id.cbDriving1);
        if (cbDriving1.isChecked()) {
            strengths.put("Driving", cbDriving1.isChecked());
        } else {
            strengths.put("Driving", cbDriving1.isChecked());
        }

        CheckBox cbOther1 = findViewById(R.id.cbOther1);
        if (cbDriving1.isChecked()) {
            strengths.put("OtherStrengths", cbOther1.isChecked());
            strengths.put("OtherText",edOther.getText().toString().trim());
        } else {
            strengths.put("OtherStrengths", cbOther1.isChecked());
        }
        formData.put("strengths",strengths);

        Map<String, String> skills = new HashMap<>();
        skills.put("Mandarin", getRdText(findViewById(R.id.rgMandarin)));
        skills.put("Cantonese", getRdText(findViewById(R.id.rgCantonese)));
        skills.put("English", getRdText(findViewById(R.id.rgEnglish)));
        formData.put("languageSkills", skills);

        Map<String, String> questions = new HashMap<>();
        questions.put("Q1.Do you eat pork?", getRdText(findViewById(R.id.rgQ1)));
        questions.put("Q2.Accept Day-off not on Sunday?", getRdText(findViewById(R.id.rgQ2)));
        questions.put("Q3.Sharing a room with babies / children / elder?", getRdText(findViewById(R.id.rgQ3)));
        questions.put("Q4.Are you afraid of dog or cat?", getRdText(findViewById(R.id.rgQ4)));
        questions.put("Q5.Do you smoke?", getRdText(findViewById(R.id.rgQ5)));
        questions.put("Q6.Do you drink alcohol?", getRdText(findViewById(R.id.rgQ6)));
        questions.put("Q7.Have you any prolonged illnesses/undergone surgery?", getRdText(findViewById(R.id.rgQ7)));

        if(rdQ7Yes.isChecked()){
            questions.put("illnessDetails", ((EditText) findViewById(R.id.edQ7Yes)).getText().toString().trim());
        }

        formData.put("otherQuestions", questions);



        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String postTime = sdf.format(new Date());
        formData.put("postTime", postTime);

        String[] bDay = btnBirth.getText().toString().split("-");
        int year = Integer.parseInt(bDay[0]);
        int month = Integer.parseInt(bDay[1]);
        int day = Integer.parseInt(bDay[2]);

        Calendar today = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();
        dob.set(year,month - 1,day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if(today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        formData.put("age",String.valueOf(age));

        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) {
            formData.put("constellation", "Aquarius");
        } else if ((month == 2 && day >= 19) || (month == 3 && day <= 20)) {
            formData.put("constellation", "Pisces");
        } else if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) {
            formData.put("constellation", "Aries");
        } else if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) {
            formData.put("constellation", "Taurus");
        } else if ((month == 5 && day >= 21) || (month == 6 && day <= 20)) {
            formData.put("constellation", "Gemini");
        } else if ((month == 6 && day >= 21) || (month == 7 && day <= 22)) {
            formData.put("constellation", "Cancer");
        } else if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) {
            formData.put("constellation", "Leo");
        } else if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) {
            formData.put("constellation", "Virgo");
        } else if ((month == 9 && day >= 23) || (month == 10 && day <= 22)) {
            formData.put("constellation", "Libra");
        } else if ((month == 10 && day >= 23) || (month == 11 && day <= 21)) {
            formData.put("constellation", "Scorpio");
        } else if ((month == 11 && day >= 22) || (month == 12 && day <= 21)) {
            formData.put("constellation", "Sagittarius");
        } else if ((month == 12 && day >= 22) || (month == 1 && day <= 19)) {
            formData.put("constellation", "Capricorn");
        }

        List<Map<String, Object>> dutiesList = new ArrayList<>();
        Map<String,int[]> countryDuration = new HashMap<>();
        int totalYear=0,totalMonth=0;

        for (int i = 0; i < layPreCont.getChildCount(); i++) {
            View dutyView = layPreCont.getChildAt(i);
            if (dutyView instanceof LinearLayout) {
                LinearLayout dutyLayout = (LinearLayout) dutyView;
                Map<String, Object> dutyData = new HashMap<>();

                EditText etWC = (EditText) dutyLayout.getChildAt(2);
                String workingCountry = etWC.getText().toString().trim();
                dutyData.put("workingCountry", workingCountry);

                LinearLayout layDate = (LinearLayout) dutyLayout.getChildAt(3);
                Button btnStartDate = (Button) layDate.getChildAt(0);
                Button btnEndDate = (Button) layDate.getChildAt(2);
                String startDate = btnStartDate.getText().toString();
                String endDate = btnEndDate.getText().toString();
                dutyData.put("startDate", startDate);
                dutyData.put("endDate", endDate);

                String country = getCountry(workingCountry);
                int[] duration = calculateDuration(startDate,endDate);
                int dYear = duration[0];
                int dMonth = duration[1];

                String durationS = dYear+"year "+ dMonth + "month";

                dutyData.put("Duration",durationS);

                if(countryDuration.containsKey(country)){
                    int[] currentDuration = countryDuration.get(country);
                    currentDuration[0] += dYear;
                    currentDuration[1] += dMonth;

                    if(currentDuration[1] >= 12){
                        currentDuration[0] += currentDuration[1]/12;
                        currentDuration[1]%=12;
                    }
                }else{
                    countryDuration.put(country,new int[]{dYear,dMonth});
                }

                totalYear += dYear;
                totalMonth += dMonth;
                if(totalMonth >= 12){
                    totalYear += totalMonth/12;
                    totalMonth %=12;
                }

                EditText etSalary = (EditText) dutyLayout.getChildAt(5);
                dutyData.put("salary", etSalary.getText().toString().trim());

                EditText etNoServe = (EditText) dutyLayout.getChildAt(7);
                dutyData.put("noOfServe", etNoServe.getText().toString().trim());

                EditText etRL = (EditText) dutyLayout.getChildAt(9);
                dutyData.put("reasonToLeave", etRL.getText().toString().trim());

                CheckBox cbCareBabies = (CheckBox) dutyLayout.getChildAt(10);
                if (cbCareBabies.isChecked()) {
                    EditText etBabiesNo = (EditText) dutyLayout.getChildAt(11);
                    LinearLayout layBabies = (LinearLayout) dutyLayout.getChildAt(12);
                    dutyData.put("CareOfBabies", cbCareBabies.isChecked());
                    dutyData.put("babiesNo", etBabiesNo.getText().toString().trim());
                    dutyData.put("babiesAges", getEvNoData(layBabies));
                } else {
                    dutyData.put("CareOfBabies", cbCareBabies.isChecked());
                }

                CheckBox cbCareToddler = (CheckBox) dutyLayout.getChildAt(13);
                if (cbCareToddler.isChecked()) {
                    EditText etToddlerNo = (EditText) dutyLayout.getChildAt(14);
                    LinearLayout layToddler = (LinearLayout) dutyLayout.getChildAt(15);
                    dutyData.put("CareOfToddler(1-3)", cbCareToddler.isChecked());
                    dutyData.put("toddlersNo", etToddlerNo.getText().toString().trim());
                    dutyData.put("toddlersAges", getEvNoData(layToddler));
                } else {
                    dutyData.put("CareOfToddler(1-3)", cbCareToddler.isChecked());
                }

                CheckBox cbCareChildren = (CheckBox) dutyLayout.getChildAt(16);
                if (cbCareChildren.isChecked()) {
                    EditText etChildrenNo = (EditText) dutyLayout.getChildAt(17);
                    LinearLayout layChildren = (LinearLayout) dutyLayout.getChildAt(18);
                    dutyData.put("CareOfChildren(4-12)", cbCareChildren.isChecked());
                    dutyData.put("childrenNo", etChildrenNo.getText().toString().trim());
                    dutyData.put("childrenAges", getEvNoData(layChildren));
                } else {
                    dutyData.put("CareOfChildren(4-12)", cbCareChildren.isChecked());
                }

                CheckBox cbCareElderly = (CheckBox) dutyLayout.getChildAt(19);
                if (cbCareElderly.isChecked()) {
                    EditText etElderlyNo = (EditText) dutyLayout.getChildAt(20);
                    LinearLayout layElderly = (LinearLayout) dutyLayout.getChildAt(21);
                    dutyData.put("CareOfElderly", cbCareElderly.isChecked());
                    dutyData.put("elderlyNo", etElderlyNo.getText().toString().trim());
                    dutyData.put("elderlyAges", getEvNoData(layElderly));
                } else {
                    dutyData.put("CareOfElderly", cbCareElderly.isChecked());
                }

                CheckBox cbCareDisabled = (CheckBox) dutyLayout.getChildAt(22);
                if (cbCareDisabled.isChecked()) {
                    EditText etDisabledNo = (EditText) dutyLayout.getChildAt(23);
                    LinearLayout layDisabled = (LinearLayout) dutyLayout.getChildAt(24);
                    dutyData.put("CareOfDisabled", cbCareDisabled.isChecked());
                    dutyData.put("disabledNo", etDisabledNo.getText().toString().trim());
                    dutyData.put("disabledAges", getEvNoData(layDisabled));
                } else {
                    dutyData.put("CareOfDisabled", cbCareDisabled.isChecked());
                }

                CheckBox cbBedridden = (CheckBox) dutyLayout.getChildAt(25);
                if (cbBedridden.isChecked()) {
                    dutyData.put("CareOfBedrieedn", cbBedridden.isChecked());
                } else {
                    dutyData.put("CareOfBedrieedn", cbBedridden.isChecked());
                }

                CheckBox cbCarePet = (CheckBox) dutyLayout.getChildAt(26);
                if (cbCarePet.isChecked()) {
                    dutyData.put("CareOfPet", cbCarePet.isChecked());
                } else {
                    dutyData.put("CareOfPet", cbCarePet.isChecked());
                }

                CheckBox cbHW = (CheckBox) dutyLayout.getChildAt(27);
                if (cbHW.isChecked()) {
                    dutyData.put("HouseholdWorks", cbHW.isChecked());
                } else {
                    dutyData.put("HouseholdWorks", cbHW.isChecked());
                }

                CheckBox cbCW = (CheckBox) dutyLayout.getChildAt(28);
                if (cbCW.isChecked()) {
                    dutyData.put("CarWashing", cbCW.isChecked());
                } else {
                    dutyData.put("CarWashing", cbCW.isChecked());
                }

                CheckBox cbGardening = (CheckBox) dutyLayout.getChildAt(29);
                if (cbGardening.isChecked()) {
                    dutyData.put("Gardening", cbGardening.isChecked());
                } else {
                    dutyData.put("Gardening", cbGardening.isChecked());
                }

                CheckBox cbCook = (CheckBox) dutyLayout.getChildAt(30);
                if (cbCook.isChecked()) {
                    dutyData.put("Cooking", cbCook.isChecked());
                } else {
                    dutyData.put("Cooking", cbCook.isChecked());
                }

                CheckBox cbDriving = (CheckBox) dutyLayout.getChildAt(31);
                if (cbDriving.isChecked()) {
                    dutyData.put("Driving", cbDriving.isChecked());
                } else {
                    dutyData.put("Driving", cbDriving.isChecked());
                }

                dutiesList.add(dutyData);
            }
        }

        formData.put("previousDuties", dutiesList);

        Map<String,String> cd = new HashMap<>();
        for(Map.Entry<String,int[]> entry : countryDuration.entrySet()){
            String country = entry.getKey();
            int[] duration = entry.getValue();
            cd.put(country,duration[0]+"year " + duration[1] + "month");
        }
        formData.put("overseasExperience",cd);
        formData.put("totalExperienceDuration",totalYear + "year " + totalMonth +"month");
        formData.put("availability","Available");

        //        String userID = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String userEmail = "test";
        formData.put("userEmail",userEmail);

        db.collection("users").document(userEmail).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String firstName = documentSnapshot.getString("firstName");
                String lastName = documentSnapshot.getString("lastName");
                formData.put("firstName",firstName);
                formData.put("lastName",lastName);

                db.collection("HelperInfo")
                        .add(formData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(JobPostActivity.this,"Form Submit Success",Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(JobPostActivity.this,"Form Submit Failed",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private String getRdText(RadioGroup rg) {
        int selectId = rg.getCheckedRadioButtonId();
        RadioButton selectRd = findViewById(selectId);
        return selectRd.getText().toString();
    }

    private List<String> getEvNoData(LinearLayout layout) {
        List<String> dates = new ArrayList<>();
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof EditText) {
                String data = ((EditText) child).getText().toString().trim();
                dates.add(data);
            }
        }
        return dates;
    }

    private String getCountry(String loc) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addList = geocoder.getFromLocationName(loc, 1);
            if (addList != null && !addList.isEmpty()) {
                return addList.get(0).getCountryName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loc;
    }

    private int[] calculateDuration(String startDate,String endDate){
        try {

            String[] sDate = startDate.split("-");
            String[] eDate = endDate.split("-");

            int sYear = Integer.parseInt(sDate[0]);
            int sMonth = Integer.parseInt(sDate[1]);
            int sDay = Integer.parseInt(sDate[2]);

            int eYear = Integer.parseInt(eDate[0]);
            int eMonth = Integer.parseInt(eDate[1]);
            int eDay = Integer.parseInt(eDate[2]);

            int years = eYear - sYear;
            int months = eMonth - sMonth;

            if(months < 0){
                years--;
                months +=12;
            }

            if(eDay < sDay){
                months--;
                if(months < 0){
                    years--;
                    months +=12;
                }
            }

            return new int[]{years,months};

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{0,0};
    }
}