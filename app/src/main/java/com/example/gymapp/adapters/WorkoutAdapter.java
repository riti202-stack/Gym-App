package com.example.gymapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymapp.R;
import com.example.gymapp.models.Workout;

import java.util.ArrayList;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {
    private List<Workout> workouts = new ArrayList<>();

    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts != null ? workouts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.workout_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.exercise.setText(workout.getExercise());
        holder.reps.setText(workout.getReps() + " reps");
        holder.date.setText(workout.getDate());
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView exercise, reps, date;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            exercise = itemView.findViewById(R.id.exerciseText);
            reps = itemView.findViewById(R.id.repsText);
            date = itemView.findViewById(R.id.dateText);
        }
    }
}
