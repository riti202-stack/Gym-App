package com.example.gymapp.models;




public class Attendance {
    private String date;
    private String status;

    public Attendance(String date, String status) {
        this.date = date;
        this.status = status;
    }

    public String getDate() { return date; }
    public String getStatus() { return status; }
}
