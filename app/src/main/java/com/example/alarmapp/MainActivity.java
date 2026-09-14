package com.example.alarmapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {
    private ArrayList<AlarmModel> alarms = new ArrayList<>();
    private ListView listView;
    private TextView txtNotice;
    private AlarmAdapter adapter;
    private static final int TONE_PICK_REQ = 777;

    private int editingIndex = -1;
    private int pendingHour, pendingMinute;
    private int pendingYear, pendingMonth, pendingDay;
    private String pendingMode = "DAILY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.alarmListView);
        txtNotice = findViewById(R.id.txtUpcomingNotice);
        Button btnAdd = findViewById(R.id.btnAddAlarm);

        loadAlarms();
        adapter = new AlarmAdapter();
        listView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            editingIndex = -1;
            promptScheduleMode();
        });

        checkAndRequestAppPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlarms();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void promptScheduleMode() {
        String[] options = {"Daily (Repeats every day)", "Once (Specific date)"};
        new AlertDialog.Builder(this)
            .setTitle("Alarm Frequency")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    pendingMode = "DAILY";
                    showTimePicker();
                } else {
                    pendingMode = "ONCE";
                    showDatePicker();
                }
            })
            .show();
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            pendingYear = year;
            pendingMonth = month;
            pendingDay = dayOfMonth;
            showTimePicker();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        dp.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dp.show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            pendingHour = hourOfDay;
            pendingMinute = minute;

            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            startActivityForResult(intent, TONE_PICK_REQ);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (reqCode == TONE_PICK_REQ && resCode == RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }
            Ringtone r = RingtoneManager.getRingtone(this, uri);
            String toneName = (r != null) ? r.getTitle(this) : "Default Alarm";

            if (editingIndex >= 0 && editingIndex < alarms.size()) {
                AlarmModel current = alarms.get(editingIndex);
                AlarmScheduler.cancel(this, current.id);

                current.hour = pendingHour;
                current.minute = pendingMinute;
                current.year = pendingYear;
                current.month = pendingMonth;
                current.day = pendingDay;
                current.mode = pendingMode;
                current.toneUri = uri.toString();
                current.toneName = toneName;

                AlarmScheduler.schedule(this, current);
                Toast.makeText(this, "Alarm Updated", Toast.LENGTH_SHORT).show();
            } else {
                int id = (int) System.currentTimeMillis();
                AlarmModel model = new AlarmModel(id, pendingHour, pendingMinute, pendingYear, pendingMonth, pendingDay, pendingMode, uri.toString(), toneName);
                alarms.add(model);
                AlarmScheduler.schedule(this, model);
                Toast.makeText(this, "Alarm Scheduled", Toast.LENGTH_SHORT).show();
            }

            saveAlarms();
            adapter.notifyDataSetChanged();
            updateNotice();
        }
    }

    private void updateNotice() {
        if (alarms.isEmpty()) {
            txtNotice.setText("No active alarms scheduled");
        } else {
            txtNotice.setText(alarms.size() + " active alarm(s) set in system clock");
        }
    }

    private void saveAlarms() {
        SharedPreferences sp = getSharedPreferences("alarms_db", MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        try {
            for (AlarmModel a : alarms) {
                JSONObject obj = new JSONObject();
                obj.put("id", a.id);
                obj.put("hour", a.hour);
                obj.put("minute", a.minute);
                obj.put("year", a.year);
                obj.put("month", a.month);
                obj.put("day", a.day);
                obj.put("mode", a.mode);
                obj.put("toneUri", a.toneUri);
                obj.put("toneName", a.toneName);
                arr.put(obj);
            }
            sp.edit().putString("list", arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    private void loadAlarms() {
        alarms.clear();
        SharedPreferences sp = getSharedPreferences("alarms_db", MODE_PRIVATE);
        String raw = sp.getString("list", "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                alarms.add(new AlarmModel(
                    o.getInt("id"),
                    o.getInt("hour"),
                    o.getInt("minute"),
                    o.optInt("year", 0),
                    o.optInt("month", 0),
                    o.optInt("day", 0),
                    o.optString("mode", "DAILY"),
                    o.getString("toneUri"),
                    o.getString("toneName")
                ));
            }
        } catch (Exception ignored) {}
        updateNotice();
    }

    private void checkAndRequestAppPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private class AlarmAdapter extends BaseAdapter {
        @Override
        public int getCount() { return alarms.size(); }
        @Override
        public Object getItem(int pos) { return alarms.get(pos); }
        @Override
        public long getItemId(int pos) { return pos; }
        @Override
        public View getView(int pos, View v, ViewGroup parent) {
            if (v == null) {
                v = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_alarm, parent, false);
            }
            AlarmModel a = alarms.get(pos);
            TextView txtTime = v.findViewById(R.id.txtTime);
            TextView txtAmPm = v.findViewById(R.id.txtAmPm);
            TextView txtBadge = v.findViewById(R.id.txtScheduleBadge);
            TextView txtTone = v.findViewById(R.id.txtToneTitle);
            Button btnDelete = v.findViewById(R.id.btnDelete);
            View card = v.findViewById(R.id.cardContent);

            int displayHour = a.hour % 12 == 0 ? 12 : a.hour % 12;
            txtTime.setText(String.format("%02d:%02d", displayHour, a.minute));
            txtAmPm.setText(a.hour >= 12 ? "PM" : "AM");
            txtTone.setText("Tone: " + a.toneName);

            if ("DAILY".equals(a.mode)) {
                txtBadge.setText("DAILY");
                txtBadge.setTextColor(0xFF22C55E);
                txtBadge.setBackgroundColor(0xFF14532D);
            } else {
                Calendar c = Calendar.getInstance();
                c.set(a.year, a.month, a.day);
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
                txtBadge.setText(sdf.format(c.getTime()).toUpperCase());
                txtBadge.setTextColor(0xFF38BDF8);
                txtBadge.setBackgroundColor(0xFF0C4A6E);
            }

            card.setOnClickListener(click -> {
                editingIndex = pos;
                promptScheduleMode();
            });

            btnDelete.setOnClickListener(btn -> {
                AlarmScheduler.cancel(MainActivity.this, a.id);
                alarms.remove(pos);
                saveAlarms();
                notifyDataSetChanged();
                updateNotice();
                Toast.makeText(MainActivity.this, "Alarm Deleted", Toast.LENGTH_SHORT).show();
            });

            return v;
        }
    }
}
