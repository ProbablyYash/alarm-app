package com.example.alarmapp;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class AlarmTriggerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Force Screen ON and dismiss keyguard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (km != null) km.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        setContentView(R.layout.activity_alarm_trigger);

        TextView txtTime = findViewById(R.id.txtRingingTime);
        TextView txtLabel = findViewById(R.id.txtAlarmLabel);
        ImageView imgWallpaper = findViewById(R.id.imgWallpaper);
        View vOverlay = findViewById(R.id.vOverlay);
        Button btnDismiss = findViewById(R.id.btnDismiss);

        String timeStr = getIntent().getStringExtra("TIME_STR");
        String labelStr = getIntent().getStringExtra("LABEL");

        if (timeStr != null) txtTime.setText(timeStr);
        if (labelStr != null && !labelStr.trim().isEmpty()) {
            txtLabel.setText(labelStr);
        } else {
            txtLabel.setText("Alarm");
        }

        // Load cached global wallpaper from internal filesDir
        File wallpaperFile = new File(getFilesDir(), "alarm_wallpaper.jpg");
        if (wallpaperFile.exists()) {
            Bitmap bmp = BitmapFactory.decodeFile(wallpaperFile.getAbsolutePath());
            if (bmp != null) {
                imgWallpaper.setImageBitmap(bmp);
                imgWallpaper.setVisibility(View.VISIBLE);
                vOverlay.setVisibility(View.VISIBLE);
            }
        }

        btnDismiss.setOnClickListener(v -> {
            Intent stopIntent = new Intent(this, AlarmService.class);
            stopIntent.setAction("ACTION_DISMISS");
            startService(stopIntent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {}
}
