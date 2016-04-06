package com.tzc.downloadmanager.downloader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * 对数据库进行各种操作
 * Created by tzc on 2015/8/18.
 */
public class DownloadTaskDao {

    /**
     * 上下文
     */
    private Context mContext;

    /**
     * 表名
     */
    static final String TABLE_NAME = "downloadApkTask";

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    public DownloadTaskDao(Context context) {
        mContext = context;
    }

    /**
     * 创建或者更改
     *
     * @param taskInfo 下载任务信息
     * @return 返回true, 失败返回false
     */
    synchronized boolean createOrUpdate(DownloadTaskInfo taskInfo) {
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        //根据url查询
        Cursor cursor = readableDatabase.query(TABLE_NAME, null, "url=?",
                new String[]{taskInfo.getUrl()}, null, null, null);
        try {
            if (cursor.getCount() > 0) {
                ContentValues values = new ContentValues();
                values.put("fileName", taskInfo.getFileName());
                values.put("path", taskInfo.getPath());
                values.put("startTime", taskInfo.getStartTime());
                values.put("finishTime", taskInfo.getFinishTime());
                values.put("size", taskInfo.getSize());
                values.put("status", taskInfo.getStatus());
                values.put("needReStart", taskInfo.getReStart());
                return writableDatabase.update(TABLE_NAME, values, "url=?", new String[]{taskInfo.getUrl()}) > 0;
            } else {
                ContentValues values = new ContentValues();
                values.put("url", taskInfo.getUrl());
                values.put("fileName", taskInfo.getFileName());
                values.put("path", taskInfo.getPath());
                values.put("startTime", taskInfo.getStartTime());
                values.put("finishTime", taskInfo.getFinishTime());
                values.put("needReStart", taskInfo.getReStart());
                values.put("size", taskInfo.getSize());
                values.put("status", taskInfo.getStatus());
                return writableDatabase.insert(TABLE_NAME, "url", values) != -1;
            }
        } finally {
            cursor.close();
            writableDatabase.close();
            readableDatabase.close();
        }


    }

