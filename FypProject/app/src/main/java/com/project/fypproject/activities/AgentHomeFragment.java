package com.project.fypproject.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.project.fypproject.R;
import com.project.fypproject.activities.employer.JobListActivity;

public class AgentHomeFragment extends Fragment {

    LinearLayout llAdd_dh,llJobList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_agent_home, container, false);

        llAdd_dh = view.findViewById(R.id.btn_add_dh);
        llJobList = view.findViewById(R.id.btn_Find);

        llJobList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), JobListActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        llAdd_dh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Pdfbox.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        return view;
    }
}