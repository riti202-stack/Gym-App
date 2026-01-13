package com.example.gymapp.models;



public class Payment {
    private double amount;
    private String date;

    public Payment(double amount, String date) {
        this.amount = amount;
        this.date = date;
    }

    public double getAmount() { return amount; }
    public String getDate() { return date; }
}

