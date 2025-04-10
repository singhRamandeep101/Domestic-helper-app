package com.project.fypproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.fypproject.R;

public class Registry_usertype extends AppCompatActivity {

    Button btnForEmp, btnForDH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registry_usertype);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnForDH = findViewById(R.id.lookingForDH);
        btnForEmp = findViewById(R.id.lookingForEmployer);

        btnForEmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);

                Bundle b = new Bundle();
                b.putString("userType", "DomesticHelper");
                intent.putExtras(b);

                startActivity(intent);
                finish();
            }
        });

        btnForDH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);

                Bundle b = new Bundle();
                b.putString("userType", "Employer"); //userType 2 = looking For a Domestic Helper
                intent.putExtras(b);

                startActivity(intent);
                finish();
            }
        });
    }
}