    /**
     * 更改
     *
     * @param taskInfo 下载任务状态
     * @return 成功返回true, 失败返回false
     */
    synchronized boolean update(DownloadTaskInfo taskInfo) {
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("fileName", taskInfo.getFileName());
            values.put("path", taskInfo.getPath());
            values.put("startTime", taskInfo.getStartTime());
            values.put("finishTime", taskInfo.getFinishTime());
            values.put("size", taskInfo.getSize());
            values.put("needReStart", taskInfo.getReStart());
            values.put("status", taskInfo.getStatus());
            return writableDatabase.update(TABLE_NAME, values, "url=?", new String[]{taskInfo.getUrl()}) > 0;
        } finally {
            writableDatabase.close();
        }

    }

    /**
     * 根据下载状态查找任务
     *
     * @param status 下载状态,具体值见MSageDownloadStatus
     * @return 返回任务列表
     * @see DownloadStatus
     */
    synchronized ArrayList<DownloadTaskInfo> queryByStatus(int status) {
        ArrayList<DownloadTaskInfo> tasks = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.query(TABLE_NAME, null, "status=?", new String[]{status + ""}, null, null, null);
        try {
            if (cursor.getCount() > 0) {
                DownloadTaskInfo task;
                while (cursor.moveToNext()) {
                    task = new DownloadTaskInfo(mContext);
                    task.setPath(cursor.getString(cursor.getColumnIndex("path")));
                    task.setFinishTime(cursor.getInt(cursor.getColumnIndex("finishTime")));
                    task.setFileName(cursor.getString(cursor.getColumnIndex("fileName")));
                    task.setSize(cursor.getInt(cursor.getColumnIndex("size")));
                    task.setStartTime(cursor.getInt(cursor.getColumnIndex("startTime")));
                    task.setStatus(cursor.getInt(cursor.getColumnIndex("status")), false);
                    task.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    task.setReStart(cursor.getInt(cursor.getColumnIndex("needReStart")) == 1 ? true : false, false);
                    task.setId(cursor.getInt(cursor.getColumnIndex("task_id")));
                    tasks.add(task);
                }


            }
            return tasks;
        } finally {
            cursor.close();
            readableDatabase.close();
        }
    }

    /**
     * 查找未完成任务
     *
     * @return 任务列表
     */
    synchronized ArrayList<DownloadTaskInfo> queryUnFinishedTasks() {
        ArrayList<DownloadTaskInfo> tasks = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.query(TABLE_NAME, null, "status!=?", new String[]{"5"}, null, null, null);
        try {
            if (cursor.getCount() > 0) {
                DownloadTaskInfo task;
                while (cursor.moveToNext()) {
                    task = new DownloadTaskInfo(mContext);
                    task.setPath(cursor.getString(cursor.getColumnIndex("path")));
                    task.setFinishTime(cursor.getInt(cursor.getColumnIndex("finishTime")));
                    task.setFileName(cursor.getString(cursor.getColumnIndex("fileName")));
                    task.setSize(cursor.getInt(cursor.getColumnIndex("size")));
                    task.setStartTime(cursor.getInt(cursor.getColumnIndex("startTime")));
                    task.setReStart(cursor.getInt(cursor.getColumnIndex("needReStart")) == 1 ? true : false, false);
                    task.setStatus(cursor.getInt(cursor.getColumnIndex("status")), false);
                    task.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    task.setId(cursor.getInt(cursor.getColumnIndex("task_id")));
                    tasks.add(task);
                }


            }
            return tasks;
        } finally {
            cursor.close();
            readableDatabase.close();
        }

    }

    /**
     * 通过url查找任务
     *
     * @return 任务信息
     */
    synchronized DownloadTaskInfo queryDownloadTask(String url) {
        if (TextUtils.isEmpty(url))
            return null;
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();
        DownloadTaskInfo task = new DownloadTaskInfo(mContext, url);
        Cursor cursor = readableDatabase.query(TABLE_NAME, null, "url=?", new String[]{url}, null, null, null);
        try {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                task.setPath(cursor.getString(cursor.getColumnIndex("path")));
                task.setFinishTime(cursor.getInt(cursor.getColumnIndex("finishTime")));
                task.setFileName(cursor.getString(cursor.getColumnIndex("fileName")));
                task.setSize(cursor.getInt(cursor.getColumnIndex("size")));
                task.setReStart(cursor.getInt(cursor.getColumnIndex("needReStart")) == 1, false);
                task.setStartTime(cursor.getInt(cursor.getColumnIndex("startTime")));
                task.setId(cursor.getInt(cursor.getColumnIndex("task_id")));
                task.setStatus(cursor.getInt(cursor.getColumnIndex("status")), false);
                return task;
            } else {
                return null;
            }
        } catch (Error e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            readableDatabase.close();
        }

        return null;
    }

    /**
     * 根据包名查找任务
     *
     * @param packageName 包名
     * @return 任务列表
     */
    synchronized ArrayList<DownloadTaskInfo> queryDownloadTaskByPkg(String packageName) {
        ArrayList<DownloadTaskInfo> tasks = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();

        Cursor cursor = readableDatabase.query(TABLE_NAME, null, "packageName=?", new String[]{packageName}, null, null, null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    DownloadTaskInfo task = new DownloadTaskInfo(mContext);
                    task.setPath(cursor.getString(cursor.getColumnIndex("path")));
                    task.setFinishTime(cursor.getInt(cursor.getColumnIndex("finishTime")));
                    task.setFileName(cursor.getString(cursor.getColumnIndex("fileName")));
                    task.setSize(cursor.getInt(cursor.getColumnIndex("size")));
                    task.setReStart(cursor.getInt(cursor.getColumnIndex("needReStart")) == 1 ? true : false, false);
                    task.setStartTime(cursor.getInt(cursor.getColumnIndex("startTime")));
                    task.setStatus(cursor.getInt(cursor.getColumnIndex("status")), false);
                    task.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    task.setId(cursor.getInt(cursor.getColumnIndex("task_id")));
                    tasks.add(task);
                }
            }
            return tasks;
        } finally {
            cursor.close();
            readableDatabase.close();
        }


    }

    /**
     * 删除任务
     *
     * @param task 任务信息
     */
    synchronized boolean deleteDownloadTask(DownloadTaskInfo task) {
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        try {
            return writableDatabase.delete(TABLE_NAME, "url=?", new String[]{task.getUrl()}) > 0;
        } finally {
            writableDatabase.close();
        }
    }

}
