package com.example.alarmapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int alarmId = intent.getIntExtra("ALARM_ID", -1);
        String mode = intent.getStringExtra("MODE");

        if (alarmId != -1) {
            SharedPreferences sp = context.getSharedPreferences("alarms_db", Context.MODE_PRIVATE);
            String raw = sp.getString("list", "[]");
            try {
                JSONArray arr = new JSONArray(raw);
                JSONArray updatedArr = new JSONArray();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    int id = o.getInt("id");

                    if (id == alarmId) {
                        if ("DAILY".equals(mode)) {
                            AlarmModel m = new AlarmModel(
                                id, o.getInt("hour"), o.getInt("minute"),
                                o.optInt("year"), o.optInt("month"), o.optInt("day"),
                                "DAILY", o.getString("toneUri"), o.getString("toneName"),
                                o.optString("label", "Alarm"), o.optString("imageUri", "")
                            );
                            AlarmScheduler.schedule(context, m);
                            updatedArr.put(o);
                        }
                    } else {
                        updatedArr.put(o);
                    }
                }
                sp.edit().putString("list", updatedArr.toString()).apply();
            } catch (Exception ignored) {}
        }

        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtras(intent.getExtras());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
