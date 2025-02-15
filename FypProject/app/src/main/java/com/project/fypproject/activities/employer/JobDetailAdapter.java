package com.project.fypproject.activities.employer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;
import com.project.fypproject.R;

public class JobDetailAdapter extends RecyclerView.Adapter<JobDetailAdapter.ViewHolder> {

    Context context;
    List<Map<String,Object>> dutiesList;

    public JobDetailAdapter(Context context, List<Map<String,Object>> dutiesList) {
        this.context = context;
        this.dutiesList = dutiesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.jobdetail_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Map<String,Object> duty = dutiesList.get(position);

        holder.tvTitle.setText("Previous Duties" + (position + 1));
        holder.tvWorkingCountry.setText((String) duty.get("workingCountry"));
        holder.tvSalary.setText((String) duty.get("salary"));

        String duration = (String) duty.get("Duration");
        String startDate = (String) duty.get("startDate");
        String endDate = (String) duty.get("endDate");
        String[] durations = duration.split(" ");
        String year = durations[0];
        String month = durations[1];

        int years = Integer.parseInt(year.replace("year", ""));
        int months = Integer.parseInt(month.replace("month", ""));

        if (years > 0 && months > 0) {
            holder.tvDuration.setText(startDate +" To " + endDate +"(" +years + " Year " + months + " Month)");
        } else if (years > 0) {
            holder.tvDuration.setText(startDate +" To " + endDate +"(" + years + " Year)");
        } else{
            holder.tvDuration.setText(startDate +" To " + endDate +"(" + months + " Month)");
        }

        holder.tvReasonLeave.setText((String) duty.get("reasonToLeave"));
        holder.glWork.removeAllViews();
        for(Map.Entry<String,Object> entry: duty.entrySet()){
            if(entry.getValue() instanceof Boolean && (Boolean) entry.getValue()){
                String text = entry.getKey();

                if("CareOfBabies".equals(entry.getKey())){
                    List<String> babiesAges = (List<String>) duty.get("babiesAges");
                    String babiesAgesS = String.join(", ",babiesAges);
                    text += " (" + babiesAgesS + " Mths)";
                }

                if("CareOfToddler(1-3)".equals(entry.getKey())){
                    List<String> toddlerAges = (List<String>) duty.get("toddlersAges");
                    String toddlerAgesS = String.join(", ",toddlerAges);
                    text += " (" + toddlerAgesS + " Yrs)";
                }

                if("CareOfChildren(4-12)".equals(entry.getKey())){
                    List<String> childrenAges = (List<String>) duty.get("childrenAges");
                    String childrenAgesS = String.join(", ",childrenAges);
                    text += " (" + childrenAgesS + " Yrs)";
                }

                if("CareOfDisabled".equals(entry.getKey())){
                    List<String> disabledAges = (List<String>) duty.get("disabledAges");
                    String disabledAgesS = String.join(", ",disabledAges);
                    text += " (" + disabledAgesS + " Yrs)";
                }

                if("CareOfElderly".equals(entry.getKey())){
                    List<String> elderlyAges = (List<String>) duty.get("elderlyAges");
                    String elderlyAgesS = String.join(", ",elderlyAges);
                    text += " (" + elderlyAgesS + " Yrs)";
                }
                TextView textView = new TextView(context);
                textView.setText(text);
                textView.setTextSize(18);
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.parseColor("#DEA1A1"));
                textView.setPadding(30, 30, 30, 30);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
                params.setMargins(10, 10, 10, 10);
                textView.setLayoutParams(params);

                holder.glWork.addView(textView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return dutiesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle,tvWorkingCountry,tvSalary,tvDuration,tvNoOfServe,tvReasonLeave;
        GridLayout glWork;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvWorkingCountry = itemView.findViewById(R.id.tvWorkingCountry);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvNoOfServe = itemView.findViewById(R.id.tvNoOfServe);
            tvReasonLeave = itemView.findViewById(R.id.tvReasonLeave);
            glWork = itemView.findViewById(R.id.glWork);
        }
    }
}
