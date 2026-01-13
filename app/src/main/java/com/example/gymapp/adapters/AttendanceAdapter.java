package com.example.gymapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymapp.R;
import com.example.gymapp.models.Attendance;

import java.util.ArrayList;
import java.util.List;

// AttendanceAdapter.java
public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
    private List<Attendance> attendance = new ArrayList<>();

    public void setAttendance(List<Attendance> attendance) {
        this.attendance = attendance;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendance item = attendance.get(position);
        holder.date.setText(item.getDate());
        holder.status.setText(item.getStatus());
    }

    @Override
    public int getItemCount() { return attendance.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView date, status;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            status = itemView.findViewById(R.id.status);
        }
    }
}

// attendance_item.xml

