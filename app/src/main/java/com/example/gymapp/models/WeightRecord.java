

package com.example.gymapp.models;

public class WeightRecord {
    private String date;
    private double weight;

    public WeightRecord(String date, double weight) {
        this.date = date;
        this.weight = weight;
    }

    public String getDate() { return date; }
    public double getWeight() { return weight; }


}
