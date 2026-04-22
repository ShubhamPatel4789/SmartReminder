package com.shubham.reminderapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context ctx, Intent intent) {
        DatabaseHelper db = new DatabaseHelper(ctx);
        List<Reminder> list = db.getEnabled();
        for (Reminder r : list) {
            if (r.isRecurring()) {
                long next = AlarmScheduler.nextOccurrence(r);
                if (next > 0) { r.setDatetimeMillis(next); db.update(r); }
            }
            if (r.getDatetimeMillis() > System.currentTimeMillis())
                AlarmScheduler.schedule(ctx, r);
        }
    }
}
