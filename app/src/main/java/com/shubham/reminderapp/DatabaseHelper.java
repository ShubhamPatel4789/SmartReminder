package com.shubham.reminderapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "reminders.db";
    private static final int DB_VERSION = 1;
    static final String TABLE = "reminders";
    static final String C_ID = "_id", C_TITLE = "title", C_MSG = "message";
    static final String C_CAT = "category", C_DT = "dt_millis";
    static final String C_REC = "recurring", C_RTYPE = "recur_type";
    static final String C_RDAYS = "recur_days", C_RDOM = "recur_dom", C_EN = "enabled";

    public DatabaseHelper(Context ctx) { super(ctx, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + "("
            + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + C_TITLE + " TEXT NOT NULL," + C_MSG + " TEXT," + C_CAT + " TEXT,"
            + C_DT + " INTEGER," + C_REC + " INTEGER DEFAULT 0,"
            + C_RTYPE + " TEXT," + C_RDAYS + " TEXT,"
            + C_RDOM + " INTEGER DEFAULT 1," + C_EN + " INTEGER DEFAULT 1)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int o, int n) { db.execSQL("DROP TABLE IF EXISTS "+TABLE); onCreate(db); }

    public long insert(Reminder r) { SQLiteDatabase db=getWritableDatabase(); long id=db.insert(TABLE,null,cv(r)); db.close(); return id; }
    public void update(Reminder r) { SQLiteDatabase db=getWritableDatabase(); db.update(TABLE,cv(r),C_ID+"=?",new String[]{String.valueOf(r.getId())}); db.close(); }
    public void delete(int id) { SQLiteDatabase db=getWritableDatabase(); db.delete(TABLE,C_ID+"=?",new String[]{String.valueOf(id)}); db.close(); }
    public void setEnabled(int id, boolean en) { SQLiteDatabase db=getWritableDatabase(); ContentValues cv=new ContentValues(); cv.put(C_EN,en?1:0); db.update(TABLE,cv,C_ID+"=?",new String[]{String.valueOf(id)}); db.close(); }

    public Reminder get(int id) {
        SQLiteDatabase db=getReadableDatabase();
        Cursor c=db.query(TABLE,null,C_ID+"=?",new String[]{String.valueOf(id)},null,null,null);
        Reminder r=null; if(c.moveToFirst()) r=from(c); c.close(); db.close(); return r;
    }
    public List<Reminder> getAll() {
        SQLiteDatabase db=getReadableDatabase();
        Cursor c=db.query(TABLE,null,null,null,null,null,C_DT+" ASC");
        List<Reminder> list=new ArrayList<>(); while(c.moveToNext()) list.add(from(c)); c.close(); db.close(); return list;
    }
    public List<Reminder> getEnabled() {
        SQLiteDatabase db=getReadableDatabase();
        Cursor c=db.query(TABLE,null,C_EN+"=1",null,null,null,null);
        List<Reminder> list=new ArrayList<>(); while(c.moveToNext()) list.add(from(c)); c.close(); db.close(); return list;
    }

    private ContentValues cv(Reminder r) {
        ContentValues cv=new ContentValues();
        cv.put(C_TITLE,r.getTitle()); cv.put(C_MSG,r.getMessage()); cv.put(C_CAT,r.getCategory());
        cv.put(C_DT,r.getDatetimeMillis()); cv.put(C_REC,r.isRecurring()?1:0);
        cv.put(C_RTYPE,r.getRecurType()); cv.put(C_RDAYS,r.getRecurDays());
        cv.put(C_RDOM,r.getRecurDayOfMonth()); cv.put(C_EN,r.isEnabled()?1:0);
        return cv;
    }
    private Reminder from(Cursor c) {
        Reminder r=new Reminder();
        r.setId(c.getInt(c.getColumnIndexOrThrow(C_ID)));
        r.setTitle(c.getString(c.getColumnIndexOrThrow(C_TITLE)));
        r.setMessage(c.getString(c.getColumnIndexOrThrow(C_MSG)));
        r.setCategory(c.getString(c.getColumnIndexOrThrow(C_CAT)));
        r.setDatetimeMillis(c.getLong(c.getColumnIndexOrThrow(C_DT)));
        r.setRecurring(c.getInt(c.getColumnIndexOrThrow(C_REC))==1);
        r.setRecurType(c.getString(c.getColumnIndexOrThrow(C_RTYPE)));
        r.setRecurDays(c.getString(c.getColumnIndexOrThrow(C_RDAYS)));
        r.setRecurDayOfMonth(c.getInt(c.getColumnIndexOrThrow(C_RDOM)));
        r.setEnabled(c.getInt(c.getColumnIndexOrThrow(C_EN))==1);
        return r;
    }
}
