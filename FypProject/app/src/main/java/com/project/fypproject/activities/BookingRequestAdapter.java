package com.project.fypproject.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.fypproject.R;

import java.util.HashMap;
import java.util.List;

public class BookingRequestAdapter extends RecyclerView.Adapter<BookingRequestAdapter.ViewHolder> {

    private List<HashMap<String, String>> bookingRequests;
    Context context;
    String userState;

    public BookingRequestAdapter(Context context,List<HashMap<String, String>> bookingRequests) {
        this.bookingRequests = bookingRequests;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.booking_requestlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, String> request = bookingRequests.get(position);
        holder.tvHelperName.setText(request.get("helperName"));
        holder.tvUserName.setText(request.get("userName"));

        if(request.get("userType").equals("Agent") && request.get("agentState").equals("Awaiting Both")){
            holder.userStatus.setText("Awaiting Both");
            userState = "Awaiting Both";
            holder.userStatus.setTextColor(Color.parseColor("#6B7280"));
            holder.stateIcon.setImageResource(R.drawable.ic_pending);

        }else if (request.get("userType").equals("Agent")
                && request.get("employeeState").equals("DomesticHelper Confirmed")
                && request.get("translatorState").equals("Action Required")){
                holder.userStatus.setText("Awaiting Translator");
                holder.userStatus.setTextColor(Color.parseColor("#6B7280"));
                holder.stateIcon.setImageResource(R.drawable.ic_pending);
                userState = "Awaiting Translator";

        }else if (request.get("userType").equals("Agent")
                && request.get("employeeState").equals("Action Required")
                && request.get("translatorState").equals("Translator Confirmed")){
                holder.userStatus.setText("Awaiting DomesticHelper");
                holder.userStatus.setTextColor(Color.parseColor("#6B7280"));
                holder.stateIcon.setImageResource(R.drawable.ic_pending);
                userState = "Awaiting DomesticHelper";

        }else if (request.get("userType").equals("Agent")
                && request.get("employeeState").equals("DomesticHelper Confirmed")
                && request.get("translatorState").equals("Translator Confirmed")) {
            holder.userStatus.setText("Action Required");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_action);
            userState = "Action Required";

        }else if (request.get("userType").equals("Agent") && request.get("employeeState").equals("DomesticHelper Decline")){
            holder.userStatus.setText("DomesticHelper Decline");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_rej);
            userState = "DomesticHelper Decline";

        }else if (request.get("userType").equals("Agent") && request.get("translatorState").equals("Translator Decline")){
            holder.userStatus.setText("Translator Decline");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_rej);
            userState = "Translator Decline";


        }else if (request.get("userType").equals("Agent")
                && request.get("employeeState").equals("Translator Decline")
                && request.get("translatorState").equals("DomesticHelper Decline")) {
            holder.userStatus.setText("Both Declined");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_rej);
            userState = "Both Declined";

        }else if (request.get("userType").equals("Agent") && request.get("agentState").equals("Agent Decline")) {
            holder.userStatus.setText("Declined");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_rej);
            userState = "Declined";

        }else if (request.get("userType").equals("Agent")
                && request.get("agentState").equals("Final Time Confirmed")) {
            holder.userStatus.setText("Final Time Confirmed");
            holder.userStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.stateIcon.setImageResource(R.drawable.icon_confirm);
            userState = "Final Time Confirmed";

        }else if (request.get("userType").equals("Employer")
                && request.get("employerState").equals("Pending Confirmation")) {
            holder.userStatus.setText("Pending Confirmation");
            holder.userStatus.setTextColor(Color.parseColor("#6B7280"));
            holder.stateIcon.setImageResource(R.drawable.ic_pending);
            userState = "Pending Confirmation";

        }else if (request.get("userType").equals("Employer")
                && request.get("employerState").equals("Declined")) {
            holder.userStatus.setText("Declined");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_rej);
            userState = "Declined";

        }else if (request.get("userType").equals("Employer")
                && request.get("employerState").equals("Final Time Confirmed")) {
            holder.userStatus.setText("Final Time Confirmed");
            holder.userStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.stateIcon.setImageResource(R.drawable.icon_confirm);
            userState = "Final Time Confirmed";


        }else if (request.get("userType").equals("Employee")
                && request.get("employeeState").equals("Action Required")) {
            holder.userStatus.setText("Action Required");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_action);
            userState = "Action Required";

        }else if (request.get("userType").equals("Employee")
                && request.get("employeeState").equals("DomesticHelper Confirmed")
                && request.get("translatorState").equals("Action Required")){
            holder.userStatus.setText("Awaiting Translator");
            holder.userStatus.setTextColor(Color.parseColor("#6B7280"));
            holder.stateIcon.setImageResource(R.drawable.ic_pending);
            userState = "Awaiting Translator";

        }else if (request.get("userType").equals("Employee")
                && request.get("employeeState").equals("DomesticHelper Confirmed")
                && request.get("translatorState").equals("Translator Confirmed")){
            holder.userStatus.setText("Awaiting Agent");
            holder.userStatus.setTextColor(Color.parseColor("#6B7280"));
            holder.stateIcon.setImageResource(R.drawable.ic_pending);
            userState = "Awaiting Agent";

        }else if (request.get("userType").equals("Employee") && request.get("employeeState").equals("DomesticHelper Decline")){
            holder.userStatus.setText("Declined");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_rej);
            userState = "Declined";

        }else if (request.get("userType").equals("Employee") && request.get("translatorState").equals("Translator Decline")){
            holder.userStatus.setText("Translator Decline");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_rej);
            userState = "Translator Decline";


        }else if (request.get("userType").equals("Employee")
                && request.get("employeeState").equals("Translator Decline")
                && request.get("translatorState").equals("DomesticHelper Decline")) {
            holder.userStatus.setText("Declined");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_rej);
            userState = "Declined";

        }else if (request.get("userType").equals("Employee") && request.get("employeeState").equals("Final Time Confirmed")) {
            holder.userStatus.setText("Final Time Confirmed");
            holder.userStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.stateIcon.setImageResource(R.drawable.icon_confirm);
            userState = "Final Time Confirmed";
        }
        else if (request.get("userType").equals("Translator")
                && request.get("translatorState").equals("Action Required")) {
            holder.userStatus.setText("Action Required");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_action);
            userState = "Action Required";

        }else if (request.get("userType").equals("Translator")
                && request.get("translatorState").equals("Translator Confirmed")
                && request.get("employeeState").equals("Action Required")){
            holder.userStatus.setText("Awaiting DomesticHelper");
            holder.userStatus.setTextColor(Color.parseColor("#6B7280"));
            holder.stateIcon.setImageResource(R.drawable.ic_pending);
            userState = "Awaiting DomesticHelper";

        }else if (request.get("userType").equals("Translator")
                && request.get("employeeState").equals("DomesticHelper Confirmed")
                && request.get("translatorState").equals("Translator Confirmed")){
            holder.userStatus.setText("Awaiting Agent");
            holder.userStatus.setTextColor(Color.parseColor("#6B7280"));
            holder.stateIcon.setImageResource(R.drawable.ic_pending);
            userState = "Awaiting Agent";

        }else if (request.get("userType").equals("Translator") && request.get("employeeState").equals("DomesticHelper Decline")){
            holder.userStatus.setText("DomesticHelper Decline");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_rej);
            userState = "DomesticHelper Decline";

        }else if (request.get("userType").equals("Translator") && request.get("translatorState").equals("Translator Decline")){
            holder.userStatus.setText("Declined");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_rej);
            userState = "Declined";


        }else if (request.get("userType").equals("Translator")
                && request.get("employeeState").equals("Translator Decline")
                && request.get("translatorState").equals("DomesticHelper Decline")) {
            holder.userStatus.setText("Declined");
            holder.userStatus.setTextColor(Color.parseColor("#F44336"));
            holder.stateIcon.setImageResource(R.drawable.ic_rej);
            userState = "Declined";

        }else if (request.get("userType").equals("Translator") && request.get("translatorState").equals("Final Time Confirmed")) {
            holder.userStatus.setText("Final Time Confirmed");
            holder.userStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.stateIcon.setImageResource(R.drawable.icon_confirm);
            userState = "Final Time Confirmed";
        }



        holder.docId.setText("Request ID: "+ request.get("docId"));

        holder.btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Class<?> targetClass;
                if(request.get("userType").equals("Agent")){
                    targetClass = AgentBookRequestDetailActivity.class;
                }else if(request.get("userType").equals("Employer")){
                    targetClass = EmployerBookRequestDetailActivity.class;
                }else if(request.get("userType").equals("Translator")){
                    targetClass = TranslatorBookRequestDetailActivity.class;
                }else{
                    targetClass = EmployeeBookRequestDetailActivity.class;
                }
                Intent intent = new Intent(context, targetClass);
                intent.putExtra("requestID",request.get("docId"));
                intent.putExtra("requestState",userState);
                intent.putExtra("employerName",request.get("userName"));
                intent.putExtra("helperName",request.get("helperName"));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHelperName, tvUserName, userStatus,docId;
        ImageView stateIcon,btnGo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHelperName = itemView.findViewById(R.id.tvHelperName);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            userStatus = itemView.findViewById(R.id.userStatus);
            stateIcon = itemView.findViewById(R.id.stateIcon);
            docId = itemView.findViewById(R.id.bookRequestID);
            btnGo = itemView.findViewById(R.id.btnGo);
        }
    }
}