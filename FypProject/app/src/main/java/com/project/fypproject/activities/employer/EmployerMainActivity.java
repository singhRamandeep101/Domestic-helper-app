package com.project.fypproject.activities.employer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.project.fypproject.R;
import com.project.fypproject.activities.AgentHomeFragment;
import com.project.fypproject.activities.Login;

public class EmployerMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    TextView txtUserName, txtUserType;

    private DrawerLayout drawerLayout;
    FirebaseAuth auth;
    FirebaseUser user;

    String userType = "";

    public void SelectUserTypeHomeFragment(String userType){
        switch (userType){
            case "Employer":
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new EmployerHomeFragment()).commit();
                break;
            case "Agent":
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new AgentHomeFragment()).commit();
                break;
            default:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new EmployerHomeFragment()).commit();
                break;
        }
    }

    public void loadData(NavigationView navigationView){
        View headerView = navigationView.getHeaderView(0);
        txtUserName = headerView.findViewById(R.id.userName);
        txtUserType = headerView.findViewById(R.id.userType);
    }

    public void ChangeHomeActivity(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userType = document.getString("userType");
                        txtUserName.setText(document.getString("firstName") + " " + document.getString("lastName"));
                        txtUserType.setText(userType);
                        SelectUserTypeHomeFragment(userType);
                    } else {
                        userType = "";
                    }
                } else {
                    userType = "";
                }
            }
        });
    }

    public void ReloadProfileActivity(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userType = document.getString("userType");
                        txtUserName.setText(document.getString("firstName") + " " + document.getString("lastName"));
                        txtUserType.setText(userType);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new ProfileFragment()).commit();
                    } else {
                        userType = "";
                    }
                } else {
                    userType = "";
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employer_main);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawableLayout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Load the user name and the user type
        loadData(navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        if(savedInstanceState == null){
            ChangeHomeActivity();
            navigationView.setCheckedItem(R.id.nav_home);
        }



    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            ChangeHomeActivity();
        } else if (id == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new ProfileFragment()).commit();
        } else if (id == R.id.nav_language) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new LanguageFragment()).commit();
        } else if (id == R.id.nav_notification) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new NotificationFragment()).commit();
        } else if (id == R.id.nav_appointment) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new ApointmentFragment()).commit();
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onBackPressed(){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);;
        } else {
            super.onBackPressed();
        }
    }
}