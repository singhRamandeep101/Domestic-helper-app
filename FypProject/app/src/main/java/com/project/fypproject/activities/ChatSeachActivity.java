package com.project.fypproject.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.fypproject.R;
import com.project.fypproject.models.ChatModel;

public class ChatSeachActivity extends AppCompatActivity {
    EditText edSearch;
    ImageButton btnSearch,btnBack;
    RecyclerView recyclerView;
    ChatSearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_seach);

        edSearch = findViewById(R.id.edSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.rvSearch);

        edSearch.requestFocus();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchEmail = edSearch.getText().toString();
                if(searchEmail.isEmpty()){
                    edSearch.setError("Please Enter Email!");
                    return;
                }
                searchRV(searchEmail);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    void searchRV(String searchEmail){
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Query currentUserQuery = ChatUtil.allUserCollectionReference().whereEqualTo("email",currentUserEmail);
        currentUserQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    DocumentSnapshot currentUserDoc = queryDocumentSnapshots.getDocuments().get(0);
                    String userType = currentUserDoc.getString("userType");

                    final Query[] query = new Query[1];

                    if(userType.equals("Agent")){
                        query[0] = ChatUtil.allUserCollectionReference()
                                .whereGreaterThanOrEqualTo("email",searchEmail)
                                .whereLessThanOrEqualTo("email",searchEmail+'\uf8ff');
                    }else{
                            query[0] = ChatUtil.allUserCollectionReference()
                                    .whereEqualTo("userType","Agent")
                                    .whereGreaterThanOrEqualTo("email",searchEmail)
                                    .whereLessThanOrEqualTo("email",searchEmail+'\uf8ff');
                    }
                    query[0].get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.isEmpty()){
                                findViewById(R.id.tvNoResults).setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }else{
                                findViewById(R.id.tvNoResults).setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);

                                FirestoreRecyclerOptions<ChatModel> options = new FirestoreRecyclerOptions.Builder<ChatModel>().setQuery(query[0],ChatModel.class).build();
                                adapter = new ChatSearchAdapter(options,getApplicationContext());
                                recyclerView.setLayoutManager(new LinearLayoutManager(ChatSeachActivity.this));
                                recyclerView.setAdapter(adapter);
                                adapter.startListening();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.notifyDataSetChanged();
    }
}