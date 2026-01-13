package com.example.gymapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.util.DatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

        // ✅ FIXED: Match exact XML IDs
        logoutBtn = findViewById(R.id.logout);           // XML: @+id/logout
        paymentsLabel = findViewById(R.id.paymentsLabel);
        totalMembersLabel = findViewById(R.id.totalMembersLabel);
        monthlyIncomeLabel = findViewById(R.id.monthlyIncomeLabel);
        membersLabel = findViewById(R.id.membersLabel);

        // Load initial stats
        loadDashboardStats();

        // ✅ FIXED: Click listeners ONLY for existing views
        membersLabel.setOnClickListener(this::showMembers);
        paymentsLabel.setOnClickListener(this::showPayments);
        logoutBtn.setOnClickListener(this::handleLogout);

        // Visual feedback for clickable TextViews
        membersLabel.setBackground(getDrawable(android.R.drawable.list_selector_background));
        paymentsLabel.setBackground(getDrawable(android.R.drawable.list_selector_background));
        logoutBtn.setBackground(getDrawable(android.R.drawable.list_selector_background));

        // Auto-refresh every 30 seconds
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
                    runOnUiThread(() -> totalMembersLabel.setText(String.valueOf(total)));
                }
                cursor.close();
            } catch (Exception e) {
                runOnUiThread(() -> totalMembersLabel.setText("0"));
                e.printStackTrace();
            } finally {
                db.close();
            }
        }).start();
    }

    private void updateMonthlyIncome() {
        new Thread(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            try {
                // ✅ FIXED: Android-compatible date calculation
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                cal.set(Calendar.DAY_OF_MONTH, 1);
                String monthStart = monthFormat.format(cal.getTime());

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                String monthEnd = monthFormat.format(cal.getTime());

                Cursor cursor = db.rawQuery(
                        "SELECT COALESCE(SUM(amount), 0) as monthly_total FROM payments WHERE paid_date BETWEEN ? AND ?",
                        new String[]{monthStart, monthEnd}
                );

                if (cursor.moveToFirst()) {
                    double monthlyTotal = cursor.getDouble(0);
                    runOnUiThread(() ->
                            monthlyIncomeLabel.setText(String.format("৳%,.0f", monthlyTotal))
                    );
                } else {
                    runOnUiThread(() -> monthlyIncomeLabel.setText("৳0"));
                }
                cursor.close();
            } catch (Exception e) {
                runOnUiThread(() -> monthlyIncomeLabel.setText("৳0"));
                e.printStackTrace();
            } finally {
                db.close();
            }
        }).start();
    }

    private void startAutoRefresh() {
        refreshRunnable = this::loadDashboardStats;  // Lambda reference
        handler.postDelayed(refreshRunnable, 30000); // 30 seconds
        handler.postDelayed(() -> {
            if (refreshRunnable != null) {
                handler.postDelayed(refreshRunnable, 30000);
            }
        }, 30000);
    }

    private void showMembers() {
        // Visual feedback
        membersLabel.setAlpha(0.7f);
        membersLabel.postDelayed(() -> membersLabel.setAlpha(1.0f), 150);

        Intent intent = new Intent(this, MembersActivity.class);
        startActivity(intent);
    }

    private void showPayments() {
        // Visual feedback
        paymentsLabel.setAlpha(0.7f);
        paymentsLabel.postDelayed(() -> paymentsLabel.setAlpha(1.0f), 150);

        Intent intent = new Intent(this, PaymentsActivity.class);
        startActivity(intent);
    }

    private void handleLogout() {
        logoutBtn.setAlpha(0.7f);
        logoutBtn.postDelayed(() -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 150);
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
