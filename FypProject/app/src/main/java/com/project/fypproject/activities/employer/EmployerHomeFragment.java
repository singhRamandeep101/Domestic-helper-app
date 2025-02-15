package com.project.fypproject.activities.employer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.fypproject.R;

public class EmployerHomeFragment extends Fragment {

    FirebaseAuth auth;
    Button btnLogout;
    LinearLayout llPublicHoliday, llJobList, llJobPost;
    TextView textView;
    FirebaseUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        btnLogout = findViewById(R.id.btn_logout);
//
//        llPublicHoliday = findViewById(R.id.btn_PublicHoliday);
//        llJobList = findViewById(R.id.btn_Find);
//        llJobPost = findViewById(R.id.btn_JobPost);
//
//        auth = FirebaseAuth.getInstance();
//        user = auth.getCurrentUser();
//        if (user == null) {
//            Intent intent = new Intent(getApplicationContext(), Login.class);
//            startActivity(intent);
//            finish();
//        }else{
////            textView.setText(user.getEmail());
//        }
//
//        btnLogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseAuth.getInstance().signOut();
//
//                Intent intent = new Intent(getApplicationContext(), Login.class);
//                startActivity(intent);
//                finish();
//            }
//        });


//        btnLogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
//
//                Intent intent = new Intent(getApplicationContext(), Login.class);
//                startActivity(intent);
//                finish();
//            }
//        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_emplyer_home, container, false);
    }
}