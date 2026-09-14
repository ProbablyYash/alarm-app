package com.example.alarmapp;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends Activity {
    private ArrayList<AlarmModel> alarms = new ArrayList<>();
    private ListView listView;
    private AlarmAdapter adapter;
    private static final int TONE_PICK_REQ = 777;
    private int pendingHour, pendingMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.alarmListView);
        Button btnAdd = findViewById(R.id.btnAddAlarm);

        loadAlarms();
        adapter = new AlarmAdapter();
        listView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showTimePicker());
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
            String toneName = (r != null) ? r.getTitle(this) : "Alarm Sound";

            int id = (int) System.currentTimeMillis();
            AlarmModel model = new AlarmModel(id, pendingHour, pendingMinute, uri.toString(), toneName);
            alarms.add(model);
            saveAlarms();
            AlarmScheduler.schedule(this, model);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Alarm Scheduled", Toast.LENGTH_SHORT).show();
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
                    o.getString("toneUri"),
                    o.getString("toneName")
                ));
            }
        } catch (Exception ignored) {}
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
            TextView txtTone = v.findViewById(R.id.txtToneTitle);
            Button btnDelete = v.findViewById(R.id.btnDelete);

            int displayHour = a.hour % 12 == 0 ? 12 : a.hour % 12;
            String amPm = a.hour >= 12 ? "PM" : "AM";
            txtTime.setText(String.format("%02d:%02d %s", displayHour, a.minute, amPm));
            txtTone.setText(a.toneName);

            btnDelete.setOnClickListener(btn -> {
                AlarmScheduler.cancel(MainActivity.this, a.id);
                alarms.remove(pos);
                saveAlarms();
                notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Alarm Deleted", Toast.LENGTH_SHORT).show();
            });
            return v;
        }
    }
}
