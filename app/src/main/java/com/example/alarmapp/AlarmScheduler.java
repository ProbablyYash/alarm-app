package com.example.alarmapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

public class AlarmScheduler {
    public static void schedule(Context context, AlarmModel alarm) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, alarm.hour);
        cal.set(Calendar.MINUTE, alarm.minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if ("ONCE".equals(alarm.mode)) {
            cal.set(Calendar.YEAR, alarm.year);
            cal.set(Calendar.MONTH, alarm.month);
            cal.set(Calendar.DAY_OF_MONTH, alarm.day);
        } else {
            if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("ALARM_ID", alarm.id);
        intent.putExtra("MODE", alarm.mode);
        intent.putExtra("TIME_STR", String.format("%02d:%02d", alarm.hour, alarm.minute));
        intent.putExtra("LABEL", alarm.label);

        PendingIntent pi = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent showIntent = new Intent(context, MainActivity.class);
        PendingIntent showPi = PendingIntent.getActivity(
            context,
            alarm.id,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(cal.getTimeInMillis(), showPi);
        am.setAlarmClock(clockInfo, pi);
    }

    public static void cancel(Context context, int id) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        am.cancel(pi);
    }
}
