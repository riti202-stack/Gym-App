package com.example.gymapp.adapters;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gymapp.R;
import com.example.gymapp.models.Payment;

import java.util.ArrayList;
import java.util.List;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.ViewHolder> {
    private Cursor cursor;
    private List<? extends Object> payments;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_row, parent, false);
        return new ViewHolder(view);
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments != null ? payments : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (cursor == null || cursor.isClosed() || !cursor.moveToPosition(position)) {
            holder.bind("No data", "N/A", "৳0", "N/A", "Regular");
            return;
        }

        try {
            int memberIdIdx = cursor.getColumnIndex("member_id");
            int memberNameIdx = cursor.getColumnIndex("member_name");
            int amountIdx = cursor.getColumnIndex("amount");
            int dateIdx = cursor.getColumnIndex("paid_date");
            int statusIdx = cursor.getColumnIndex("status");

            String memberId = memberIdIdx != -1 ? String.valueOf(cursor.getInt(memberIdIdx)) : "N/A";
            String memberName = memberNameIdx != -1 ? cursor.getString(memberNameIdx) : "Unknown";
            String amount = amountIdx != -1 ? String.valueOf(cursor.getDouble(amountIdx)) : "0";
            String date = dateIdx != -1 ? cursor.getString(dateIdx) : "N/A";
            String status = statusIdx != -1 ? cursor.getString(statusIdx) : "Regular";

            holder.bind(memberId, memberName, "৳" + amount, date, status);

        } catch (Exception e) {
            holder.bind("N/A", "Error loading data", "৳0", "N/A", "Regular");
        }
    }

    @Override
    public int getItemCount() {
        return cursor != null && !cursor.isClosed() ? cursor.getCount() : 0;
    }

    public void updateData(Cursor newCursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView memberIdText, memberNameText, amountText, dateText, statusText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberIdText = itemView.findViewById(R.id.paymentMemberId);
            memberNameText = itemView.findViewById(R.id.paymentMemberName);
            amountText = itemView.findViewById(R.id.paymentAmount);
            dateText = itemView.findViewById(R.id.paymentDate);
            statusText = itemView.findViewById(R.id.paymentStatus);
        }

        public void bind(String memberId, String memberName, String amount, String date, String status) {
            memberIdText.setText("ID: " + memberId);
            memberNameText.setText(memberName);
            amountText.setText(amount);
            dateText.setText(date);
            statusText.setText(status);
        }
    }
}
