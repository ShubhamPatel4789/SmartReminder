package com.shubham.reminderapp;

import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddEditReminderActivity extends Activity {
    private EditText etTitle, etMsg;
    private Spinner spCat;
    private Button btnDate, btnTime, btnSave;
    private CheckBox cbRec;
    private RadioGroup rgType;
    private RadioButton rbDaily, rbWeekly, rbMonthly;
    private LinearLayout llWeek, llMonth;
    private CheckBox[] cbDays = new CheckBox[7];
    private NumberPicker npDom;
    private Calendar cal = Calendar.getInstance();
    private DatabaseHelper db;
    private int editId = -1;
    private static final int REQ_POST_NOTIFICATIONS = 20;
    private static final int REQ_EXACT_ALARM = 21;
    private SimpleDateFormat dfDate = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
    private SimpleDateFormat dfTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_edit);
        db = new DatabaseHelper(this);
        etTitle   = (EditText)    findViewById(R.id.et_title);
        etMsg     = (EditText)    findViewById(R.id.et_msg);
        spCat     = (Spinner)     findViewById(R.id.sp_cat);
        btnDate   = (Button)      findViewById(R.id.btn_date);
        btnTime   = (Button)      findViewById(R.id.btn_time);
        btnSave   = (Button)      findViewById(R.id.btn_save);
        cbRec     = (CheckBox)    findViewById(R.id.cb_rec);
        rgType    = (RadioGroup)  findViewById(R.id.rg_type);
        rbDaily   = (RadioButton) findViewById(R.id.rb_daily);
        rbWeekly  = (RadioButton) findViewById(R.id.rb_weekly);
        rbMonthly = (RadioButton) findViewById(R.id.rb_monthly);
        llWeek    = (LinearLayout)findViewById(R.id.ll_week);
        llMonth   = (LinearLayout)findViewById(R.id.ll_month);
        npDom     = (NumberPicker)findViewById(R.id.np_dom);
        npDom.setMinValue(1); npDom.setMaxValue(28);

        int[] dayIds = {R.id.cb_sun,R.id.cb_mon,R.id.cb_tue,R.id.cb_wed,R.id.cb_thu,R.id.cb_fri,R.id.cb_sat};
        for (int i=0;i<7;i++) cbDays[i]=(CheckBox)findViewById(dayIds[i]);

        spCat.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Health / Exercise","Payment / Finance","Work","Personal","General"}));

        cal.add(Calendar.MINUTE, 10);
        updateBtns();

        editId = getIntent().getIntExtra("reminder_id",-1);
        if(editId!=-1) { setTitle("Edit Reminder"); loadReminder(); }
        else setTitle("New Reminder");

        btnDate.setOnClickListener(v -> new DatePickerDialog(this,
            (dp,y,m,d)->{cal.set(y,m,d); updateBtns();},
            cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show());

        btnTime.setOnClickListener(v -> new TimePickerDialog(this,
            (tp,h,m)->{cal.set(Calendar.HOUR_OF_DAY,h);cal.set(Calendar.MINUTE,m);cal.set(Calendar.SECOND,0);updateBtns();},
            cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),false).show());

        cbRec.setOnCheckedChangeListener((btn,ch)->updatePanels());
        rgType.setOnCheckedChangeListener((g,id)->updatePanels());
        btnSave.setOnClickListener(v->save());
        updatePanels();
        requestRuntimePermissions();
    }

    private void updateBtns() {
        btnDate.setText("\uD83D\uDCC5  " + dfDate.format(cal.getTime()));
        btnTime.setText("\u23F0  " + dfTime.format(cal.getTime()));
    }

    private void updatePanels() {
        boolean rec = cbRec.isChecked();
        rgType.setVisibility(rec?View.VISIBLE:View.GONE);
        llWeek.setVisibility(rec&&rbWeekly.isChecked()?View.VISIBLE:View.GONE);
        llMonth.setVisibility(rec&&rbMonthly.isChecked()?View.VISIBLE:View.GONE);
    }

    private void loadReminder() {
        Reminder r = db.get(editId);
        if(r==null) return;
        etTitle.setText(r.getTitle()); etMsg.setText(r.getMessage());
        cal.setTimeInMillis(r.getDatetimeMillis()); updateBtns();
        String[] cats={"Health / Exercise","Payment / Finance","Work","Personal","General"};
        for(int i=0;i<cats.length;i++) if(cats[i].equals(r.getCategory())) {spCat.setSelection(i);break;}
        cbRec.setChecked(r.isRecurring());
        if(r.isRecurring()&&r.getRecurType()!=null) {
            switch(r.getRecurType()){
                case "DAILY": rbDaily.setChecked(true); break;
                case "WEEKLY": rbWeekly.setChecked(true);
                    if(r.getRecurDays()!=null) for(String s:r.getRecurDays().split(",")) {
                        int d=Integer.parseInt(s.trim())-1; if(d>=0&&d<7) cbDays[d].setChecked(true);
                    } break;
                case "MONTHLY": rbMonthly.setChecked(true); npDom.setValue(r.getRecurDayOfMonth()); break;
            }
        }
        updatePanels();
    }

    private void save() {
        String title = etTitle.getText().toString().trim();
        if(title.isEmpty()) { etTitle.setError("Required"); return; }
        Reminder r = new Reminder();
        if(editId!=-1) r.setId(editId);
        r.setTitle(title); r.setMessage(etMsg.getText().toString().trim());
        r.setCategory(spCat.getSelectedItem().toString());
        r.setDatetimeMillis(cal.getTimeInMillis()); r.setEnabled(true);
        boolean rec = cbRec.isChecked(); r.setRecurring(rec);
        if(rec) {
            int cid = rgType.getCheckedRadioButtonId();
            if(cid==R.id.rb_daily) { r.setRecurType("DAILY"); }
            else if(cid==R.id.rb_weekly) {
                r.setRecurType("WEEKLY");
                StringBuilder days=new StringBuilder();
                for(int i=0;i<7;i++) if(cbDays[i].isChecked()) { if(days.length()>0)days.append(","); days.append(i+1); }
                if(days.length()==0) { Toast.makeText(this,"Pick at least one day",Toast.LENGTH_SHORT).show(); return; }
                r.setRecurDays(days.toString());
            } else if(cid==R.id.rb_monthly) {
                r.setRecurType("MONTHLY"); r.setRecurDayOfMonth(npDom.getValue());
            }
            long next=AlarmScheduler.nextOccurrence(r);
            if(next>0) r.setDatetimeMillis(next);
        }
        if(editId!=-1) { AlarmScheduler.cancel(this,editId); db.update(r); }
        else { long nid=db.insert(r); r.setId((int)nid); }

        if (!PermissionUtils.canScheduleExactAlarms(this)) {
            Toast.makeText(this, "Exact alarm access denied. Using inexact reminders.", Toast.LENGTH_SHORT).show();
        }
        AlarmScheduler.schedule(this,r);
        setResult(RESULT_OK); finish();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIFICATIONS && !PermissionUtils.hasPostNotificationsPermission(this)) {
            Toast.makeText(this, "Notifications are disabled until permission is granted.", Toast.LENGTH_SHORT).show();
        }
    }
}
