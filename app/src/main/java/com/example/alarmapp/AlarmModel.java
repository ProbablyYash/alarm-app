package com.example.alarmapp;

public class AlarmModel {
    public int id;
    public int hour;
    public int minute;
    public String toneUri;
    public String toneName;

    public AlarmModel(int id, int hour, int minute, String toneUri, String toneName) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.toneUri = toneUri;
        this.toneName = toneName;
    }
}
