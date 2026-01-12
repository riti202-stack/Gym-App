package com.example.util;

// Change to your package

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
}

