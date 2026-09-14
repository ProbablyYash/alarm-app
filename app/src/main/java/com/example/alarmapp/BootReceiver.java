package com.example.alarmapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences sp = context.getSharedPreferences("alarms_db", Context.MODE_PRIVATE);
            String raw = sp.getString("list", "[]");
            try {
                JSONArray arr = new JSONArray(raw);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    AlarmModel m = new AlarmModel(
                        o.getInt("id"),
                        o.getInt("hour"),
                        o.getInt("minute"),
                        o.getString("toneUri"),
                        o.getString("toneName")
                    );
                    AlarmScheduler.schedule(context, m);
                }
            } catch (Exception ignored) {}
        }
    }
}
