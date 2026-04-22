package com.shubham.reminderapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private ListView lv;
    private ReminderAdapter adapter;
    private DatabaseHelper db;
    private List<Reminder> list;
    static final int REQ_ADD=1, REQ_EDIT=2;
    private static final int REQ_POST_NOTIFICATIONS = 10;
    private static final int REQ_EXACT_ALARM = 11;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        db = new DatabaseHelper(this);
        list = new ArrayList<>();
        lv = (ListView) findViewById(R.id.lv);
        adapter = new ReminderAdapter(this, list);
        lv.setAdapter(adapter);
        load();

        ((Button) findViewById(R.id.btn_add)).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, AddEditReminderActivity.class), REQ_ADD);
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                Intent i = new Intent(MainActivity.this, AddEditReminderActivity.class);
                i.putExtra("reminder_id", list.get(pos).getId());
                startActivityForResult(i, REQ_EDIT);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override public boolean onItemLongClick(AdapterView<?> p, View v, final int pos, long id) {
                final Reminder r = list.get(pos);
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete?")
                    .setMessage("Delete \"" + r.getTitle() + "\"?")
                    .setPositiveButton("Delete", (d,w2) -> {
                        AlarmScheduler.cancel(MainActivity.this, r.getId());
                        db.delete(r.getId()); load();
                    })
                    .setNegativeButton("Cancel", null).show();
                return true;
            }
        });

        requestRuntimePermissions();
    }

    private void load() {
        list.clear(); list.addAll(db.getAll()); adapter.notifyDataSetChanged();
        boolean empty = list.isEmpty();
        lv.setVisibility(empty ? View.GONE : View.VISIBLE);
        findViewById(R.id.tv_empty).setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void requestRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !PermissionUtils.hasPostNotificationsPermission(this)) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
        }
        if (!PermissionUtils.canScheduleExactAlarms(this)) {
            try {
                PermissionUtils.requestExactAlarmPermission(this, REQ_EXACT_ALARM);
            } catch (Exception ignored) {
                Toast.makeText(this, "Exact alarm access denied. Using inexact reminders.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_EXACT_ALARM && !PermissionUtils.canScheduleExactAlarms(this)) {
            Toast.makeText(this, "Exact alarm access not granted. Using inexact reminders.", Toast.LENGTH_SHORT).show();
        }
        if(res==RESULT_OK) load();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIFICATIONS && !PermissionUtils.hasPostNotificationsPermission(this)) {
            Toast.makeText(this, "Notifications are disabled until permission is granted.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onResume() { super.onResume(); load(); }
}
