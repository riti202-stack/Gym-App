package com.example.util;

// Change to your package

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.DropBoxManager;
import android.util.Log;

import com.example.gymapp.models.WeightRecord;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "gym.db";
    private static final int DB_VERSION = 1;

    // Admin credentials
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "1234";

    // Context for database file location
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "🧹 Creating fresh database...");
        createAllTables(db);
        insertAdminOnly(db);
        Log.d(TAG, "🎉 CLEAN DATABASE READY!");
        Log.d(TAG, "✅ Admin login: " + ADMIN_USERNAME + "/" + ADMIN_PASSWORD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "🔄 Upgrading database from v" + oldVersion + " to v" + newVersion);
        db.execSQL("DROP TABLE IF EXISTS weight");
        db.execSQL("DROP TABLE IF EXISTS diet");
        db.execSQL("DROP TABLE IF EXISTS workout");
        db.execSQL("DROP TABLE IF EXISTS attendance");
        db.execSQL("DROP TABLE IF EXISTS payments");
        db.execSQL("DROP TABLE IF EXISTS members");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // 🔥 ONE-TIME: Clean ALL sample data
    public void cleanupSampleData() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            Log.d(TAG, "🗑️ Deleting ALL sample data...");

            db.execSQL("DELETE FROM weight");
            db.execSQL("DELETE FROM diet");
            db.execSQL("DELETE FROM workout");
            db.execSQL("DELETE FROM attendance");
            db.execSQL("DELETE FROM payments");
            db.execSQL("DELETE FROM members");
            db.execSQL("DELETE FROM users");
            db.execSQL("DELETE FROM sqlite_sequence");

            Log.d(TAG, "✅ Sample data completely removed!");
        } finally {
            db.close();
        }
    }

    // ✅ Create all tables (identical to JavaFX)
    private void createAllTables(SQLiteDatabase db) {
        Log.d(TAG, "📋 Creating tables...");

        // Users table
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL" +
                ")");

        // Members table
        db.execSQL("CREATE TABLE IF NOT EXISTS members (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER UNIQUE, " +
                "name TEXT NOT NULL, " +
                "email TEXT, " +
                "phone TEXT, " +
                "join_date TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(id)" +
                ")");

        // Payments table
        db.execSQL("CREATE TABLE IF NOT EXISTS payments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "member_id INTEGER, " +
                "amount REAL, " +
                "paid_date TEXT, " +
                "FOREIGN KEY(member_id) REFERENCES members(id)" +
                ")");

        // Attendance table
        db.execSQL("CREATE TABLE IF NOT EXISTS attendance (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "member_id INTEGER, " +
                "date TEXT, " +
                "status TEXT, " +
                "FOREIGN KEY(member_id) REFERENCES members(id)" +
                ")");

        // Workout table
        db.execSQL("CREATE TABLE IF NOT EXISTS workout (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "member_id INTEGER, " +
                "exercise TEXT, " +
                "reps INTEGER, " +
                "day TEXT, " +
                "FOREIGN KEY(member_id) REFERENCES members(id)" +
                ")");

        // Diet table
        db.execSQL("CREATE TABLE IF NOT EXISTS diet (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "member_id INTEGER, " +
                "meal TEXT, " +
                "calories INTEGER, " +
                "date TEXT, " +
                "FOREIGN KEY(member_id) REFERENCES members(id)" +
                ")");

        // Weight table
        db.execSQL("CREATE TABLE IF NOT EXISTS weight (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "member_id INTEGER, " +
                "weight REAL, " +
                "record_date TEXT, " +
                "FOREIGN KEY(member_id) REFERENCES members(id)" +
                ")");
    }

    // ✅ Insert ONLY admin user
    private void insertAdminOnly(SQLiteDatabase db) {
        Log.d(TAG, "👑 Inserting admin user...");
        db.execSQL("INSERT OR IGNORE INTO users(id, username, password, role) " +
                "VALUES(1, '" + ADMIN_USERNAME + "', '" + ADMIN_PASSWORD + "', 'manager')");
        Log.d(TAG, "✅ Admin created: " + ADMIN_USERNAME + "/" + ADMIN_PASSWORD);
    }

    // 🔥 Initialize clean database (call once)
    public static void initDatabase(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        helper.getReadableDatabase(); // Triggers onCreate
        helper.close();
    }
    public void insertWorkout(int memberId, String exercise, int reps, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("member_id", memberId);
        values.put("exercise", exercise);
        values.put("reps", reps);
        values.put("day", date);
        db.insert("workout", null, values);
        db.close();
    }

    // Add to your DatabaseHelper class:
    public void insertWeightRecord(WeightRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("member_id", record.getMemberId());
        values.put("weight", record.getWeight());
        values.put("record_date", record.getDate());
        db.insert("weight", null, values);
        db.close();
    }

    public List<WeightRecord> getWeightRecords(int memberId) {
        List<WeightRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM weight WHERE member_id = ? ORDER BY record_date DESC",
                new String[]{String.valueOf(memberId)});

        if (cursor.moveToFirst()) {
            do {
                WeightRecord record = new WeightRecord(
                        cursor.getInt(0),  // id
                        cursor.getString(2),  // date
                        cursor.getDouble(3),  // weight
                        cursor.getInt(1)  // member_id
                );
                records.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return records;
    }


}

