package com.project.fypproject.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.R;
import com.project.fypproject.activities.employer.HiringStatusActivity;

public class DhHomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    LinearLayout llMeetingRequest, llInterviewInvitation;
    TextView txtName;
    FirebaseAuth auth;
    FirebaseUser user;
    String employer;
    String employerName = "not found";

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dh_home, container, false);

        llInterviewInvitation = view.findViewById(R.id.btn_InterviewInvitation);
        llMeetingRequest = view.findViewById(R.id.btn_request);

        txtName = view.findViewById(R.id.txtName);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
        }

        llInterviewInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BookingRequestActivity.class);
                startActivity(intent);
            }
        });

        llMeetingRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BookingRequestActivity.class);
                startActivity(intent);
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("firstName");
                        txtName.setText(name);
                        Log.d("Dennis", "My name is " + name);
                        employer = document.getString("employer");
                        LinearLayout mainContainer = view.findViewById(R.id.MySalary_Container);
                        if(employer != null && !employer.trim().isEmpty()){
                            // 2. 建立新的 LinearLayout
                            LinearLayout newLayout = new LinearLayout(getContext());
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    dpToPx(110), // 設定寬度 110dp
                                    dpToPx(150)  // 設定高度 150dp
                            );
                            layoutParams.setMargins(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10)); // 設定 margin
                            newLayout.setLayoutParams(layoutParams);
                            newLayout.setOrientation(LinearLayout.VERTICAL);
                            newLayout.setGravity(Gravity.CENTER);
                            newLayout.setId(R.id.btn_MySalary);
                            newLayout.setBackgroundResource(R.drawable.card); // 設定背景

                            newLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getActivity(), HiringStatusActivity.class);
                                    Bundle b = new Bundle();
                                    b.putString("employerEmail", employer);
                                    b.putString("employeeEmail", user.getEmail());
                                    intent.putExtras(b);
                                    startActivity(intent);
                                }
                            });

                            // 3. 建立 ShapeableImageView
                            ShapeableImageView imageView = new ShapeableImageView(getContext());
                            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dpToPx(75), dpToPx(75));
                            imageView.setLayoutParams(imgParams);
                            imageView.setImageResource(R.drawable.icon_domestic_helper);
                            imageView.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
                            imageView.setStrokeWidth(1f);
                            imageView.setBackgroundResource(R.drawable.rounded_image_view);

                            // 4. 建立 TextView (MyDomesticHelper_name)

                            DocumentReference docRef2 = db.collection("users").document(employer);
                            docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {

                                            employerName = document.getString("firstName") + " " + document.getString("lastName");
                                            TextView textView = new TextView(getContext());
                                            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    dpToPx(40)
                                            );
                                            textView.setLayoutParams(textParams);
                                            textView.setGravity(Gravity.CENTER);
                                            textView.setText(employerName);
                                            textView.setTextSize(16);
                                            textView.setTextColor(Color.BLACK);

                                            // 5. 將 ShapeableImageView 和 TextView 加入 LinearLayout
                                            newLayout.addView(imageView);
                                            newLayout.addView(textView);

                                            // 6. 將新建的 LinearLayout 加入 mainContainer
                                            mainContainer.addView(newLayout);
                                        }
                                    }
                                }
                            });
                        }
                        else {
                            TextView textView = new TextView(getContext());
                            textView.setText("Let's find a new employer");
                            textView.setTextSize(18);
                            textView.setGravity(Gravity.CENTER);
                            textView.setPadding(16, 50, 16, 50);

                            mainContainer.addView(textView);
                        }
                    }
                }
            }
        });

        return view;
    }
}