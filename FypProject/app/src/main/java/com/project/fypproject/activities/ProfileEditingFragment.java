package com.project.fypproject.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.R;
import com.project.fypproject.activities.employer.EmployerMainActivity;
import com.project.fypproject.activities.employer.ProfileFragment;

public class ProfileEditingFragment extends Fragment {

    TextInputEditText userRole, firstName, lastName, email, telephone;
    Button btnUpdate;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_editing, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        userRole = view.findViewById(R.id.userRole);
        firstName = view.findViewById(R.id.FirstName);
        lastName = view.findViewById(R.id.LastName);
        email = view.findViewById(R.id.email);
        telephone = view.findViewById(R.id.telephone);

        btnUpdate = view.findViewById(R.id.btn_update);

        userRole.setEnabled(false);
        email.setEnabled(false);

        //get the existing information
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        firstName.setText(document.getString("firstName"));
                        lastName.setText(document.getString("lastName"));
                        userRole.setText(document.getString("userType"));
                        email.setText(document.getString("email"));
                        telephone.setText(document.getString("telephone"));
                    }
                }
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newFirstName, newLastName, newTelephone;
                newFirstName = String.valueOf(firstName.getText());
                newLastName = String.valueOf(lastName.getText());
                newTelephone = String.valueOf(telephone.getText());

                if(TextUtils.isEmpty(newFirstName)){
                    Toast.makeText(getActivity(), "Enter your first name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(newLastName)){
                    Toast.makeText(getActivity(), "Enter your last name", Toast.LENGTH_SHORT).show();
                    return;
                }

                docRef.update(
                        "firstName", newFirstName,
                        "lastName", newLastName,
                        "telephone", newTelephone)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                EmployerMainActivity activity = (EmployerMainActivity) getActivity();
                                if (activity != null) {
                                    activity.ReloadProfileActivity(); // 呼叫 Activity 中的方法
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });


            }
        });

        return view;
    }
}