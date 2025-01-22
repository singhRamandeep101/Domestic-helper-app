package com.project.fypproject.activities;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

//        Glide.with(context).load(helperInfo.getImg_url()).into(holder.imageView);
        holder.name.setText(helperInfo.getLastName() +" "+ helperInfo.getFirstName());
        holder.age.setText("("+ helperInfo.getAge()+"yr)");
        holder.nation.setText(helperInfo.getNationality());

        String experience = helperInfo.getTotalExperienceDuration();
        String[] exp = experience.split(" ");
        String year = exp[0];
        String month = exp[1];

        int years = Integer.parseInt(year.replace("year", ""));
        int months = Integer.parseInt(month.replace("month",  ""));

        if(years > 0 && months >0){
            holder.workExp.setText(years + " year " + months +" month experience");
        }else if(years > 0){
            holder.workExp.setText(years + " year experience");
        }else if(months > 0){
            holder.workExp.setText(months + " month experience");
        }else{
            holder.workExp.setText("No experience");
        }

        holder.postDate.setText("PostDate:"+ helperInfo.getPostTime().split(" ")[0]);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, JobDetailActivity.class);
                intent.putExtra("userEmail",helperInfo.getUserEmail());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return helperInfoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name, age, nation, workExp, postDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imgUser);
            name = itemView.findViewById(R.id.tvName);
            age = itemView.findViewById(R.id.tvAge);
            nation = itemView.findViewById(R.id.tvNation);
            workExp = itemView.findViewById(R.id.tvWork);
            postDate = itemView.findViewById(R.id.tvPostDate);
        }
    }
}
