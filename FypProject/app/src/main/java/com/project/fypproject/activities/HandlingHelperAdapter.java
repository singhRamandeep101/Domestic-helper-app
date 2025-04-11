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

import com.project.fypproject.R;
import com.project.fypproject.models.HelperInfo;

import java.util.List;

public class HandlingHelperAdapter extends RecyclerView.Adapter<HandlingHelperAdapter.ViewHolder>{

    Context context;
    List<HelperInfo> helperInfoList;

    public HandlingHelperAdapter(Context context, List<HelperInfo> helperInfoList) {
        this.context = context;
        this.helperInfoList = helperInfoList;
    }

    @NonNull
    @Override
    public HandlingHelperAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HandlingHelperAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.joblist_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull HandlingHelperAdapter.ViewHolder holder, int position) {

        HelperInfo helperInfo = helperInfoList.get(position);

//        Glide.with(context).load(helperInfo.getImg_url()).into(holder.imageView);
        holder.name.setText(helperInfo.getName());
        holder.age.setText("("+ helperInfo.getAge()+"yr)");
        holder.nation.setText(helperInfo.getNationality());
        holder.zodiac.setText(helperInfo.getZodiac());
        holder.religion.setText(helperInfo.getReligion());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HiringManagementActivity.class);
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
