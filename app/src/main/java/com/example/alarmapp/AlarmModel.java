package com.example.alarmapp;

public class AlarmModel {
    public int id;
    public int hour;
    public int minute;
    public int year;
    public int month; // 0-indexed (0 = Jan)
    public int day;
    public String mode; // "ONCE" or "DAILY"
    public String toneUri;
    public String toneName;

    public AlarmModel(int id, int hour, int minute, int year, int month, int day, String mode, String toneUri, String toneName) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.year = year;
        this.month = month;
        this.day = day;
        this.mode = mode;
        this.toneUri = toneUri;
        this.toneName = toneName;
    }
}
