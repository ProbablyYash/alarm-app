package com.example.alarmapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends Activity {
    private TimePicker timePicker;
    private TextView txtTone;
    private Uri selectedToneUri;
    private static final int RINGTONE_PICKER_REQUEST = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = findViewById(R.id.timePicker);
        txtTone = findViewById(R.id.txtSelectedTone);
        Button btnPickTone = findViewById(R.id.btnPickTone);
        Button btnSetAlarm = findViewById(R.id.btnSetAlarm);
        Button btnCancelAlarm = findViewById(R.id.btnCancelAlarm);

        selectedToneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        btnPickTone.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM | RingtoneManager.TYPE_RINGTONE);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            startActivityForResult(intent, RINGTONE_PICKER_REQUEST);
        });

        btnSetAlarm.setOnClickListener(v -> scheduleAlarm());
        btnCancelAlarm.setOnClickListener(v -> cancelAlarm());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RINGTONE_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                selectedToneUri = uri;
                txtTone.setText("Custom Tone Selected");
            }
        }
    }

    private void scheduleAlarm() {
        Calendar cal = Calendar.getInstance();
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("TONE_URI", selectedToneUri != null ? selectedToneUri.toString() : null);

        PendingIntent pi = PendingIntent.getBroadcast(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        }

        Toast.makeText(this, "Alarm set for " + String.format("%02d:%02d", hour, minute), Toast.LENGTH_SHORT).show();
    }

    private void cancelAlarm() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);

        Intent serviceIntent = new Intent(this, AlarmService.class);
        stopService(serviceIntent);

        Toast.makeText(this, "Alarm Cancelled", Toast.LENGTH_SHORT).show();
    }
}
