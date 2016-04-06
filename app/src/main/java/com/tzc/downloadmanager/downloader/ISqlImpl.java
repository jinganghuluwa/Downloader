package com.tzc.downloadmanager.downloader;

import android.content.Context;

import java.util.ArrayList;

/**
 * 数据库操作类
 *
 * @author tzc
 * @version v1.1
 * @date 2015-8-17
 */
public class ISqlImpl {
    /**
     * Dao实例
     */
    private DownloadTaskDao mDao;
    /**
     * 上下文
     */
    private Context mContext = null;

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    public ISqlImpl(Context context) {
        mContext = context;
    }

    /**
     * 单例同步锁
     */
    private final static Object mLock = new Object();

    /**
     * 获取Dao实例
     */
    private DownloadTaskDao getDao() {
        if (mDao == null) {
            synchronized (mLock){
                if(mDao==null){
                    mDao = new DownloadTaskDao(mContext);
                }
            }

        }
        return mDao;
    }

    /**
     * 添加任务
     *
     * @param taskInfo 任务信息
     */
    public void addDownloadTask(DownloadTaskInfo taskInfo) {
        if (taskInfo == null) {
            return;
        }
        getDao().createOrUpdate(taskInfo);
    }

    /**
     * 更改任务
     *
     * @param taskInfo 任务信息
     */
    public void updateDownloadTask(DownloadTaskInfo taskInfo) {
        if (taskInfo == null) {
            return;
        }
        getDao().update(taskInfo);
    }

    /**
     * 查找任务
     *
     * @param url url链接
     * @return 任务信息
     */
    public DownloadTaskInfo queryDownloadTask(String url) {
        return getDao().queryDownloadTask(url);
    }

    /**
     * 通过下载状态查找任务
     *
     * @param status 下载状态
     * @return 任务信息列表
     */
    public ArrayList<DownloadTaskInfo> queryByStatus(int status) {
        return getDao().queryByStatus(status);
    }

    /**
     * 获取未完成任务
     *
     * @return 任务信息列表
     */
    public ArrayList<DownloadTaskInfo> getUnFinishedTasks() {
        return getDao().queryUnFinishedTasks();
    }


    /**
     * 删除任务
     *
     * @param taskInfo 任务信息
     */
    public void deleteDownloadTask(DownloadTaskInfo taskInfo) {
        if (taskInfo == null) {
            return;
        }
        getDao().deleteDownloadTask(taskInfo);
    }

    /**
     * 通过包名查找任务
     *
     * @param packageName 包名
     * @return 任务信息列表
     */
    public ArrayList<DownloadTaskInfo> queryDownloadTaskByPkg(String packageName) {
        return getDao().queryDownloadTaskByPkg(packageName);
    }


}
