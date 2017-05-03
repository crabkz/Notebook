package com.agsoft.notebook.Receiver;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.agsoft.notebook.DataBase.MDataBaseHelper;

/**
 * Created by 1 on 2016/9/14.
 */
public class OneShotAlarm extends BroadcastReceiver {
    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        long time_created = intent.getLongExtra("time_created", 0);
        SQLiteDatabase db = new MDataBaseHelper(context, "notebook").getWritableDatabase();
        Cursor query = db.query("clock", null, "time_created = ?", new String[]{String.valueOf(time_created)}, null, null, null);
        if (query.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put("note", query.getString(query.getColumnIndex("note")));
            values.put("time", query.getString(query.getColumnIndex("time")));
            values.put("time_millis", query.getLong(query.getColumnIndex("time_millis")));
            values.put("time_created", query.getLong(query.getColumnIndex("time_created")));
            db.insert("clocked", null, values);
            db.delete("clock", "_id = ?", new String[]{String.valueOf(query.getInt(query.getColumnIndex("_id")))});
        }
        Log.e("loglog", "onReceive: 闹铃响");
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone ringtone = RingtoneManager.getRingtone(context, notification);
    }
}
