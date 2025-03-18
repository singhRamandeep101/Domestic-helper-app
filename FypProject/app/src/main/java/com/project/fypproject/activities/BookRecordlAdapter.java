package com.project.fypproject.activities;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.fypproject.R;

import java.util.List;
import java.util.Map;

public class BookRecordlAdapter extends RecyclerView.Adapter<BookRecordlAdapter.ViewHolder> {

    Context context;
    List<Map<String,Object>> recordsList;

    public BookRecordlAdapter(Context context, List<Map<String,Object>> recordsList) {
        this.context = context;
        this.recordsList = recordsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.bookrecord_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Map<String,Object> record = recordsList.get(position);

        holder.tvDate.setText((String) record.get("date"));
        holder.tvTime.setText((String) record.get("startTime") +" To "+ record.get("endTime"));
        holder.tvName.setText((String) record.get("employeeName"));
    }

    @Override
    public int getItemCount() {
        return recordsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime,tvName,tvDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
