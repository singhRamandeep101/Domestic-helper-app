package com.project.fypproject.activities.employer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.fypproject.R;
import com.project.fypproject.activities.Login;

public class EmployerHomeFragment extends Fragment {

    Button btnLogout;
    LinearLayout llPublicHoliday, llJobList, llJobPost;
    FirebaseAuth auth;
    FirebaseUser user;
    String MyDomesticHelper;

    public void changeActivity(Class<?> cls){
        Intent intent = new Intent(getActivity(), cls);
        startActivity(intent);
        requireActivity().finish();
    }

    // dp 轉換成 px
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_emplyer_home, container, false);

        llPublicHoliday = view.findViewById(R.id.btn_PublicHoliday);
        llJobList = view.findViewById(R.id.btn_Find);
        llJobPost = view.findViewById(R.id.btn_JobPost);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            changeActivity(Login.class);
        }else{
            // Implement the code the here!
        }

        llJobList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeActivity(JobListActivity.class);
            }
        });

        EmployerMainActivity activity = (EmployerMainActivity) getActivity();
        if (activity != null) {
            MyDomesticHelper = activity.getUserInfo("DomesticHelper"); // 呼叫 Activity 中的方法
        }


        LinearLayout mainContainer = view.findViewById(R.id.MyDomesticHelper_Container);
        if(MyDomesticHelper != null){
            // 2. 建立新的 LinearLayout
            // 2. 建立新的 LinearLayout (btn_MyDomesticHelper)
            LinearLayout newLayout = new LinearLayout(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    dpToPx(110), // 設定寬度 110dp
                    dpToPx(150)  // 設定高度 150dp
            );
            layoutParams.setMargins(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10)); // 設定 margin
            newLayout.setLayoutParams(layoutParams);
            newLayout.setOrientation(LinearLayout.VERTICAL);
            newLayout.setGravity(Gravity.CENTER);
            newLayout.setBackgroundResource(R.drawable.card); // 設定背景

            // 3. 建立 ShapeableImageView
            ShapeableImageView imageView = new ShapeableImageView(getContext());
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dpToPx(75), dpToPx(75));
            imageView.setLayoutParams(imgParams);
            imageView.setImageResource(R.drawable.icon_domestic_helper);
            imageView.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
            imageView.setStrokeWidth(1f);
            imageView.setBackgroundResource(R.drawable.rounded_image_view);

            // 4. 建立 TextView (MyDomesticHelper_name)
            TextView textView = new TextView(getContext());
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(35)
            );
            textView.setLayoutParams(textParams);
            textView.setGravity(Gravity.CENTER);
            textView.setText("Oliphia");
            textView.setTextSize(16);
            textView.setTextColor(Color.BLACK);

            // 5. 將 ShapeableImageView 和 TextView 加入 LinearLayout
            newLayout.addView(imageView);
            newLayout.addView(textView);

            // 6. 將新建的 LinearLayout 加入 mainContainer
            mainContainer.addView(newLayout);
        } else {
            TextView textView = new TextView(getContext());
            textView.setText("Let's hiring your first Domestic");
            textView.setTextSize(18);
            textView.setPadding(16, 16, 16, 16);
        }

        return view;
    }
}