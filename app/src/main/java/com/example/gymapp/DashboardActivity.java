package com.example.gymapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.util.DatabaseHelper;

public class DashboardActivity extends AppCompatActivity {

    private TextView logoutBtn, paymentsLabel, totalMembersLabel, monthlyIncomeLabel, membersLabel;
    private DatabaseHelper dbHelper;
    private Handler handler = new Handler();
    private Runnable refreshRunnable;

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

        // ✅ FIXED: Only TextViews that exist
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
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            try {
                Cursor cursor = db.rawQuery("SELECT COUNT(*) as total FROM members", null);
                if (cursor.moveToFirst()) {
                    int total = cursor.getInt(0);
                    runOnUiThread(() -> {
                        if (totalMembersLabel != null) {
                            totalMembersLabel.setText(String.valueOf(total));
                        }
                    });
                }
                cursor.close();
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (totalMembersLabel != null) totalMembersLabel.setText("0");
                });
            } finally {
                db.close();
            }
        }).start();
    }

    private void updateMonthlyIncome() {
        new Thread(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            try {
                Cursor cursor = db.rawQuery("SELECT COALESCE(SUM(amount), 0) as total FROM payments", null);
                if (cursor.moveToFirst()) {
                    double total = cursor.getDouble(0);
                    runOnUiThread(() -> {
                        if (monthlyIncomeLabel != null) {
                            monthlyIncomeLabel.setText("৳" + (int)total);
                        }
                    });
                }
                cursor.close();
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (monthlyIncomeLabel != null) monthlyIncomeLabel.setText("৳0");
                });
            } finally {
                db.close();
            }
        }).start();
    }

    // ✅ FIXED: Proper recurring timer
    private void startAutoRefresh() {
        refreshRunnable = this::loadDashboardStats;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadDashboardStats();
                handler.postDelayed(this, 30000);  // Repeat every 30s
            }
        }, 30000);
    }

    // ✅ FIXED: Create MembersActivity instead of Fragment
    private void showMembers() {
        Intent intent = new Intent(this, MembersActivity.class);  // Create this Activity
        startActivity(intent);
    }

    private void showPayments() {
        Intent intent = new Intent(this, PaymentsActivity.class);  // Create this Activity
        startActivity(intent);
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
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
