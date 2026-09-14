package com.example.alarmapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

public class AlarmTriggerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        TextView txtLabel = findViewById(R.id.txtAlarmLabel);
        ImageView imgWallpaper = findViewById(R.id.imgWallpaper);
        View vOverlay = findViewById(R.id.vOverlay);
        Button btnDismiss = findViewById(R.id.btnDismiss);

        String timeStr = getIntent().getStringExtra("TIME_STR");
        String labelStr = getIntent().getStringExtra("LABEL");
        String imageUriStr = getIntent().getStringExtra("IMAGE_URI");

        if (timeStr != null) txtTime.setText(timeStr);
        if (labelStr != null && !labelStr.trim().isEmpty()) {
            txtLabel.setText(labelStr);
        } else {
            txtLabel.setText("Alarm");
        }

        // Render custom wallpaper if user selected one
        if (imageUriStr != null && !imageUriStr.isEmpty()) {
            try {
                Uri imgUri = Uri.parse(imageUriStr);
                InputStream stream = getContentResolver().openInputStream(imgUri);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                if (bitmap != null) {
                    imgWallpaper.setImageBitmap(bitmap);
                    imgWallpaper.setVisibility(View.VISIBLE);
                    vOverlay.setVisibility(View.VISIBLE);
                }
            } catch (Exception ignored) {}
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
