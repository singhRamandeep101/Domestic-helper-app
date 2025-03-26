package com.project.fypproject.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.project.fypproject.R;

public class AgentHomeFragment extends Fragment {

    LinearLayout llAdd_dh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_agent_home, container, false);

        llAdd_dh = view.findViewById(R.id.btn_add_dh);

        llAdd_dh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MeetingActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}