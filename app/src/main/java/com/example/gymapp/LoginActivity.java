package com.example.gymapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.util.DatabaseHelper;  // ✅ Use DatabaseHelper

public class LoginActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText passwordField;
    private TextView errorLabel;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();  // ← Triggers tables + admin
        db.close();
        dbHelper.close();

        // ✅ Initialize Database
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        errorLabel = findViewById(R.id.errorLabel);
        Button loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(v -> login());
    }

    private void login() {
        String user = usernameField.getText().toString().trim();
        String pass = passwordField.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Username and password cannot be empty!");
            return;
        }

        errorLabel.setText("Logging in...");

        new Thread(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            SQLiteDatabase writeDb = null;
            try {
                // ✅ ANDROID SQLite: Use Cursor + rawQuery
                Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{user});

                if (cursor.moveToFirst()) {
                    // User exists → verify password
                    String dbPass = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                    String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                    int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));

                    cursor.close();

                    if (!dbPass.equals(pass)) {
                        runOnUiThread(() -> errorLabel.setText("Invalid password!"));
                        return;
                    }

                    // Login successful
                    runOnUiThread(() -> {
                        if (role.equals("manager")) {
                            switchToDashboard();
                        } else {
                            switchToUserPanel(userId);
                        }
                    });

                } else {
                    // Create new user
                    writeDb = dbHelper.getWritableDatabase();
                    ContentValues userValues = new ContentValues();
                    userValues.put("username", user);
                    userValues.put("password", pass);
                    userValues.put("role", "user");

                    long newUserId = writeDb.insert("users", null, userValues);

                    if (newUserId != -1) {
                        // Create member profile
                        ContentValues memberValues = new ContentValues();
                        memberValues.put("user_id", newUserId);
                        memberValues.put("name", user);
                        memberValues.put("email", "");
                        memberValues.put("phone", "");
                        memberValues.put("join_date", android.text.format.DateFormat.getDateFormat(this).format(new java.util.Date()));
                        writeDb.insert("members", null, memberValues);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "🆕 New user created!", Toast.LENGTH_SHORT).show();
                            switchToUserPanel((int) newUserId);
                        });
                    } else {
                        runOnUiThread(() -> errorLabel.setText("Error creating user!"));
                    }
                    writeDb.close();
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    errorLabel.setText("Database error!");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            } finally {
                db.close();
                if (writeDb != null) writeDb.close();
                dbHelper.close();
            }
        }).start();
    }




    private void switchToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void switchToUserPanel(int userId) {
        Intent intent = new Intent(LoginActivity.this, UserPanelActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
