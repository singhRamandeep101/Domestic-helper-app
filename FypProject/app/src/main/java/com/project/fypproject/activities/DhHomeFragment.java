package com.project.fypproject.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.project.fypproject.R;

public class DhHomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    LinearLayout llPublicHoliday;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dh_home, container, false);

        llPublicHoliday = view.findViewById(R.id.btn_meeting);
        llPublicHoliday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BookingRequestActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        return view;
    }
}