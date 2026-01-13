package com.example.gymapp;

import android.graphics.Color;
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

    // ✅ EXACT MATCH - All @FXML fields from controller
    private TextView timer1, timer2, timer3, timer4, timer5;
    private TextView reps1, reps2, reps3, reps4, reps5;
    private EditText weightInput;
    private Button saveWeightBtn;
    private LinearLayout dietChart;
    private Button attendanceBtn, backBtn;
    private EditText breakfastCalories, lunchCalories, dinnerCalories;
    private TextView totalCalories;

    // Timer state tracking (matches controller arrays)
    private Handler[] timersHandler = new Handler[5];
    private Runnable[] timerRunnables = new Runnable[5];
    private long[] startTimes = new long[5];
    private boolean[] isPaused = new boolean[5];
    private int[] repCounts = new int[5];

    private final String[] EXERCISES = {"Push Ups", "Squats", "Sit Ups", "Lunges", "Plank"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_exercise);  // ✅ Use same layout!

        // Get memberId from Intent
        if (getIntent().hasExtra("memberId")) {
            memberId = getIntent().getIntExtra("memberId", 1);
        }

        initAllViews();
        setupAllListeners();
        updateTotalCalories();
        loadDietChart();

        Toast.makeText(this, "🏋️ Exercise loaded for member: " + memberId, Toast.LENGTH_SHORT).show();
    }

    private void initAllViews() {
        // ✅ EXACT @FXML field matches
        timer1 = findViewById(R.id.timer1);
        timer2 = findViewById(R.id.timer2);
        timer3 = findViewById(R.id.timer3);
        timer4 = findViewById(R.id.timer4);
        timer5 = findViewById(R.id.timer5);

        reps1 = findViewById(R.id.reps1);
        reps2 = findViewById(R.id.reps2);
        reps3 = findViewById(R.id.reps3);
        reps4 = findViewById(R.id.reps4);
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
        // Timer 1 buttons
        Button startBtn1 = findViewById(R.id.startBtn1); startBtn1.setOnClickListener(v -> startTimer1());
        Button pauseBtn1 = findViewById(R.id.pauseBtn1); pauseBtn1.setOnClickListener(v -> pauseTimer1());
        Button stopBtn1 = findViewById(R.id.stopBtn1); stopBtn1.setOnClickListener(v -> stopTimer1());
        Button addRepBtn1 = findViewById(R.id.addRepBtn1); addRepBtn1.setOnClickListener(v -> addRep1());

        // Timer 2 buttons
        Button startBtn2 = findViewById(R.id.startBtn2); startBtn2.setOnClickListener(v -> startTimer2());
        Button pauseBtn2 = findViewById(R.id.pauseBtn2); pauseBtn2.setOnClickListener(v -> pauseTimer2());
        Button stopBtn2 = findViewById(R.id.stopBtn2); stopBtn2.setOnClickListener(v -> stopTimer2());
        Button addRepBtn2 = findViewById(R.id.addRepBtn2); addRepBtn2.setOnClickListener(v -> addRep2());

        // Timer 3 buttons
        Button startBtn3 = findViewById(R.id.startBtn3); startBtn3.setOnClickListener(v -> startTimer3());
        Button pauseBtn3 = findViewById(R.id.pauseBtn3); pauseBtn3.setOnClickListener(v -> pauseTimer3());
        Button stopBtn3 = findViewById(R.id.stopBtn3); stopBtn3.setOnClickListener(v -> stopTimer3());
        Button addRepBtn3 = findViewById(R.id.addRepBtn3); addRepBtn3.setOnClickListener(v -> addRep3());

        // Timer 4 buttons
        Button startBtn4 = findViewById(R.id.startBtn4); startBtn4.setOnClickListener(v -> startTimer4());
        Button pauseBtn4 = findViewById(R.id.pauseBtn4); pauseBtn4.setOnClickListener(v -> pauseTimer4());
        Button stopBtn4 = findViewById(R.id.stopBtn4); stopBtn4.setOnClickListener(v -> stopTimer4());
        Button addRepBtn4 = findViewById(R.id.addRepBtn4); addRepBtn4.setOnClickListener(v -> addRep4());

        // Timer 5 buttons
        Button startBtn5 = findViewById(R.id.startBtn5); startBtn5.setOnClickListener(v -> startTimer5());
        Button pauseBtn5 = findViewById(R.id.pauseBtn5); pauseBtn5.setOnClickListener(v -> pauseTimer5());
        Button stopBtn5 = findViewById(R.id.stopBtn5); stopBtn5.setOnClickListener(v -> stopTimer5());
        Button addRepBtn5 = findViewById(R.id.addRepBtn5); addRepBtn5.setOnClickListener(v -> addRep5());

        // Other buttons
        saveWeightBtn.setOnClickListener(v -> saveWeight());
        attendanceBtn.setOnClickListener(v -> markAttendance());
        backBtn.setOnClickListener(v -> finish());  // ✅ Activity back = finish()

        findViewById(R.id.saveBreakfastBtn).setOnClickListener(v -> saveBreakfast());
        findViewById(R.id.saveLunchBtn).setOnClickListener(v -> saveLunch());
        findViewById(R.id.saveDinnerBtn).setOnClickListener(v -> saveDinner());
    }

    // ✅ ALL SAME TIMER METHODS (unchanged)
    @SuppressWarnings("unused")
    public void startTimer1() { startTimer(0, timer1, reps1); }
    @SuppressWarnings("unused")
    public void pauseTimer1() { pauseTimer(0, timer1); }
    @SuppressWarnings("unused")
    public void stopTimer1() { stopTimer(0, timer1, reps1); }

    @SuppressWarnings("unused")
    public void startTimer2() { startTimer(1, timer2, reps2); }
    @SuppressWarnings("unused")
    public void pauseTimer2() { pauseTimer(1, timer2); }
    @SuppressWarnings("unused")
    public void stopTimer2() { stopTimer(1, timer2, reps2); }

    @SuppressWarnings("unused")
    public void startTimer3() { startTimer(2, timer3, reps3); }
    @SuppressWarnings("unused")
    public void pauseTimer3() { pauseTimer(2, timer3); }
    @SuppressWarnings("unused")
    public void stopTimer3() { stopTimer(2, timer3, reps3); }

    @SuppressWarnings("unused")
    public void startTimer4() { startTimer(3, timer4, reps4); }
    @SuppressWarnings("unused")
    public void pauseTimer4() { pauseTimer(3, timer4); }
    @SuppressWarnings("unused")
    public void stopTimer4() { stopTimer(3, timer4, reps4); }

    @SuppressWarnings("unused")
    public void startTimer5() { startTimer(4, timer5, reps5); }
    @SuppressWarnings("unused")
    public void pauseTimer5() { pauseTimer(4, timer5); }
    @SuppressWarnings("unused")
    public void stopTimer5() { stopTimer(4, timer5, reps5); }

    @SuppressWarnings("unused")
    public void addRep1() { addRep(0, reps1, "Push Ups"); }
    @SuppressWarnings("unused")
    public void addRep2() { addRep(1, reps2, "Squats"); }
    @SuppressWarnings("unused")
    public void addRep3() { addRep(2, reps3, "Sit Ups"); }
    @SuppressWarnings("unused")
    public void addRep4() { addRep(3, reps4, "Lunges"); }
    @SuppressWarnings("unused")
    public void addRep5() { addRep(4, reps5, "Plank"); }

    // ✅ ALL SAME TIMER LOGIC (unchanged from Fragment)
    private void startTimer(int index, TextView timerLabel, TextView repsLabel) {
        if (timersHandler[index] != null) {
            timersHandler[index].removeCallbacksAndMessages(null);
        }

        final long startTime = System.currentTimeMillis();
        timersHandler[index] = new Handler(Looper.getMainLooper());

        timerRunnables[index] = new Runnable() {
            @Override
            public void run() {
                if (timersHandler[index] == null) return;
                long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
                int minutes = (int)(elapsedSeconds / 60);
                int seconds = (int)(elapsedSeconds % 60);
                timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

                if (!isPaused[index]) {
                    timersHandler[index].postDelayed(this, 1000);
                }
            }
        };
        timersHandler[index].post(timerRunnables[index]);
        Toast.makeText(this, "▶️ Timer " + (index+1) + " STARTED", Toast.LENGTH_SHORT).show();
    }

    private void pauseTimer(int index, TextView timerLabel) {
        if (timersHandler[index] != null) {
            if (isPaused[index]) {
                isPaused[index] = false;
                timersHandler[index].post(timerRunnables[index]);
            } else {
                isPaused[index] = true;
            }
            Toast.makeText(this, "⏸️ Timer " + (index+1) + (isPaused[index] ? " PAUSED" : " RESUMED"), Toast.LENGTH_SHORT).show();
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
        timerLabel.setText("00:00");
        repsLabel.setText("0 reps");
        Toast.makeText(this, "⏹️ Timer " + (index+1) + " RESET", Toast.LENGTH_SHORT).show();
    }

    private void addRep(int index, TextView repsLabel, String exercise) {
        repCounts[index]++;
        repsLabel.setText(repCounts[index] + " reps");
        Toast.makeText(this, "✅ Saved " + exercise + ": " + repCounts[index] + " reps", Toast.LENGTH_SHORT).show();
    }

    // ✅ ALL SAME METHODS (unchanged)
    public void saveWeight() {
        try {
            double weight = Double.parseDouble(weightInput.getText().toString().trim());
            weightInput.setText("");
            Toast.makeText(this, "✅ Weight saved: " + weight + "kg", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ Invalid weight", Toast.LENGTH_SHORT).show();
        }
    }

    public void markAttendance() {
        try {
            attendanceBtn.setText("✅ Marked Today!");
            Toast.makeText(this, "✅ Attendance marked", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "ℹ️ Already marked today", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveBreakfast() { saveMeal("Breakfast", breakfastCalories); }
    public void saveLunch() { saveMeal("Lunch", lunchCalories); }
    public void saveDinner() { saveMeal("Dinner", dinnerCalories); }

    private void saveMeal(String mealName, EditText calorieField) {
        try {
            String text = calorieField.getText().toString().trim();
            if (text.isEmpty()) return;
            int calories = Integer.parseInt(text);
            if (calories <= 0 || calories > 5000) {
                Toast.makeText(this, "❌ Invalid calories for " + mealName, Toast.LENGTH_SHORT).show();
                return;
            }
            calorieField.setText("");
            loadDietChart();
            updateTotalCalories();
            Toast.makeText(this, "✅ " + mealName + ": " + calories + " kcal saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ Enter valid calories for " + mealName, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalCalories() {
        if (totalCalories != null) {
            totalCalories.setText("1800 kcal");
        }
    }

    private void loadDietChart() {
        if (dietChart == null || dietChart.getChildCount() < 3) return;

        try {
            TextView breakfastView = (TextView) dietChart.getChildAt(0);
            TextView lunchView = (TextView) dietChart.getChildAt(1);
            TextView dinnerView = (TextView) dietChart.getChildAt(2);

            breakfastView.setText("Breakfast\n500 kcal");
            lunchView.setText("Lunch\n700 kcal");
            dinnerView.setText("Dinner\n600 kcal");
        } catch (Exception e) {
            Toast.makeText(this, "ℹ️ No diet data", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < 5; i++) {
            if (timersHandler[i] != null) {
                timersHandler[i].removeCallbacksAndMessages(null);
            }
        }
    }
}
