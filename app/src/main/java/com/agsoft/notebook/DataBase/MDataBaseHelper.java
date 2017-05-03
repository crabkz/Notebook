package com.agsoft.notebook.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 1 on 2016/9/14.
 */
public class MDataBaseHelper extends SQLiteOpenHelper {
    public MDataBaseHelper(Context context, String name) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table clock(_id integer primary key autoincrement,time text,time_millis integer,time_created integer,note text)");
        db.execSQL("create table clocked(_id integer primary key autoincrement,time text,time_millis integer,time_created integer,note text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
