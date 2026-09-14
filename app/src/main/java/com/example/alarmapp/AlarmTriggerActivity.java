package com.example.alarmapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AlarmTriggerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Turn on display over locked screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        setContentView(R.layout.activity_alarm_trigger);

        TextView txtTime = findViewById(R.id.txtRingingTime);
        String timeStr = getIntent().getStringExtra("TIME_STR");
        if (timeStr != null) txtTime.setText(timeStr);

        Button btnDismiss = findViewById(R.id.btnDismiss);
        btnDismiss.setOnClickListener(v -> {
            Intent stopIntent = new Intent(this, AlarmService.class);
            stopIntent.setAction("ACTION_DISMISS");
            startService(stopIntent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent bypassing without clicking Dismiss
    }
}
