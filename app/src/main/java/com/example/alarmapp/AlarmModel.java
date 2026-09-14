package com.example.alarmapp;

public class AlarmModel {
    public int id;
    public int hour;
    public int minute;
    public int year;
    public int month;
    public int day;
    public String mode;
    public String label;

    public AlarmModel(int id, int hour, int minute, int year, int month, int day, String mode, String label) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.year = year;
        this.month = month;
        this.day = day;
        this.mode = mode;
        this.label = label;
    }
}
