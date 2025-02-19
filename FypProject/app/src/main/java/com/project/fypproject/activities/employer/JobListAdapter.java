package com.project.fypproject.activities.employer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.fypproject.models.MaidInfo;

import com.project.fypproject.R;
import java.util.List;
import java.util.Map;

public class JobListAdapter extends RecyclerView.Adapter<JobListAdapter.ViewHolder> {

    Context context;
    List<MaidInfo> maidInfoList;
    Map<String, Object> overseasExperience;
    String[] locatoin = {"hong_kong", "singapore", "taiwan", "malaysia", "middle_east", "macau", "other", "home_country"};

    public JobListAdapter(Context context, List<MaidInfo> maidInfoList) {
        this.context = context;
        this.maidInfoList = maidInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.joblist_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        MaidInfo maidInfo = maidInfoList.get(position);

//        Glide.with(context).load(maidInfo.getImg_url()).into(holder.imageView);

        String firstName = "Maria";
        String lastName = "Ceres";

        if (maidInfo.getFirstName() != null)
            firstName = maidInfo.getFirstName();
        if (maidInfo.getLastName() != null)
            lastName = maidInfo.getLastName();

        holder.name.setText(firstName +" "+ lastName);
        holder.age.setText("("+ maidInfo.getAge()+" years)");
        holder.nationality.setText(maidInfo.getNationality());
        holder.religion.setText(maidInfo.getReligion());

        overseasExperience = maidInfo.getOverseasExperience();

        int years = 0;
        int months = 0;

        if (overseasExperience != null){
            for (Object value : overseasExperience.values()) {
                if (value.toString().contains("year")) {
                    years += Integer.parseInt(value.toString().replaceAll("[^0-9]", ""));
                } else if (value.toString().contains("months")) {
                    months += Integer.parseInt(value.toString().replaceAll("[^0-9]", ""));
                    if (months >= 12){
                        years += 1;
                        months -= 12;
                    }
                }
            }
        }

        if(years > 0 && months >0){
            holder.workExp.setText(years + " year " + months +" month experience");
        }else if(years > 0){
            holder.workExp.setText(years + " year experience");
        }else if(months > 0){
            holder.workExp.setText(months + " month experience");
        }else{
            holder.workExp.setText("No experience");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, JobDetailActivity.class);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return maidInfoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name, age, nationality, workExp, religion;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imgUser);
            name = itemView.findViewById(R.id.tvName);
            age = itemView.findViewById(R.id.tvAge);
            nationality = itemView.findViewById(R.id.tvNationality);
            religion = itemView.findViewById(R.id.tvReligion);
            workExp = itemView.findViewById(R.id.tvWork);

        }
    }
}
