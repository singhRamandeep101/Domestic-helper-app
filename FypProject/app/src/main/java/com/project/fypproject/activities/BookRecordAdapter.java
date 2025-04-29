package com.project.fypproject.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project.fypproject.R;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.project.fypproject.activities.employer.HiringStatusActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookRecordAdapter extends RecyclerView.Adapter<BookRecordAdapter.ViewHolder> {

    private List<QueryDocumentSnapshot> bookingList;
    private String userType;

    public BookRecordAdapter(List<QueryDocumentSnapshot> bookingList,String userType) {
        this.bookingList = bookingList;
        this.userType = userType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookrecord_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QueryDocumentSnapshot document = bookingList.get(position);

        holder.tvHelperName.setText(document.getString("employeeName")+" (Domestic Helper)");
        holder.tvEmployerName.setText(document.getString("employerName")+" (Employer)");
        holder.tvAgentName.setText(document.getString("agentName")+" (Agent)");
        holder.tvTranName.setText(document.getString("translatorName")+" (Translator)");
        holder.tvDate.setText(document.getString("date"));
        holder.tvTime.setText(document.getString("timeSlot"));
        holder.bookRecordID.setText("Booking ID: " + document.getString("bookingID"));
        holder.bookStatus.setText(document.getString("meetingStatus"));

        String dateStr = document.getString("date");
        String timeSlotStr = document.getString("timeSlot");
        String startTime = timeSlotStr.split("-")[0].trim();
        String dateTimeStr = dateStr.replaceAll("\\(.*\\)", "").trim() + " " + startTime;

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.ENGLISH);
        boolean isMeetingTime = false;
        try {
            Date meetingStart = sdf.parse(dateTimeStr);
            Date now = new Date();
            if (now.after(meetingStart) || now.equals(meetingStart)) {
                isMeetingTime = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String meetingStatus = document.getString("meetingStatus");
        String bookingID = document.getString("bookingID");
        String meetingID = document.getString("meetingID");

        if ("Waiting for interview".equals(meetingStatus)) {
            holder.stateIcon.setImageResource(R.drawable.icon_wait);
            holder.bookStatus.setTextColor(android.graphics.Color.parseColor("#42A5F5"));
        } else if ("Started interview".equals(meetingStatus)) {
            holder.stateIcon.setImageResource(R.drawable.icon_start);
            holder.bookStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        } else if ("Waiting for Employer Response".equals(meetingStatus)) {
            if(userType.equals("Employer")){
                holder.stateIcon.setImageResource(R.drawable.ic_action);
                holder.bookStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
            }else{
                holder.stateIcon.setImageResource(R.drawable.ic_pending);
                holder.bookStatus.setTextColor(android.graphics.Color.parseColor("#6B7280"));
            }
        }else if ("Waiting for Agent Confirm".equals(meetingStatus)) {
            if(userType.equals("Agent")){
                holder.stateIcon.setImageResource(R.drawable.ic_action);
                holder.bookStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
            }else{
                holder.stateIcon.setImageResource(R.drawable.ic_pending);
                holder.bookStatus.setTextColor(android.graphics.Color.parseColor("#6B7280"));
            }
        }else if ("Confirmed".equals(meetingStatus)) {
                holder.stateIcon.setImageResource(R.drawable.icon_confirm);
                holder.bookStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        }else{
            holder.stateIcon.setImageResource(R.drawable.ic_rej);
            holder.bookStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
        }

        if ("Waiting for interview".equals(meetingStatus) && userType.equals("Agent") && isMeetingTime) {
            holder.btnJoin.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnHire.setVisibility(View.GONE);
            holder.btnNotHiring.setVisibility(View.GONE);
            holder.btnConfirm.setVisibility(View.GONE);
        } else if ("Waiting for interview".equals(meetingStatus) && userType.equals("Agent")) {
            holder.btnJoin.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnHire.setVisibility(View.GONE);
            holder.btnNotHiring.setVisibility(View.GONE);
            holder.btnConfirm.setVisibility(View.GONE);
        } else if ("Started interview".equals(meetingStatus)) {
            holder.btnJoin.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.GONE);
            holder.btnHire.setVisibility(View.GONE);
            holder.btnNotHiring.setVisibility(View.GONE);
            holder.btnConfirm.setVisibility(View.GONE);

        }else if("Waiting for Employer Response".equals(meetingStatus)) {
            if (userType.equals("Employer")) {
                holder.btnJoin.setVisibility(View.GONE);
                holder.btnCancel.setVisibility(View.GONE);
                holder.btnHire.setVisibility(View.VISIBLE);
                holder.btnNotHiring.setVisibility(View.VISIBLE);
                holder.btnConfirm.setVisibility(View.GONE);
            }else{
                holder.btnJoin.setVisibility(View.GONE);
                holder.btnCancel.setVisibility(View.GONE);
                holder.btnHire.setVisibility(View.GONE);
                holder.btnNotHiring.setVisibility(View.GONE);
                holder.btnConfirm.setVisibility(View.GONE);
            }
        }else if("Waiting for Agent Confirm".equals(meetingStatus)){
            if (userType.equals("Agent")) {
                holder.btnJoin.setVisibility(View.GONE);
                holder.btnCancel.setVisibility(View.GONE);
                holder.btnHire.setVisibility(View.GONE);
                holder.btnNotHiring.setVisibility(View.GONE);
                holder.btnConfirm.setVisibility(View.VISIBLE);
            }else{
                holder.btnJoin.setVisibility(View.GONE);
                holder.btnCancel.setVisibility(View.GONE);
                holder.btnHire.setVisibility(View.GONE);
                holder.btnNotHiring.setVisibility(View.GONE);
                holder.btnConfirm.setVisibility(View.GONE);
            }
        }else{
            holder.btnJoin.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
            holder.btnHire.setVisibility(View.GONE);
            holder.btnNotHiring.setVisibility(View.GONE);
            holder.btnConfirm.setVisibility(View.GONE);
        }

        holder.btnCancel.setOnClickListener(v -> {
            document.getReference().update("meetingStatus", "Cancel interview")
                    .addOnSuccessListener(aVoid -> {

                    })
                    .addOnFailureListener(e -> {
                        android.widget.Toast.makeText(holder.itemView.getContext(), "Failed to cancel booking", android.widget.Toast.LENGTH_SHORT).show();
                    });
        });

        holder.btnHire.setOnClickListener(v -> {
            document.getReference().update("meetingStatus", "Waiting for Agent Confirm")
                    .addOnSuccessListener(aVoid -> {

                    })
                    .addOnFailureListener(e -> {
                        android.widget.Toast.makeText(holder.itemView.getContext(), "Failed to Hire", android.widget.Toast.LENGTH_SHORT).show();
                    });
        });

        holder.btnNotHiring.setOnClickListener(v -> {
            document.getReference().update("meetingStatus", "Declined")
                    .addOnSuccessListener(aVoid -> {

                    })
                    .addOnFailureListener(e -> {
                        android.widget.Toast.makeText(holder.itemView.getContext(), "Failed to Not Hire", android.widget.Toast.LENGTH_SHORT).show();
                    });
        });

        holder.btnConfirm.setOnClickListener(v -> {

                        android.content.Context context = holder.itemView.getContext();
                        android.content.Intent intent = new android.content.Intent(context, HiringManagementActivity.class);
                        intent.putExtra("email", document.getString("employeeEmail"));
                        intent.putExtra("employerEmail", document.getString("employerEmail"));
                        intent.putExtra("bookID", document.getString("bookingID"));
                        context.startActivity(intent);
        });

        holder.btnJoin.setOnClickListener(v -> {
            android.content.Context context = holder.itemView.getContext();
            android.content.Intent intent = new android.content.Intent(context, MeetingActivity.class);
            intent.putExtra("bookingID", document.getString("bookingID"));
            context.startActivity(intent);
        });

        holder.detailIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                android.content.Context context = holder.itemView.getContext();
                android.content.Intent intent = new android.content.Intent(context, BookingDetailActivity.class);
                intent.putExtra("bookingID", document.getString("bookingID"));
                intent.putExtra("date", document.getString("date"));
                intent.putExtra("timeSlot", document.getString("timeSlot"));
                intent.putExtra("meetingStatus", document.getString("meetingStatus"));
                intent.putExtra("agentName", document.getString("agentName"));
                intent.putExtra("employerName", document.getString("employerName"));
                intent.putExtra("employeeName", document.getString("employeeName"));
                intent.putExtra("translatorName", document.getString("translatorName"));
                intent.putExtra("agentEmail", document.getString("agentEmail"));
                intent.putExtra("employerEmail", document.getString("employerEmail"));
                intent.putExtra("employeeEmail", document.getString("employeeEmail"));
                intent.putExtra("translatorEmail", document.getString("translatorEmail"));
                intent.putExtra("userType", userType);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHelperName, tvEmployerName, tvAgentName, tvTranName, tvDate, tvTime, bookRecordID, bookStatus;
        Button btnJoin, btnCancel,btnHire,btnNotHiring,btnConfirm;
        ImageView stateIcon,detailIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHelperName = itemView.findViewById(R.id.tvHelperName);
            tvEmployerName = itemView.findViewById(R.id.tvEmployerName);
            tvAgentName = itemView.findViewById(R.id.tvAgentName);
            tvTranName = itemView.findViewById(R.id.tvTranName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            bookRecordID = itemView.findViewById(R.id.bookRecordID);
            bookStatus = itemView.findViewById(R.id.booktatus);
            btnJoin = itemView.findViewById(R.id.btnJoin);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            stateIcon = itemView.findViewById(R.id.stateIcon);
            detailIcon = itemView.findViewById(R.id.DetailIcon);
            btnHire = itemView.findViewById(R.id.btnHire);
            btnNotHiring = itemView.findViewById(R.id.btnNotHiring);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
        }
    }
}