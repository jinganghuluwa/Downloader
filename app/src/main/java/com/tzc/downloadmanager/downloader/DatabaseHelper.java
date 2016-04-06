package com.tzc.downloadmanager.downloader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库操作helper类
 *
 * @author tzc
 * @version v1.1
 * @date 2015-8-17
 */
public class DatabaseHelper extends SQLiteOpenHelper {


    /**
     * 数据库版本号
     */
    private static final int DATABASE_VERSION = 2;

    /**
     * 构造方法
     *
     * @param context
     */
    public DatabaseHelper(Context context) {
        super(context, DownLoaderConfig.DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 数据库创建
     *
     * @param db SQLiteDatabase
     *           markId 用来作为标示key,暂时没用上
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DownloadTaskDao.TABLE_NAME + "(task_id integer primary key," +
                "url text,fileName text,path text,startTime integer," +
                "finishTime integer,size integer,status integer,needReStart boolean,markId text)");

    }

    /**
     * 数据库升级
     *
     * @param db         SQLiteDatabase
     * @param oldVersion 旧版本号
     * @param newVersion 新版本号
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE " + DownloadTaskDao.TABLE_NAME + " add column markId text");
    }


}
