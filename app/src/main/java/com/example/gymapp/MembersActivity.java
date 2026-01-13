package com.example.gymapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.util.DatabaseHelper;

public class MembersActivity extends AppCompatActivity {

    // ✅ EXACT XML ID MATCH
    private TextView totalMembersLabel;
    private RecyclerView membersTable;
    private Button refreshBtn, backBtn;

    private DatabaseHelper dbHelper;
    private MembersAdapter membersAdapter;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.members_fragment);  // ✅ Use same layout!

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadMembersData();
        updateTotalMembersCount();
    }

    private void initViews() {
        totalMembersLabel = findViewById(R.id.totalMembersLabel);
        membersTable = findViewById(R.id.membersTable);
        refreshBtn = findViewById(R.id.refreshBtn);
        backBtn = findViewById(R.id.backBtn);
    }

    private void setupRecyclerView() {
        membersTable.setLayoutManager(new LinearLayoutManager(this));
        membersAdapter = new MembersAdapter();
        membersTable.setAdapter(membersAdapter);
    }

    private void setupListeners() {
        refreshBtn.setOnClickListener(v -> {
            v.setAlpha(0.7f);
            v.postDelayed(() -> v.setAlpha(1.0f), 150);
            refreshData();
            Toast.makeText(this, "🔄 Refreshed!", Toast.LENGTH_SHORT).show();
        });

        backBtn.setOnClickListener(v -> {
            v.setAlpha(0.7f);
            v.postDelayed(() -> {
                finish();  // ✅ Activity back = finish()
            }, 150);
        });
    }

    public void refreshData() {
        loadMembersData();
        updateTotalMembersCount();
    }

    private void loadMembersData() {
        new Thread(() -> {
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String query = """
                    SELECT m.id, m.name, m.email, m.join_date,
                           COALESCE(MAX(p.paid_date), 'No payments') as latest_payment,
                           COALESCE(SUM(p.amount), 0) as total_paid,
                           COALESCE(MAX(w.weight), 0) as latest_weight_val,
                           COALESCE(m.join_date, 'N/A') as first_login
                    FROM members m 
                    LEFT JOIN payments p ON m.id = p.member_id
                    LEFT JOIN weight w ON m.id = w.member_id
                    GROUP BY m.id, m.name, m.email, m.join_date
                    ORDER BY m.id
                    """;
                Cursor cursor = db.rawQuery(query, null);

                mainHandler.post(() -> {
                    membersAdapter.updateData(cursor);
                    Toast.makeText(this, "📊 Loaded " + cursor.getCount() + " members", Toast.LENGTH_SHORT).show();
                });

                db.close();
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this,
                        "❌ Members load failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                e.printStackTrace();
            }
        }).start();
    }

    private void updateTotalMembersCount() {
        new Thread(() -> {
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM members", null);
                if (cursor.moveToFirst()) {
                    int count = cursor.getInt(0);
                    mainHandler.post(() -> totalMembersLabel.setText("Total Members: " + count));
                }
                cursor.close();
                db.close();
            } catch (Exception e) {
                mainHandler.post(() -> totalMembersLabel.setText("Total Members: 0"));
                e.printStackTrace();
            }
        }).start();
    }

    // ✅ SAME MembersAdapter (unchanged)
    private static class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {
        private Cursor cursor;

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_member_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (cursor != null && !cursor.isClosed() && cursor.moveToPosition(position)) {
                try {
                    int idIdx = cursor.getColumnIndex("id");
                    int nameIdx = cursor.getColumnIndex("name");
                    int emailIdx = cursor.getColumnIndex("email");
                    int firstLoginIdx = cursor.getColumnIndex("first_login");
                    int latestPaymentIdx = cursor.getColumnIndex("latest_payment");
                    int totalPaidIdx = cursor.getColumnIndex("total_paid");
                    int weightIdx = cursor.getColumnIndex("latest_weight_val");
                    int joinDateIdx = cursor.getColumnIndex("join_date");

                    holder.idText.setText("#" + cursor.getInt(idIdx));
                    holder.nameText.setText(cursor.getString(nameIdx));
                    holder.emailText.setText(cursor.getString(emailIdx));
                    holder.firstLoginText.setText(cursor.getString(firstLoginIdx));
                    holder.latestPaymentText.setText(cursor.getString(latestPaymentIdx));
                    holder.totalPaidText.setText("৳" + cursor.getDouble(totalPaidIdx));

                    double weight = cursor.getDouble(weightIdx);
                    holder.latestWeightText.setText(weight > 0 ? weight + " kg" : "No weight");
                    holder.joinDateText.setText(cursor.getString(joinDateIdx));
                } catch (Exception e) {
                    holder.nameText.setText("Error loading data");
                }
            }
        }

        @Override
        public int getItemCount() {
            return cursor != null && !cursor.isClosed() ? cursor.getCount() : 0;
        }

        void updateData(Cursor newCursor) {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            cursor = newCursor;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView idText, nameText, emailText, firstLoginText, latestPaymentText,
                    totalPaidText, latestWeightText, joinDateText;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                idText = itemView.findViewById(R.id.memberId);
                nameText = itemView.findViewById(R.id.memberName);
                emailText = itemView.findViewById(R.id.memberEmail);
                //firstLoginText = itemView.findViewById(R.id.memberFirstLogin);
                latestPaymentText = itemView.findViewById(R.id.memberLatestPayment);
                totalPaidText = itemView.findViewById(R.id.memberTotalPaid);
                latestWeightText = itemView.findViewById(R.id.memberLatestWeight);
                joinDateText = itemView.findViewById(R.id.memberJoinDate);
            }
        }
    }
}
