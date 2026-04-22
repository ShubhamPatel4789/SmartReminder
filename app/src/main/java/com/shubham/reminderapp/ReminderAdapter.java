package com.shubham.reminderapp;

import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReminderAdapter extends BaseAdapter {
    private Context ctx;
    private List<Reminder> list;
    private DatabaseHelper db;
    private SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy  hh:mm a", Locale.getDefault());

    public ReminderAdapter(Context ctx, List<Reminder> list) {
        this.ctx = ctx; this.list = list; this.db = new DatabaseHelper(ctx);
    }

    @Override public int getCount() { return list.size(); }
    @Override public Reminder getItem(int p) { return list.get(p); }
    @Override public long getItemId(int p) { return list.get(p).getId(); }

    @Override public View getView(int pos, View cv, ViewGroup parent) {
        if (cv == null) cv = LayoutInflater.from(ctx).inflate(R.layout.item_reminder, parent, false);
        Reminder r = list.get(pos);

        ((TextView) cv.findViewById(R.id.tv_emoji)).setText(emoji(r.getCategory()));
        ((TextView) cv.findViewById(R.id.tv_title)).setText(r.getTitle());
        ((TextView) cv.findViewById(R.id.tv_dt)).setText(sdf.format(new Date(r.getDatetimeMillis())));

        TextView tvRec = (TextView) cv.findViewById(R.id.tv_recur);
        if (r.isRecurring() && r.getRecurType() != null) {
            tvRec.setVisibility(View.VISIBLE); tvRec.setText(recurLabel(r));
        } else { tvRec.setVisibility(View.GONE); }

        cv.findViewById(R.id.color_bar).setBackgroundColor(barColor(r.getCategory()));

        Switch sw = (Switch) cv.findViewById(R.id.sw);
        sw.setOnCheckedChangeListener(null);
        sw.setChecked(r.isEnabled());
        sw.setOnCheckedChangeListener((btn, checked) -> {
            r.setEnabled(checked);
            db.setEnabled(r.getId(), checked);
            if (checked) AlarmScheduler.schedule(ctx, r);
            else AlarmScheduler.cancel(ctx, r.getId());
            cv.setAlpha(checked ? 1f : 0.5f);
        });
        cv.setAlpha(r.isEnabled() ? 1f : 0.5f);
        return cv;
    }

    private String emoji(String c) {
        if(c==null) return "\uD83D\uDD14";
        switch(c){
            case "Health / Exercise": return "\uD83D\uDCAA";
            case "Payment / Finance": return "\uD83D\uDCB0";
            case "Work": return "\uD83D\uDCBC";
            case "Personal": return "\uD83D\uDC64";
            default: return "\uD83D\uDD14";
        }
    }
    private int barColor(String c) {
        if(c==null) return Color.parseColor("#95a5a6");
        switch(c){
            case "Health / Exercise": return Color.parseColor("#27ae60");
            case "Payment / Finance": return Color.parseColor("#e67e22");
            case "Work": return Color.parseColor("#2980b9");
            case "Personal": return Color.parseColor("#8e44ad");
            default: return Color.parseColor("#95a5a6");
        }
    }
    private String recurLabel(Reminder r) {
        if("DAILY".equals(r.getRecurType())) return "Repeats daily";
        if("MONTHLY".equals(r.getRecurType())) return "Repeats monthly on day "+r.getRecurDayOfMonth();
        if("WEEKLY".equals(r.getRecurType()) && r.getRecurDays()!=null) {
            String[] names={"","Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
            StringBuilder sb=new StringBuilder("Weekly: ");
            for(String s:r.getRecurDays().split(",")) {
                int d=Integer.parseInt(s.trim());
                if(d>=1&&d<=7) sb.append(names[d]).append(" ");
            }
            return sb.toString().trim();
        }
        return "Recurring";
    }
}
