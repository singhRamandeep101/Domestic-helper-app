package com.project.fypproject.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.R;

import java.util.List;
import java.util.Map;

public class BookRecordlAdapter extends RecyclerView.Adapter<BookRecordlAdapter.ViewHolder> {

    Context context;
    List<Map<String,Object>> recordsList;
    String userType;

    public BookRecordlAdapter(Context context, List<Map<String,Object>> recordsList,String userType) {
        this.context = context;
        this.recordsList = recordsList;
        this.userType = userType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.bookrecord_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Map<String, Object> record = recordsList.get(position);

        holder.tvDate.setText((String) record.get("date"));
        holder.tvTime.setText((String) record.get("startTime") + " To " + record.get("endTime"));
        holder.tvName.setText((String) record.get("employeeName"));
        if ("Agent".equalsIgnoreCase(userType)) {
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }
        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this booking?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String documentId = (String) record.get("documentId");
                                if (documentId != null) {
                                    FirebaseFirestore.getInstance().collection("bookings")
                                            .document(documentId)
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    recordsList.remove(position);
                                                    notifyItemRemoved(position);
                                                    notifyItemRangeChanged(position, recordsList.size());
                                                    Toast.makeText(context, "Booking deleted successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // 提示删除失败并打印日志
                                                    Toast.makeText(context, "Failed to delete booking", Toast.LENGTH_SHORT).show();
                                                    Log.e("Firestore", "Error deleting record", e);
                                                }
                                            });
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime,tvName,tvDate;
        Button btnCancel;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvName = itemView.findViewById(R.id.tvName);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
