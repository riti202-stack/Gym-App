package com.example.gymapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.util.DatabaseHelper;

public class PaymentsActivity extends AppCompatActivity {

    private TextView totalIncomeLabel;
    private RecyclerView paymentsTable;
    private Button refreshBtn, backBtn;
    private DatabaseHelper dbHelper;
    private PaymentsAdapter paymentsAdapter;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_payments);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupRecyclerView();
        setupListeners();
        loadPaymentsData();
        updateTotalIncome();
    }

    private void initViews() {
        totalIncomeLabel = findViewById(R.id.totalIncomeLabel);
        paymentsTable = findViewById(R.id.paymentsTable);
        refreshBtn = findViewById(R.id.refreshBtn);
        backBtn = findViewById(R.id.backBtn);
    }

    private void setupRecyclerView() {
        paymentsTable.setLayoutManager(new LinearLayoutManager(this));
        paymentsAdapter = new PaymentsAdapter();
        paymentsTable.setAdapter(paymentsAdapter);
    }

    private void setupListeners() {
        refreshBtn.setOnClickListener(v -> {
            v.setAlpha(0.7f);
            v.postDelayed(() -> v.setAlpha(1.0f), 150);
            refreshData();
            Toast.makeText(this, "🔄 Payments refreshed", Toast.LENGTH_SHORT).show();
        });

        backBtn.setOnClickListener(v -> {
            v.setAlpha(0.7f);
            v.postDelayed(this::finish, 150);
        });
    }

    public void refreshData() {
        loadPaymentsData();
        updateTotalIncome();
    }

    private void loadPaymentsData() {
        new Thread(() -> {
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String query = """
                    SELECT p.id, p.member_id, COALESCE(m.name, 'No Member') as member_name, 
                           COALESCE(p.amount, 0) as amount, 
                           COALESCE(p.paid_date, 'N/A') as paid_date,
                           CASE WHEN COALESCE(p.amount, 0) > 500 THEN 'Premium' ELSE 'Regular' END as status
                    FROM payments p 
                    LEFT JOIN members m ON p.member_id = m.id 
                    ORDER BY COALESCE(p.paid_date, '1900-01-01') DESC
                    """;
                Cursor cursor = db.rawQuery(query, null);

                mainHandler.post(() -> {
                    paymentsAdapter.updateData(cursor);
                    Toast.makeText(this, "📊 Loaded " + (cursor != null ? cursor.getCount() : 0) + " records", Toast.LENGTH_SHORT).show();
                });
                db.close();
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this, "❌ Payments load failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }

    private void updateTotalIncome() {
        new Thread(() -> {
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT COALESCE(SUM(amount), 0) as total FROM payments", null);
                if (cursor.moveToFirst()) {
                    double total = cursor.getDouble(0);
                    mainHandler.post(() ->
                            totalIncomeLabel.setText(String.format("Total Income: ৳%.0f", total)));
                } else {
                    mainHandler.post(() -> totalIncomeLabel.setText("Total Income: ৳0"));
                }
                if (cursor != null) cursor.close();
                db.close();
            } catch (Exception e) {
                mainHandler.post(() -> totalIncomeLabel.setText("Total Income: ৳0"));
                e.printStackTrace();
            }
        }).start();
    }

    // 🔥 FIXED PaymentsAdapter - CRASH-PROOF
    private static class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.ViewHolder> {
        private Cursor cursor;

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_payment_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (cursor == null || cursor.isClosed() || !cursor.moveToPosition(position)) {
                holder.bind("N/A", "No data", "৳0", "N/A", "Regular");
                return;
            }

            try {
                int memberIdIdx = cursor.getColumnIndex("member_id");
                int memberNameIdx = cursor.getColumnIndex("member_name");
                int amountIdx = cursor.getColumnIndex("amount");
                int dateIdx = cursor.getColumnIndex("paid_date");
                int statusIdx = cursor.getColumnIndex("status");

                String memberId = memberIdIdx != -1 ? String.valueOf(cursor.getInt(memberIdIdx)) : "N/A";
                String memberName = memberNameIdx != -1 ? getSafeString(cursor, memberNameIdx) : "Unknown";
                String amount = amountIdx != -1 ? String.valueOf(cursor.getDouble(amountIdx)) : "0";
                String date = dateIdx != -1 ? getSafeString(cursor, dateIdx) : "N/A";
                String status = statusIdx != -1 ? getSafeString(cursor, statusIdx) : "Regular";

                holder.bind(memberId, memberName, "৳" + amount, date, status);
            } catch (Exception e) {
                holder.bind("N/A", "Error loading data", "৳0", "N/A", "Regular");
            }
        }

        private static String getSafeString(Cursor cursor, int index) {
            try {
                return cursor.getString(index) != null ? cursor.getString(index) : "N/A";
            } catch (Exception e) {
                return "N/A";
            }
        }

        @Override
        public int getItemCount() {
            return cursor != null && !cursor.isClosed() ? cursor.getCount() : 0;
        }

        void updateData(Cursor newCursor) {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            cursor = newCursor;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView memberIdText, memberNameText, amountText, dateText, statusText;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                memberIdText = itemView.findViewById(R.id.paymentMemberId);
                memberNameText = itemView.findViewById(R.id.paymentMemberName);
                amountText = itemView.findViewById(R.id.paymentAmount);
                dateText = itemView.findViewById(R.id.paymentDate);
                statusText = itemView.findViewById(R.id.paymentStatus);
            }

            void bind(String memberId, String memberName, String amount, String date, String status) {
                memberIdText.setText("ID: " + memberId);
                memberNameText.setText(memberName);
                amountText.setText(amount);
                dateText.setText(date);
                statusText.setText(status);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
