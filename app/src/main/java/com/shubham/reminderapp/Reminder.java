package com.shubham.reminderapp;

public class Reminder {
    private int id;
    private String title, message, category, recurType, recurDays;
    private long datetimeMillis;
    private boolean recurring, enabled;
    private int recurDayOfMonth;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getMessage() { return message; }
    public void setMessage(String m) { this.message = m; }
    public String getCategory() { return category; }
    public void setCategory(String c) { this.category = c; }
    public long getDatetimeMillis() { return datetimeMillis; }
    public void setDatetimeMillis(long d) { this.datetimeMillis = d; }
    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean r) { this.recurring = r; }
    public String getRecurType() { return recurType; }
    public void setRecurType(String r) { this.recurType = r; }
    public String getRecurDays() { return recurDays; }
    public void setRecurDays(String r) { this.recurDays = r; }
    public int getRecurDayOfMonth() { return recurDayOfMonth; }
    public void setRecurDayOfMonth(int d) { this.recurDayOfMonth = d; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { this.enabled = e; }
}
