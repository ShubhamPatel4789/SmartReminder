package com.shubham.reminderapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

public class ReminderAlarmReceiver extends BroadcastReceiver {
    static final String CH = "smart_reminders_v1";

    @Override public void onReceive(Context ctx, Intent intent) {
        int id        = intent.getIntExtra("id", 0);
        String title  = intent.getStringExtra("title");
        String msg    = intent.getStringExtra("message");
        String cat    = intent.getStringExtra("category");
        boolean rec   = intent.getBooleanExtra("recurring", false);
        String rtype  = intent.getStringExtra("recur_type");
        String rdays  = intent.getStringExtra("recur_days");
        int rdom      = intent.getIntExtra("recur_dom", 1);

        showNotification(ctx, id, title, msg, cat);

        if (rec) {
            Reminder r = new Reminder();
            r.setId(id); r.setTitle(title); r.setMessage(msg);
            r.setRecurring(true); r.setRecurType(rtype);
            r.setRecurDays(rdays); r.setRecurDayOfMonth(rdom); r.setEnabled(true);
            long next = AlarmScheduler.nextOccurrence(r);
            if (next > 0) {
                r.setDatetimeMillis(next);
                new DatabaseHelper(ctx).update(r);
                AlarmScheduler.schedule(ctx, r);
            }
        }
    }

    private void showNotification(Context ctx, int id, String title, String msg, String cat) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CH, "Smart Reminders", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(ch);
        }
        Intent tap = new Intent(ctx, MainActivity.class);
        tap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(ctx, id, tap,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= 26)
            b = new Notification.Builder(ctx, CH);
        else
            b = new Notification.Builder(ctx);

        b.setSmallIcon(android.R.drawable.ic_popup_reminder)
         .setContentTitle(emoji(cat) + " " + title)
         .setContentText(msg != null && !msg.isEmpty() ? msg : "")
         .setStyle(new Notification.BigTextStyle().bigText(msg != null ? msg : ""))
         .setContentIntent(pi).setAutoCancel(true)
         .setSound(sound).setVibrate(new long[]{0,300,100,300});

        if (Build.VERSION.SDK_INT < 26) b.setPriority(Notification.PRIORITY_HIGH);
        nm.notify(id, b.build());
    }

    private String emoji(String cat) {
        if (cat == null) return "\uD83D\uDD14";
        switch(cat) {
            case "Health / Exercise": return "\uD83D\uDCAA";
            case "Payment / Finance": return "\uD83D\uDCB0";
            case "Work":              return "\uD83D\uDCBC";
            case "Personal":          return "\uD83D\uDC64";
            default:                  return "\uD83D\uDD14";
        }
    }
}
