package com.example.gymapp.models;



public class Diet {
    private String meal;
    private int calories;
    private String date;

    public Diet(String meal, int calories, String date) {
        this.meal = meal;
        this.calories = calories;
        this.date = date;
    }

    public String getMeal() { return meal; }
    public int getCalories() { return calories; }
    public String getDate() { return date; }
}

