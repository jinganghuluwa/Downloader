package com.tzc.downloadmanager.test;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tongzhichao on 16-4-6.
 */
public class DownloadDao {

    private DBHelper dbHelper;

    public DownloadDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * 判断数据库中是不是有对应这个urlString的信息
     *
     * @return
     */
    public boolean unhasInfo(String urlString) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "select count(*) from download_info where urlString=?";
        Cursor cursor = db.rawQuery(sql, new String[]{urlString});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count == 0;
    }

    /**
     * 把线程信息保存在数据库里面
     *
     * @param infos
     */
    public void saveInfos(List<ThreadDownloadInfo> infos) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (ThreadDownloadInfo info : infos) {
            String sql = "insert into download_info(thread_id,start_pos, end_pos,compelete_size,urlString) values (?,?,?,?,?)";
            Object[] bindArgs =
                    {info.getThreadId(), info.getStartPos(), info.getEndPos(),
                            info.getCompleteSize(), info.getUrlString()};
            db.execSQL(sql, bindArgs);
        }

    }

    /**
     * 暂停之后，把当前数据保存在数据库中，该方法是从数据库中查询数据
     *
     * @return
     */
    public List<ThreadDownloadInfo> getInfos(String urlString) {
        List<ThreadDownloadInfo> list = new ArrayList<ThreadDownloadInfo>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "select thread_id, start_pos, end_pos,compelete_size, urlString from download_info where urlString=?";
        Cursor cursor = db.rawQuery(sql, new String[]{urlString});
        while (cursor.moveToNext()) {
            ThreadDownloadInfo info = new ThreadDownloadInfo(cursor.getInt(0),
                    cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getString(4));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    /**
     * 把当前的数据照片 存进数据库中
     *
     * @param threadId
     * @param completeSize
     */
    public void updateInfo(int threadId, int completeSize, String urlString) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "update download_info set compelete_size=? where thread_id=? and urlString=?";
        Object[] bindArgs =
                {completeSize, threadId, urlString};
        db.execSQL(sql, bindArgs);
    }

    /**
     * 关闭数据库
     */
    public void closeDB() {
        dbHelper.close();
    }

    /**
     * 下载完成之后，从数据库里面把这个任务的信息删除
     * 不同的任务对应不同的urlString
     *
     * @param urlString
     */
    public void deleteInfos(String urlString) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("download_info", "urlString=?", new String[]{urlString});
    }
}
