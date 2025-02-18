package com.project.fypproject.activities.employer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.R;
import com.project.fypproject.activities.ProfileEditingFragment;


public class ProfileFragment extends Fragment {

    TextView userRole, userName, email, telephone;
    ImageView btnEdit;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        userRole = view.findViewById(R.id.userRole);
        userName = view.findViewById(R.id.userName);
        email = view.findViewById(R.id.email);
        telephone = view.findViewById(R.id.telephone);

        btnEdit = view.findViewById(R.id.btn_edit);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userName.setText(document.getString("firstName") + " " + document.getString("lastName"));
                        userRole.setText(document.getString("userType"));
                        email.setText(document.getString("email"));
                        telephone.setText("+852 "+ document.getString("telephone"));
                    }
                }
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 假設你想要取代目前容器中的 Fragment
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragmentContainer, new ProfileEditingFragment()).commit();
            }
        });

        return view;
    }
}