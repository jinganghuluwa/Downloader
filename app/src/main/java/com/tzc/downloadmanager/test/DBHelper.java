package com.tzc.downloadmanager.test;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by tongzhichao on 16-4-6.
 */
public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "download.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v("TAG", "DBHelper-->conCreate()");
        String sql = "create table download_info(_id integer PRIMARY KEY AUTOINCREMENT, thread_id integer,start_pos integer, end_pos integer, compelete_size integer,urlString char)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
