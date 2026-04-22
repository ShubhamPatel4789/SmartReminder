package com.shubham.reminderapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

public class AlarmScheduler {

    public static void schedule(Context ctx, Reminder r) {
        if (!r.isEnabled()) return;
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = buildPI(ctx, r);
        long trigger = r.getDatetimeMillis();
        if (!r.isRecurring() && trigger < System.currentTimeMillis()) return;
        if (Build.VERSION.SDK_INT >= 23)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
        else
            am.setExact(AlarmManager.RTC_WAKEUP, trigger, pi);
    }

    public static void cancel(Context ctx, int id) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctx, ReminderAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, id, i,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
    }

    private static PendingIntent buildPI(Context ctx, Reminder r) {
        Intent i = new Intent(ctx, ReminderAlarmReceiver.class);
        i.putExtra("id", r.getId());
        i.putExtra("title", r.getTitle());
        i.putExtra("message", r.getMessage() != null ? r.getMessage() : "");
        i.putExtra("category", r.getCategory() != null ? r.getCategory() : "");
        i.putExtra("recurring", r.isRecurring());
        i.putExtra("recur_type", r.getRecurType() != null ? r.getRecurType() : "");
        i.putExtra("recur_days", r.getRecurDays() != null ? r.getRecurDays() : "");
        i.putExtra("recur_dom", r.getRecurDayOfMonth());
        return PendingIntent.getBroadcast(ctx, r.getId(), i,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static long nextOccurrence(Reminder r) {
        Calendar now = Calendar.getInstance();
        Calendar base = Calendar.getInstance();
        base.setTimeInMillis(r.getDatetimeMillis());
        int hour = base.get(Calendar.HOUR_OF_DAY), min = base.get(Calendar.MINUTE);

        if ("DAILY".equals(r.getRecurType())) {
            Calendar next = Calendar.getInstance();
            next.set(Calendar.HOUR_OF_DAY, hour); next.set(Calendar.MINUTE, min); next.set(Calendar.SECOND, 0);
            if (!next.after(now)) next.add(Calendar.DAY_OF_YEAR, 1);
            return next.getTimeInMillis();
        }
        if ("WEEKLY".equals(r.getRecurType())) {
            String days = r.getRecurDays();
            if (days == null || days.isEmpty()) return -1;
            String[] parts = days.split(",");
            for (int offset = 0; offset <= 7; offset++) {
                Calendar next = Calendar.getInstance();
                next.add(Calendar.DAY_OF_YEAR, offset);
                next.set(Calendar.HOUR_OF_DAY, hour); next.set(Calendar.MINUTE, min); next.set(Calendar.SECOND, 0);
                if (!next.after(now)) continue;
                int dow = next.get(Calendar.DAY_OF_WEEK);
                for (String p : parts) if (Integer.parseInt(p.trim()) == dow) return next.getTimeInMillis();
            }
            return -1;
        }
        if ("MONTHLY".equals(r.getRecurType())) {
            Calendar next = Calendar.getInstance();
            next.set(Calendar.DAY_OF_MONTH, r.getRecurDayOfMonth());
            next.set(Calendar.HOUR_OF_DAY, hour); next.set(Calendar.MINUTE, min); next.set(Calendar.SECOND, 0);
            if (!next.after(now)) next.add(Calendar.MONTH, 1);
            return next.getTimeInMillis();
        }
        return -1;
    }
}
