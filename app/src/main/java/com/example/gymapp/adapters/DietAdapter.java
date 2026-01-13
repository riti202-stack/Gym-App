package com.example.gymapp.adapters;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymapp.R;
import com.example.gymapp.models.Diet;

import java.util.ArrayList;
import java.util.List;

public class DietAdapter extends RecyclerView.Adapter<DietAdapter.ViewHolder> {
    private List<Diet> diet = new ArrayList<>();

    public void setDiet(List<Diet> diet) {
        this.diet = diet != null ? diet : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diet_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Diet item = diet.get(position);
        holder.meal.setText(item.getMeal());
        holder.calories.setText(item.getCalories() + " kcal");
        holder.date.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return diet.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView meal, calories, date;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            meal = itemView.findViewById(R.id.mealText);
            calories = itemView.findViewById(R.id.caloriesText);
            date = itemView.findViewById(R.id.dateText);
        }
    }
}

