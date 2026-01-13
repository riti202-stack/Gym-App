package com.example.gymapp;



import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymapp.adapters.AttendanceAdapter;
import com.example.gymapp.adapters.DietAdapter;
import com.example.gymapp.adapters.PaymentsAdapter;
import com.example.gymapp.adapters.WeightAdapter;
import com.example.gymapp.adapters.WorkoutAdapter;
import com.example.gymapp.models.Attendance;
import com.example.gymapp.models.Diet;
import com.example.gymapp.models.Payment;
import com.example.gymapp.models.WeightRecord;
import com.example.gymapp.models.Workout;
import com.example.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class UserPanelActivity extends AppCompatActivity {

    private int userId, memberId;
    private DatabaseHelper dbHelper;

    // UI Elements - matches your exact layout XML
    private TextView memberNameLabel;
    private RecyclerView paymentsRecycler, attendanceRecycler, workoutRecycler,
            dietRecycler, weightRecycler;
    private Button refreshBtn, exerciseBtn, logoutBtn;

    // Adapters (all 5 complete)
    private PaymentsAdapter paymentAdapter;
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

        initViews();
        initRecyclerViews();
        setupButtons();

        // Load member data
        resolveMemberId();
    }

    private void initViews() {
        memberNameLabel = findViewById(R.id.memberNameLabel);
        refreshBtn = findViewById(R.id.refreshBtn);
        exerciseBtn = findViewById(R.id.exerciseBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
    }

    private void initRecyclerViews() {
        // Find all RecyclerViews from your layout
        paymentsRecycler = findViewById(R.id.paymentsRecycler);
        attendanceRecycler = findViewById(R.id.attendanceRecycler);
        workoutRecycler = findViewById(R.id.workoutRecycler);
        dietRecycler = findViewById(R.id.dietRecycler);
        weightRecycler = findViewById(R.id.weightRecycler);

        // Initialize all 5 adapters
        paymentAdapter = new PaymentsAdapter();
        attendanceAdapter = new AttendanceAdapter();
        workoutAdapter = new WorkoutAdapter();
        dietAdapter = new DietAdapter();
        weightAdapter = new WeightAdapter();

        // Setup LayoutManagers & Adapters
        setupRecyclerView(paymentsRecycler, paymentAdapter);
        setupRecyclerView(attendanceRecycler, attendanceAdapter);
        setupRecyclerView(workoutRecycler, workoutAdapter);
        setupRecyclerView(dietRecycler, dietAdapter);
        setupRecyclerView(weightRecycler, weightAdapter);
    }

    private void setupRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true); // Performance optimization
    }

    private void setupButtons() {
        refreshBtn.setOnClickListener(v -> refreshData());
        exerciseBtn.setOnClickListener(v -> goToExercise());
        logoutBtn.setOnClickListener(v -> handleLogout());
    }

    private void resolveMemberId() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT id, name FROM members WHERE user_id = ?",
                new String[]{String.valueOf(userId)})) {

            if (cursor.moveToFirst()) {
                memberId = cursor.getInt(0);
                String memberName = cursor.getString(1);

                // Update UI with member name
                memberNameLabel.setText("👤 " + memberName + " (#" + memberId + ")");

                // Load all data
                loadAllData();
                Toast.makeText(this, "Welcome " + memberName + "!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Member not found", Toast.LENGTH_LONG).show();
                memberNameLabel.setText("👤 No Member Data");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    private void refreshData() {
        if (memberId != -1) {
            memberNameLabel.setText("🔄 Refreshing...");
            loadAllData();
            Toast.makeText(this, "✅ Data refreshed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAllData() {
        loadPayments();
        loadAttendance();
        loadWorkout();
        loadDiet();
        loadWeight();
    }

    // Background thread data loading (5 methods)
    private void loadPayments() {
        new Thread(() -> {
            List<Payment> payments = queryPayments();
            runOnUiThread(() -> paymentAdapter.setPayments(payments));
        }).start();
    }

    private void loadAttendance() {
        new Thread(() -> {
            List<Attendance> attendance = queryAttendance();
            runOnUiThread(() -> attendanceAdapter.setAttendance(attendance));
        }).start();
    }

    private void loadWorkout() {
        new Thread(() -> {
            List<Workout> workouts = queryWorkout();
            runOnUiThread(() -> workoutAdapter.setWorkouts(workouts));
        }).start();
    }

    private void loadDiet() {
        new Thread(() -> {
            List<Diet> diets = queryDiet();
            runOnUiThread(() -> dietAdapter.setDiet(diets));
        }).start();
    }

    private void loadWeight() {
        new Thread(() -> {
            List<WeightRecord> weights = queryWeight();
            runOnUiThread(() -> weightAdapter.setWeights(weights));
        }).start();
    }

    // Database query methods (exact SQL from JavaFX version)
    private List<Payment> queryPayments() {
        List<Payment> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT amount, paid_date FROM payments WHERE member_id = ? ORDER BY paid_date DESC LIMIT 5",
                new String[]{String.valueOf(memberId)})) {
            while (cursor.moveToNext()) {
                list.add(new Payment(
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("paid_date"))
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return list;
    }

    private List<Attendance> queryAttendance() {
        List<Attendance> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT date, status FROM attendance WHERE member_id = ? ORDER BY date DESC LIMIT 5",
                new String[]{String.valueOf(memberId)})) {
            while (cursor.moveToNext()) {
                list.add(new Attendance(
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return list;
    }

    private List<Workout> queryWorkout() {
        List<Workout> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT exercise, reps, day FROM workout WHERE member_id = ? ORDER BY day DESC LIMIT 5",
                new String[]{String.valueOf(memberId)})) {
            while (cursor.moveToNext()) {
                list.add(new Workout(
                        cursor.getString(cursor.getColumnIndexOrThrow("exercise")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("reps")),
                        cursor.getString(cursor.getColumnIndexOrThrow("day"))
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return list;
    }

    private List<Diet> queryDiet() {
        List<Diet> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT meal, calories, date FROM diet WHERE member_id = ? ORDER BY date DESC LIMIT 5",
                new String[]{String.valueOf(memberId)})) {
            while (cursor.moveToNext()) {
                list.add(new Diet(
                        cursor.getString(cursor.getColumnIndexOrThrow("meal")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("calories")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date"))
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return list;
    }

    private List<WeightRecord> queryWeight() {
        List<WeightRecord> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT record_date, weight FROM weight WHERE member_id = ? ORDER BY record_date DESC LIMIT 5",
                new String[]{String.valueOf(memberId)})) {
            while (cursor.moveToNext()) {
                list.add(new WeightRecord(
                        cursor.getString(cursor.getColumnIndexOrThrow("record_date")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("weight"))
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return list;
    }

    private void goToExercise() {
        if (memberId != -1) {
            Intent intent = new Intent(this, ExerciseActivity.class);
            intent.putExtra("MEMBER_ID", memberId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
        Toast.makeText(this, "👋 Logged out", Toast.LENGTH_SHORT).show();
    }
}


