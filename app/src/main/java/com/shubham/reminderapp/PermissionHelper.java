package com.shubham.reminderapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

public final class PermissionHelper {
    public static final int REQ_POST_NOTIFICATIONS = 1101;

    private PermissionHelper() {}

    public interface PermissionCallback {
        void onPermissionResult(String permission, boolean granted);
    }

    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return alarmManager != null && alarmManager.canScheduleExactAlarms();
    }

    public static boolean hasPostNotificationsPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true;
        return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPostNotificationsPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPostNotificationsPermission(activity)) {
            activity.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
        }
    }

    public static void requestExactAlarmPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms(activity)) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
                // Some OEM builds may not expose this intent action.
            }
        }
    }

    public static void dispatchExactAlarmPermissionState(Context context, PermissionCallback callback) {
        if (callback != null) {
            callback.onPermissionResult(Manifest.permission.SCHEDULE_EXACT_ALARM, canScheduleExactAlarms(context));
        }
    }

    public static boolean dispatchPermissionResult(int requestCode, int[] grantResults, PermissionCallback callback) {
        if (requestCode != REQ_POST_NOTIFICATIONS) return false;
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (callback != null) callback.onPermissionResult(Manifest.permission.POST_NOTIFICATIONS, granted);
        return true;
    }
}
