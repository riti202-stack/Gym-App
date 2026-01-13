package com.example.gymapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.util.DatabaseHelper;

public class DashboardActivity extends AppCompatActivity {

    private TextView logoutBtn, paymentsLabel, totalMembersLabel, monthlyIncomeLabel, membersLabel;
    private DatabaseHelper dbHelper;
    private Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = this::loadDashboardStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);

        logoutBtn = findViewById(R.id.logout);
        paymentsLabel = findViewById(R.id.paymentsLabel);
        totalMembersLabel = findViewById(R.id.totalMembersLabel);
        monthlyIncomeLabel = findViewById(R.id.monthlyIncomeLabel);
        membersLabel = findViewById(R.id.membersLabel);

        loadDashboardStats();

        if (membersLabel != null) membersLabel.setOnClickListener(v -> showMembers());
        if (paymentsLabel != null) paymentsLabel.setOnClickListener(v -> showPayments());
        logoutBtn.setOnClickListener(v -> handleLogout());

        startAutoRefresh();
    }

    private void loadDashboardStats() {
        updateTotalMembers();
        updateMonthlyIncome();
    }

    private void updateTotalMembers() {
        new Thread(() -> {
            try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
                try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM members", null)) {
                    if (cursor.moveToFirst()) {
                        int total = cursor.getInt(0);
                        runOnUiThread(() -> totalMembersLabel.setText(String.valueOf(total)));
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> totalMembersLabel.setText("0"));
            }
        }).start();
    }

    private void updateMonthlyIncome() {
        new Thread(() -> {
            try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
                try (Cursor cursor = db.rawQuery("SELECT COALESCE(SUM(amount), 0) FROM payments", null)) {
                    if (cursor.moveToFirst()) {
                        double total = cursor.getDouble(0);
                        runOnUiThread(() -> monthlyIncomeLabel.setText("৳" + (int)total));
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> monthlyIncomeLabel.setText("৳0"));
            }
        }).start();
    }

    private void startAutoRefresh() {
        handler.postDelayed(refreshRunnable, 30000);  // Refresh every 30s
    }

    private void showMembers() {
        startActivity(new Intent(this, MembersActivity.class));
    }

    private void showPayments() {
        startActivity(new Intent(this, PaymentsActivity.class));
    }

    private void handleLogout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(refreshRunnable);
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
