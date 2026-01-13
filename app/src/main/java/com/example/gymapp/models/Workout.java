

package com.example.gymapp.models;

public class Workout {
    private String exercise;
    private int reps;
    private String date;

    public Workout(String exercise, int reps, String date) {
        this.exercise = exercise;
        this.reps = reps;
        this.date = date;
    }

    public String getExercise() { return exercise; }
    public int getReps() { return reps; }
    public String getDate() { return date; }
}
