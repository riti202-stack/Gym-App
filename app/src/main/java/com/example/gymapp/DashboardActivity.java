package com.example.gymapp;



import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.util.DatabaseHelper;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

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

        // Initialize views
        logoutBtn = findViewById(R.id.logoutBtn);
        paymentsLabel = findViewById(R.id.paymentsLabel);
        totalMembersLabel = findViewById(R.id.totalMembersLabel);
        monthlyIncomeLabel = findViewById(R.id.monthlyIncomeLabel);
        membersLabel = findViewById(R.id.membersLabel);
        Button paymentsBtn = findViewById(R.id.paymentsBtn);

        // Load initial stats
        loadDashboardStats();

        // Setup click listeners
        membersLabel.setOnClickListener(v -> showMembers());
        paymentsLabel.setOnClickListener(v -> showPayments());
        logoutBtn.setOnClickListener(v -> handleLogout());
        paymentsBtn.setOnClickListener(v -> showPayments());

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
                YearMonth currentMonth = YearMonth.now();
                String monthStart = currentMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
                String monthEnd = currentMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE);

                Cursor cursor = db.rawQuery(
                        "SELECT COALESCE(SUM(amount), 0) as monthly_total FROM payments WHERE paid_date BETWEEN ? AND ?",
                        new String[]{monthStart, monthEnd}
                );

                if (cursor.moveToFirst()) {
                    double monthlyTotal = cursor.getDouble(0);
                    runOnUiThread(() ->
                            monthlyIncomeLabel.setText(String.format("$%,.0f", monthlyTotal))
                    );
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.close();
            }
        }).start();
    }

    private void startAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadDashboardStats();
                handler.postDelayed(this, 30000); // 30 seconds
            }
        };
        handler.post(refreshRunnable);
    }

    private void showMembers() {
        Intent intent = new Intent(this, MembersActivity.class);
        startActivity(intent);
    }

    private void showPayments() {
        Intent intent = new Intent(this, PaymentsActivity.class);
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
