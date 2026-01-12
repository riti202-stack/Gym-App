package com.example.gymapp;



import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.util.DatabaseHelper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserPanelActivity extends AppCompatActivity {

    private int userId, memberId;
    private DatabaseHelper dbHelper;

    // RecyclerViews for 5 tables
    private RecyclerView paymentsTable, attendanceTable, workoutTable, dietTable, weightTable;
    private PaymentAdapter paymentAdapter;
    private AttendanceAdapter attendanceAdapter;
    private WorkoutAdapter workoutAdapter;
    private DietAdapter dietAdapter;
    private WeightAdapter weightAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_panel);

        dbHelper = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("USER_ID", -1);

        initRecyclerViews();
        resolveMemberId();
        setupButtons();
    }

    private void initRecyclerViews() {
        // Setup all 5 RecyclerViews
        paymentsTable = findViewById(R.id.paymentsTable);
        attendanceTable = findViewById(R.id.attendanceTable);
        workoutTable = findViewById(R.id.workoutTable);
        dietTable = findViewById(R.id.dietTable);
        weightTable = findViewById(R.id.weightTable);

        // Initialize adapters
        paymentAdapter = new PaymentAdapter(new ArrayList<>());
        attendanceAdapter = new AttendanceAdapter(new ArrayList<>());
        workoutAdapter = new WorkoutAdapter(new ArrayList<>());
        dietAdapter = new DietAdapter(new ArrayList<>());
        weightAdapter = new WeightAdapter(new ArrayList<>());

        // Setup layout managers
        paymentsTable.setLayoutManager(new LinearLayoutManager(this));
        attendanceTable.setLayoutManager(new LinearLayoutManager(this));
        workoutTable.setLayoutManager(new LinearLayoutManager(this));
        dietTable.setLayoutManager(new LinearLayoutManager(this));
        weightTable.setLayoutManager(new LinearLayoutManager(this));

        paymentsTable.setAdapter(paymentAdapter);
        attendanceTable.setAdapter(attendanceAdapter);
        workoutTable.setAdapter(workoutAdapter);
        dietTable.setAdapter(dietAdapter);
        weightTable.setAdapter(weightAdapter);
    }

    private void resolveMemberId() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT id FROM members WHERE user_id = ?",
                    new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) {
                memberId = cursor.getInt(0);
                loadAllData();
            }
            cursor.close();
        } finally {
            db.close();
        }
    }

    private void setupButtons() {
        Button refreshBtn = findViewById(R.id.refreshBtn);
        Button logoutBtn = findViewById(R.id.logoutBtn);
        Button exerciseBtn = findViewById(R.id.exerciseBtn);

        refreshBtn.setOnClickListener(v -> refreshData());
        logoutBtn.setOnClickListener(v -> handleLogout());
        exerciseBtn.setOnClickListener(v -> goToExercise());
    }

    private void refreshData() {
        if (memberId != -1) {
            loadAllData();
        }
    }

    private void loadAllData() {
        loadPayments();
        loadAttendance();
        loadWorkout();
        loadDiet();
        loadWeight();
    }

    private void loadPayments() {
        new Thread(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<Payment> payments = new ArrayList<>();
            try {
                Cursor cursor = db.rawQuery(
                        "SELECT amount, paid_date FROM payments WHERE member_id = ? ORDER BY paid_date DESC",
                        new String[]{String.valueOf(memberId)});
                while (cursor.moveToFirst()) {
                    payments.add(new Payment(
                            cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                            cursor.getString(cursor.getColumnIndexOrThrow("paid_date"))
                    ));
                }
                cursor.close();
                runOnUiThread(() -> paymentAdapter.setPayments(payments));
            } finally {
                db.close();
            }
        }).start();
    }

    private void loadAttendance() {
        new Thread(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<Attendance> attendance = new ArrayList<>();
            try {
                Cursor cursor = db.rawQuery(
                        "SELECT date, status FROM attendance WHERE member_id = ? ORDER BY date DESC",
                        new String[]{String.valueOf(memberId)});
                while (cursor.moveToFirst()) {
                    attendance.add(new Attendance(
                            cursor.getString(cursor.getColumnIndexOrThrow("date")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status"))
                    ));
                }
                cursor.close();
                runOnUiThread(() -> attendanceAdapter.setAttendance(attendance));
            } finally {
                db.close();
            }
        }).start();
    }

    // Similar methods for loadWorkout(), loadDiet(), loadWeight()...

    private void goToExercise() {
        Intent intent = new Intent(this, ExerciseActivity.class);
        intent.putExtra("MEMBER_ID", memberId);
        startActivity(intent);
    }

    private void handleLogout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
    }
}

