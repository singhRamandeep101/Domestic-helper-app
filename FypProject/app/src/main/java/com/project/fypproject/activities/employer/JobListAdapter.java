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

import com.bumptech.glide.Glide;
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

//        Glide.with(context).load(helperInfo.getImg_url()).into(holder.imageView);
        holder.name.setText(helperInfo.getName());
        holder.age.setText("("+ helperInfo.getAge()+"yr)");
        holder.nation.setText(helperInfo.getNationality());
        holder.zodiac.setText(helperInfo.getZodiac());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ResumeDetailActivity.class);
                intent.putExtra("email",helperInfo.getEmail());
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
        TextView name, age, nation, zodiac;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imgUser);
            name = itemView.findViewById(R.id.tvName);
            age = itemView.findViewById(R.id.tvAge);
            nation = itemView.findViewById(R.id.tvNationality);
            zodiac = itemView.findViewById(R.id.tvZodiac);
        }
    }
}
