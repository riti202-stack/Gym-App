package com.example.gymapp.adapters;

import com.example.gymapp.models.WeightRecord;  // ✅ FIXED: Your custom model
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gymapp.R;

import java.util.ArrayList;
import java.util.List;

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.ViewHolder> {
    private List<WeightRecord> weights = new ArrayList<>();  // ✅ FIXED: Your model

    public void setWeights(List<WeightRecord> weights) {  // ✅ FIXED: Your model
        this.weights = weights;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weight_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightRecord weight = weights.get(position);
        holder.dateText.setText(weight.getDate());      // ✅ dateText
        holder.weightText.setText(String.format("%.1f kg", weight.getWeight()));  // ✅ weightText
    }


    @Override
    public int getItemCount() { return weights.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, weightText;  // ✅ Match your XML IDs

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);      // ✅ dateText
            weightText = itemView.findViewById(R.id.weightText);  // ✅ weightText
        }
    }


}
