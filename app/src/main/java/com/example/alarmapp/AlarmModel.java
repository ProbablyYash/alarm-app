package com.example.alarmapp;

public class AlarmModel {
    public int id;
    public int hour;
    public int minute;
    public int year;
    public int month;
    public int day;
    public String mode;
    public String toneUri;
    public String toneName;
    public String label;
    public String imageUri;

    public AlarmModel(int id, int hour, int minute, int year, int month, int day, String mode, String toneUri, String toneName, String label, String imageUri) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.year = year;
        this.month = month;
        this.day = day;
        this.mode = mode;
        this.toneUri = toneUri;
        this.toneName = toneName;
        this.label = label;
        this.imageUri = imageUri;
    }
}
