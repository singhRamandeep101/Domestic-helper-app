package com.project.fypproject.activities.employer;

import android.content.Intent;
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
import com.project.fypproject.activities.Login;

public class EmployerHomeFragment extends Fragment {

    FirebaseAuth auth;
    Button btnLogout;
    LinearLayout llPublicHoliday, llJobList, llJobPost;
    FirebaseUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_emplyer_home, container, false);

        btnLogout = view.findViewById(R.id.btn_logout);

        llPublicHoliday = view.findViewById(R.id.btn_PublicHoliday);
        llJobList = view.findViewById(R.id.btn_Find);
        llJobPost = view.findViewById(R.id.btn_JobPost);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        }else{
            // Implement the code the here!


        }

        return view;
    }
}