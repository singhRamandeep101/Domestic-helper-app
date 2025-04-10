package com.project.fypproject.activities;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.fypproject.R;

import java.util.HashMap;
import java.util.List;

public class BookingRequestAdapter extends RecyclerView.Adapter<BookingRequestAdapter.ViewHolder> {

    private List<HashMap<String, String>> bookingRequests;

    public BookingRequestAdapter(List<HashMap<String, String>> bookingRequests) {
        this.bookingRequests = bookingRequests;
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
        holder.userStatus.setText(request.get("userStatus"));

        switch (request.get("userStatus")) {
            case "Action Required":
                holder.userStatus.setTextColor(Color.parseColor("#F44336"));
                break;
            case "Confirmed":
                holder.userStatus.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case "Rejected":
                holder.userStatus.setTextColor(Color.parseColor("#F44336"));
                break;
            case "Awaiting Action":
                holder.userStatus.setTextColor(Color.parseColor("#FF9800"));
                break;
            case "Completed":
                holder.userStatus.setTextColor(Color.parseColor("#2196F3"));
                break;
            default:
                holder.userStatus.setTextColor(Color.parseColor("#9E9E9E"));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return bookingRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHelperName, tvUserName, userStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHelperName = itemView.findViewById(R.id.tvHelperName);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            userStatus = itemView.findViewById(R.id.userStatus);
        }
    }
}