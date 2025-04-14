package com.project.fypproject.activities.employer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.fypproject.activities.ResumeDetailActivity;
import com.project.fypproject.models.HelperInfo;

import com.project.fypproject.R;
import java.util.List;

public class JobListAdapter extends RecyclerView.Adapter<JobListAdapter.ViewHolder> {

    Context context;
    List<HelperInfo> helperInfoList;

    public JobListAdapter(Context context, List<HelperInfo> helperInfoList) {
        this.context = context;
        this.helperInfoList = helperInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.joblist_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HelperInfo helperInfo = helperInfoList.get(position);

        // 確保使用正確的屬性名稱獲取圖片 URL
        String imageUrl = helperInfo.getImage_url(); // 或 helperInfo.getImg_url()，取決於 HelperInfo 類的定義

        // 清除舊圖片以防重複使用 ViewHolder 導致圖片錯亂
        holder.imgUserIcon.setImageDrawable(null);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("gs://")) {
                // 處理 Firebase Storage URL
                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(context)
                            .load(uri)
                            .placeholder(R.drawable.default_profile) // 添加佔位圖
                            .error(R.drawable.default_profile) // 添加錯誤圖
                            .into(holder.imgUserIcon);
                }).addOnFailureListener(e -> {
                    holder.imgUserIcon.setImageResource(R.drawable.default_profile);
                    Log.e("Glide", "Failed to load image from Firebase Storage", e);
                });
            } else {
                // 直接加載 HTTP/HTTPS URL
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(holder.imgUserIcon);
            }
        } else {
            // 沒有圖片 URL，使用默認圖片
            holder.imgUserIcon.setImageResource(R.drawable.default_profile);
        }

        // 設置其他文本內容
        holder.name.setText(helperInfo.getName());
        holder.age.setText("("+ helperInfo.getAge()+"yr)");
        holder.nation.setText(helperInfo.getNationality());
        holder.zodiac.setText(helperInfo.getZodiac());
        holder.religion.setText(helperInfo.getReligion());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ResumeDetailActivity.class);
            intent.putExtra("email", helperInfo.getEmail());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return helperInfoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUserIcon;
        TextView name, age, nation, zodiac, religion;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgUserIcon = itemView.findViewById(R.id.imgUser);
            name = itemView.findViewById(R.id.tvName);
            age = itemView.findViewById(R.id.tvAge);
            nation = itemView.findViewById(R.id.tvNationality);
            zodiac = itemView.findViewById(R.id.tvZodiac);
            religion = itemView.findViewById(R.id.tvReligion);
        }
    }
}
