package com.example.gymapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExerciseActivity extends AppCompatActivity {

    private int memberId = 1;
    private TextView timer1, timer2, timer3, timer4, timer5;
    private TextView reps1, reps2, reps3, reps4, reps5;
    private EditText weightInput, breakfastCalories, lunchCalories, dinnerCalories;
    private Button saveWeightBtn, attendanceBtn, backBtn;
    private LinearLayout dietChart;
    private TextView totalCalories;

    // Timer arrays
    private Handler[] timersHandler = new Handler[5];
    private Runnable[] timerRunnables = new Runnable[5];
    private long[] startTimes = new long[5];
    private boolean[] isPaused = new boolean[5];
    private int[] repCounts = new int[5];
    private final String[] EXERCISES = {"Push Ups", "Squats", "Sit Ups", "Lunges", "Plank"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);  // ✅ FIXED

        if (getIntent().hasExtra("memberId")) {
            memberId = getIntent().getIntExtra("memberId", 1);
        }

        initAllViews();
        setupAllListeners();
        Toast.makeText(this, "🏋️ Exercise for member: " + memberId, Toast.LENGTH_SHORT).show();
    }

    private void initAllViews() {
        timer1 = findViewById(R.id.timer1); timer2 = findViewById(R.id.timer2);
        timer3 = findViewById(R.id.timer3); timer4 = findViewById(R.id.timer4);
        timer5 = findViewById(R.id.timer5);

        reps1 = findViewById(R.id.reps1); reps2 = findViewById(R.id.reps2);
        reps3 = findViewById(R.id.reps3); reps4 = findViewById(R.id.reps4);
        reps5 = findViewById(R.id.reps5);

        weightInput = findViewById(R.id.weightInput);
        saveWeightBtn = findViewById(R.id.saveWeightBtn);
        dietChart = findViewById(R.id.dietChart);
        attendanceBtn = findViewById(R.id.attendanceBtn);
        backBtn = findViewById(R.id.backBtn);
        breakfastCalories = findViewById(R.id.breakfastCalories);
        lunchCalories = findViewById(R.id.lunchCalories);
        dinnerCalories = findViewById(R.id.dinnerCalories);
        totalCalories = findViewById(R.id.totalCalories);
    }

    private void setupAllListeners() {
        // Timer 1-5 buttons (safe null checks)
        safeSetListener(R.id.startBtn1, this::startTimer1);
        safeSetListener(R.id.pauseBtn1, this::pauseTimer1);
        safeSetListener(R.id.stopBtn1, this::stopTimer1);
        safeSetListener(R.id.addRepBtn1, this::addRep1);

        safeSetListener(R.id.startBtn2, this::startTimer2);
        safeSetListener(R.id.pauseBtn2, this::pauseTimer2);
        safeSetListener(R.id.stopBtn2, this::stopTimer2);
        safeSetListener(R.id.addRepBtn2, this::addRep2);

        safeSetListener(R.id.startBtn3, this::startTimer3);
        safeSetListener(R.id.pauseBtn3, this::pauseTimer3);
        safeSetListener(R.id.stopBtn3, this::stopTimer3);
        safeSetListener(R.id.addRepBtn3, this::addRep3);

        safeSetListener(R.id.startBtn4, this::startTimer4);
        safeSetListener(R.id.pauseBtn4, this::pauseTimer4);
        safeSetListener(R.id.stopBtn4, this::stopTimer4);
        safeSetListener(R.id.addRepBtn4, this::addRep4);

        safeSetListener(R.id.startBtn5, this::startTimer5);
        safeSetListener(R.id.pauseBtn5, this::pauseTimer5);
        safeSetListener(R.id.stopBtn5, this::stopTimer5);
        safeSetListener(R.id.addRepBtn5, this::addRep5);

        if (saveWeightBtn != null) saveWeightBtn.setOnClickListener(v -> saveWeight());
        if (attendanceBtn != null) attendanceBtn.setOnClickListener(v -> markAttendance());
        if (backBtn != null) backBtn.setOnClickListener(v -> finish());

        safeSetListener(R.id.saveBreakfastBtn, this::saveBreakfast);
        safeSetListener(R.id.saveLunchBtn, this::saveLunch);
        safeSetListener(R.id.saveDinnerBtn, this::saveDinner);
    }

    private void safeSetListener(int id, Runnable action) {
        Button btn = findViewById(id);
        if (btn != null) btn.setOnClickListener(v -> action.run());
    }

    // ✅ FIXED TIMER METHODS
    public void startTimer1() { startTimer(0, timer1, reps1); }
    public void pauseTimer1() { pauseTimer(0, timer1); }
    public void stopTimer1() { stopTimer(0, timer1, reps1); }
    public void addRep1() { addRep(0, reps1, "Push Ups"); }

    public void startTimer2() { startTimer(1, timer2, reps2); }
    public void pauseTimer2() { pauseTimer(1, timer2); }
    public void stopTimer2() { stopTimer(1, timer2, reps2); }
    public void addRep2() { addRep(1, reps2, "Squats"); }

    public void startTimer3() { startTimer(2, timer3, reps3); }
    public void pauseTimer3() { pauseTimer(2, timer3); }
    public void stopTimer3() { stopTimer(2, timer3, reps3); }
    public void addRep3() { addRep(2, reps3, "Sit Ups"); }

    public void startTimer4() { startTimer(3, timer4, reps4); }
    public void pauseTimer4() { pauseTimer(3, timer4); }
    public void stopTimer4() { stopTimer(3, timer4, reps4); }
    public void addRep4() { addRep(3, reps4, "Lunges"); }

    public void startTimer5() { startTimer(4, timer5, reps5); }
    public void pauseTimer5() { pauseTimer(4, timer5); }
    public void stopTimer5() { stopTimer(4, timer5, reps5); }
    public void addRep5() { addRep(4, reps5, "Plank"); }

    private void startTimer(int index, TextView timerLabel, TextView repsLabel) {
        if (timerLabel == null) return;

        if (timersHandler[index] != null) {
            timersHandler[index].removeCallbacksAndMessages(null);
        }
        startTimes[index] = System.currentTimeMillis();
        isPaused[index] = false;
        timersHandler[index] = new Handler(Looper.getMainLooper());

        timerRunnables[index] = () -> {
            if (timersHandler[index] == null || timerLabel == null) return;
            long elapsed = (System.currentTimeMillis() - startTimes[index]) / 1000;
            int min = (int)(elapsed / 60);
            int sec = (int)(elapsed % 60);
            timerLabel.setText(String.format("%02d:%02d", min, sec));
            if (!isPaused[index]) {
                timersHandler[index].postDelayed(timerRunnables[index], 1000);
            }
        };
        timersHandler[index].post(timerRunnables[index]);
        Toast.makeText(this, "▶️ " + EXERCISES[index] + " STARTED", Toast.LENGTH_SHORT).show();
    }

    private void pauseTimer(int index, TextView timerLabel) {
        if (timersHandler[index] != null) {
            isPaused[index] = !isPaused[index];
            if (!isPaused[index]) {
                timersHandler[index].post(timerRunnables[index]);
            }
            Toast.makeText(this, "⏸️ " + EXERCISES[index] + (isPaused[index] ? " PAUSED" : " RESUMED"), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopTimer(int index, TextView timerLabel, TextView repsLabel) {
        if (timersHandler[index] != null) {
            timersHandler[index].removeCallbacksAndMessages(null);
            timersHandler[index] = null;
            timerRunnables[index] = null;
        }
        isPaused[index] = false;
        repCounts[index] = 0;
        if (timerLabel != null) timerLabel.setText("00:00");
        if (repsLabel != null) repsLabel.setText("0 reps");
        Toast.makeText(this, "⏹️ " + EXERCISES[index] + " RESET", Toast.LENGTH_SHORT).show();
    }

    private void addRep(int index, TextView repsLabel, String exercise) {
        repCounts[index]++;
        if (repsLabel != null) {
            repsLabel.setText(repCounts[index] + " reps");
        }
        Toast.makeText(this, "✅ " + exercise + ": " + repCounts[index] + " reps", Toast.LENGTH_SHORT).show();
    }

    public void saveWeight() {
        if (weightInput == null) return;
        try {
            double weight = Double.parseDouble(weightInput.getText().toString().trim());
            weightInput.setText("");
            Toast.makeText(this, "✅ Weight: " + weight + "kg", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ Invalid weight", Toast.LENGTH_SHORT).show();
        }
    }

    public void markAttendance() {
        if (attendanceBtn != null) {
            attendanceBtn.setText("✅ Marked!");
        }
        Toast.makeText(this, "✅ Attendance marked", Toast.LENGTH_SHORT).show();
    }

    public void saveBreakfast() { saveMeal("Breakfast", breakfastCalories); }
    public void saveLunch() { saveMeal("Lunch", lunchCalories); }
    public void saveDinner() { saveMeal("Dinner", dinnerCalories); }

    private void saveMeal(String meal, EditText field) {
        if (field == null) return;
        try {
            String text = field.getText().toString().trim();
            if (text.isEmpty()) return;
            int cal = Integer.parseInt(text);
            if (cal > 0 && cal <= 5000) {
                field.setText("");
                Toast.makeText(this, "✅ " + meal + ": " + cal + " kcal", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "❌ Invalid " + meal + " calories", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < 5; i++) {
            if (timersHandler[i] != null) {
                timersHandler[i].removeCallbacksAndMessages(null);
                timersHandler[i] = null;
            }
        }
    }
}
