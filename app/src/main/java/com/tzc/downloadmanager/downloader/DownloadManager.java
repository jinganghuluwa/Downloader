package com.tzc.downloadmanager.downloader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * 下载器管理类,负责开始,暂停,删除等一系列下载操作
 *
 * @author tzc
 * @version v1.1
 * @date 2015-8-17
 */
public class DownloadManager {
    /**
     * 上下文
     */
    private Context mContext = null;
    /**
     * 下载路径
     */
    private static String mRootPath = "";
    /**
     * 正在下载list
     */
    private ArrayList<DownloadTaskInfo> mRunningList = new ArrayList<>();
    /**
     * 等待下载list
     */
    private ArrayList<DownloadTaskInfo> mCacheList = new ArrayList<>();
    /**
     * cpu核心数
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /**
     * 最大任务数量
     */
    private int mMaxTaskCount = CPU_COUNT * 2;
    /**
     * 下载监听
     */
    private DownloadListener mDownloadListener;
    /**
     * 单例线程同步锁
     */
    private static final Object mLock = new Object();
    /**
     * 静态实例
     */
    private static DownloadManager mInstance;
    /**
     * 下载器版本
     *
     * @deprecated 标示版本的值, 外面没用上
     */
    public static final String VERSION = "1.0.0";

    /**
     * 下载器动作常量值
     */
    private static final int ADD = 10000;
    private static final int ADDED = 10001;
    private static final int ADDWAIT = 10002;
    private static final int PAUSE = 10003;
    private static final int START = 10004;
    private static final int FIRSTSATRT = 10005;
    private static final int PROGRESSUPDATE = 10006;
    private static final int SUCCESS = 10007;
    private static final int ERROR = 10008;

    /**
     * 主线程handler用来回调,保证回调在主线程中,方便调用者绘制ui
     */
    private static final MainThreadHandler mMainThreadHandler = new MainThreadHandler();

    /**
     * 私有构造方法
     *
     * @param context  上下文
     * @param rootPath 下载文件保存路径
     */
    private DownloadManager(Context context, String rootPath) {
        mContext = context;
        mRootPath = rootPath;
    }

    /**
     * 获取MSageDownloadManager实例
     *
     * @param context  上下文
     * @param rootPath 下载路径
     * @return MSageDownloadManager实例
     */
    public static DownloadManager getInstance(Context context, String rootPath) {
        if (mInstance == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new DownloadManager(context, rootPath);
                }
            }
        }
        return mInstance;
    }

    /**
     * 获得下载路径
     *
     * @return 返回下载路径, String类型
     */
    public static String getRootPath() {
        return mRootPath;
    }

    /**
     * 获得数据库名字
     *
     * @return 返回数据库名字, String类型
     */
    public static String getDataBaseName() {
        return DownLoaderConfig.DATABASE_NAME;
    }

    /**
     * 添加任务
     *
     * @param taskInfo 任务信息
     */
    public synchronized void addTask(DownloadTaskInfo taskInfo) {
        if (taskInfo == null || !taskInfo.urlIsValid()) {
            return;
        }
        if (taskInfo.getContext() == null) {
            taskInfo.setContext(mContext);
        }
        ISqlImpl mSageISql = new ISqlImpl(mContext);
        DownloadTaskInfo sqTaskInfo;

        //在数据库查找任务
        sqTaskInfo = mSageISql.queryDownloadTask(taskInfo.getUrl());
        if (sqTaskInfo == null || !sqTaskInfo.equals(taskInfo)) {
            mSageISql.addDownloadTask(taskInfo);
        }
    }

    /**
     * 删除任务
     *
     * @param taskInfo taskInfo
     */
    public void deleteTask(DownloadTaskInfo taskInfo) {
        if (taskInfo == null || !taskInfo.urlIsValid()) {
            return;
        }
        resfreshList(taskInfo);
        taskInfo.delete();
    }

    /**
     * 清除数据库14天未完成任务
     */
    public void deleteDataBaseFourteenDays() {
        ArrayList<DownloadTaskInfo> tasks;
        ISqlImpl sageISql = new ISqlImpl(mContext);
        tasks = sageISql.getUnFinishedTasks();
        if (tasks != null && tasks.size() != 0) {
            for (DownloadTaskInfo task : tasks) {
                if (System.currentTimeMillis() - task.getStartTime() > (DownLoaderConfig.CLEAN_HISTORY_INTERVAL)) {
                    deleteTask(task);
                }
            }
        }
    }

    /**
     * 删除7天文件
     */
    public void deleteFileSevenDays() {
        File flooder = new File(mRootPath);
        if (flooder.isDirectory()) {
            File[] files = flooder.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                if (file.isFile() && (System.currentTimeMillis() - file.lastModified() > (DownLoaderConfig.CLEAN_APK_OUTDATE))) {
                    file.delete();
                }
            }
        }
    }

    /**
     * 根据包名查找任务
     *
     * @param packageName 要查找的任务的包名
     * @return ArrayList<DownloadTaskInfo> 查找到的任务集合,没有则返回null
     */
    public ArrayList<DownloadTaskInfo> queryDownloadTaskByPkg(String packageName) {
        ArrayList<DownloadTaskInfo> tasks;
        ISqlImpl mSageISql = new ISqlImpl(mContext);
        tasks = mSageISql.queryDownloadTaskByPkg(packageName);
        return tasks;
    }

    /**
     * 根据包名查找一个下载完成的url
     *
     * @param packageName 查询的包名
     * @return 查询到的url, 没有则返回""
     */
    public String quertDownloadTaskUrlByPkg(String packageName) {
        ArrayList<DownloadTaskInfo> tasks = queryDownloadTaskByPkg(packageName);
        if (tasks == null || tasks.size() == 0) {
            return "";
        }
        return tasks.get(0).getUrl();
    }

    /**
     * 开启任务
     *
     * @param taskInfo 任务信息
     * @return 开启成功返回true, 失败返回false
     */
    public synchronized boolean start(DownloadTaskInfo taskInfo) {
        //是否开启成功
        boolean startSuccess = false;
        //是否开启任务,队列满不会开启
        boolean startTask = false;

        if (taskInfo == null || !taskInfo.urlIsValid()) {
            Msg msg = new Msg(mDownloadListener, taskInfo);
            msg.mError = DownloadException.DOWNLOAD_TASK_NOT_VALID;
            mMainThreadHandler.sendMessage(mMainThreadHandler.obtainMessage(ERROR, msg));
            return false;
        }
        if (!isRunning(taskInfo)) {
            if (mRunningList.size() < mMaxTaskCount) {
                mRunningList.add(taskInfo);
                startTask = true;
            } else {
                //缓存队列是否存在该任务
                boolean added = false;
                for (int i = 0; i < mCacheList.size(); i++) {
                    if (mCacheList.get(i).getUrl().equals(taskInfo.getUrl())) {
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    mCacheList.add(taskInfo);
                    addTask(taskInfo);
                    mMainThreadHandler.sendMessage(mMainThreadHandler.obtainMessage(ADDWAIT, new Msg(mDownloadListener, taskInfo)));
                } else {
                    mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "已加入等待队列", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        } else {
            mMainThreadHandler.sendMessage(mMainThreadHandler.obtainMessage(ADDED, new Msg(mDownloadListener, taskInfo)));
            return false;
        }

        if (taskInfo.getContext() == null) {
            taskInfo.setContext(mContext);
        }

        ISqlImpl mSageISql = new ISqlImpl(mContext);

        DownloadTaskInfo sqTaskInfo;

        sqTaskInfo = mSageISql.queryDownloadTask(taskInfo.getUrl());

        if (sqTaskInfo != null && !sqTaskInfo.equals(taskInfo)) {
            taskInfo.setDownloadTask(sqTaskInfo);
        }
        if (startTask) {
            taskInfo.start(mContext, listener);
            startSuccess = true;
        }

        return startSuccess;
    }

    /**
     * 设置监听
     *
     * @param listener 下载状态监听
     */
    public void setListener(DownloadListener listener) {
        mDownloadListener = listener;
    }

    /**
     * 根据url获取task
     *
     * @param url 要查询的url
     * @return 查询到的任务信息, 没有查询到则返回null
     */
    public DownloadTaskInfo getTask(String url) {
        ISqlImpl mSageISql = new ISqlImpl(mContext);

        DownloadTaskInfo sqTaskInfo;
        sqTaskInfo = mSageISql.queryDownloadTask(url);
        return sqTaskInfo;
    }


    /**
     * 暂停所有任务
     */
    public void pauseAllTasks() {
        if (mRunningList != null && mRunningList.size() != 0) {
            for (DownloadTaskInfo task : mRunningList) {
                task.pause();
                task.setStatus(DownloadStatus.STATUS_PAUSE, true);
            }
        }
        if (mRunningList != null) {
            mRunningList.clear();
        }
        if (mCacheList != null && mCacheList.size() != 0) {
            for (DownloadTaskInfo task : mCacheList) {
                task.pause();
                task.setStatus(DownloadStatus.STATUS_PAUSE, true);
            }
        }
        if (mCacheList != null) {
            mCacheList.clear();
        }
    }


    /**
     * 重启数据库未完成
     */
    public void continueTasksInDataBase() {
        ISqlImpl mSageISql = new ISqlImpl(mContext);
        ArrayList<DownloadTaskInfo> taskIfs;
        taskIfs = mSageISql.getUnFinishedTasks();
        if (taskIfs != null && taskIfs.size() != 0) {
            for (DownloadTaskInfo task : taskIfs) {
                if (task.getReStart()) {
                    start(task);
                }

            }
        }
    }


    /**
     * 暂停或者继续,根据当前状态
     *
     * @param taskInfo 任务信息
     */
    public void pauseOrContinue(DownloadTaskInfo taskInfo) {
        if (taskInfo.getStatus() == DownloadStatus.STATUS_RUNNING) {
            pause(taskInfo);
        } else {
            start(taskInfo);
        }
    }

    /**
     * 暂停任务.
     *
     * @param taskInfo 任务信息
     */
    public void pause(DownloadTaskInfo taskInfo) {
        if (taskInfo == null) {
            return;
        }
        if (!isRunning(taskInfo)) {
            return;
        }
        for (int i = 0; i < mRunningList.size(); i++) {
            if (mRunningList.get(i).getUrl().equals(taskInfo.getUrl())) {
                taskInfo = mRunningList.get(i);
                break;
            }
        }
        if (taskInfo.getContext() == null) {
            taskInfo.setContext(mContext);
        }
        switch (taskInfo.getStatus()) {
            case DownloadStatus.STATUS_PAUSE:
                mMainThreadHandler.sendMessage(mMainThreadHandler.obtainMessage(PAUSE, new Msg(mDownloadListener, taskInfo)));
                break;
            case DownloadStatus.STATUS_RUNNING:
                taskInfo.pause();
                taskInfo.setStatus(DownloadStatus.STATUS_PAUSE, true);
                Msg mg = new Msg(mDownloadListener, taskInfo);
                mMainThreadHandler.sendMessage(mMainThreadHandler.obtainMessage(PAUSE, mg));
                resfreshList(taskInfo);
                break;
            default:
                Msg mssg = new Msg(mDownloadListener, taskInfo);
                mssg.mError = DownloadException.DOWNLOAD_TASK_NOT_VALID;
                mMainThreadHandler.sendMessage(mMainThreadHandler.obtainMessage(ERROR, mssg));
                break;
        }
    }

    /**
     * 判断一个任务是否处于运行中,即是否在runningList中
     *
     * @param taskInfo 要判断的任务
     * @return 如果处于运行中返回true, 反之返回false
     */
    private boolean isRunning(DownloadTaskInfo taskInfo) {
        for (int i = 0; i < mRunningList.size(); i++) {
            if (mRunningList.get(i).getUrl().equals(taskInfo.getUrl())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 刷新队列,在runningList中移除任务,如果cacheList中有任务,将第一条移动到runningList中
     *
     * @param taskInfo 要从runningList中移除的任务
     */
    private void resfreshList(DownloadTaskInfo taskInfo) {
        if (isRunning(taskInfo)) {
            mRunningList.remove(taskInfo);
            if (mCacheList != null && mCacheList.size() != 0) {
                start(mCacheList.get(0));
                mCacheList.remove(0);
            }
        }

    }

    /**
     * 下载监听,通过该监听收集下载回调,然后在主线程中通知给开发者传入的lisener
     */
    private DownloadListener listener = new DownloadListener() {

        @Override
        public void onAdd(DownloadTaskInfo taskInfo) {
            mMainThreadHandler.sendMessage(mMainThreadHandler
                    .obtainMessage(ADD,
                            new Msg(mDownloadListener, taskInfo)));

        }

        @Override
        public void onAdded(DownloadTaskInfo taskInfo) {
            mMainThreadHandler.sendMessage(mMainThreadHandler
                    .obtainMessage(ADDED, new Msg(mDownloadListener, taskInfo)));
        }


        @Override
        public void onAddWait(DownloadTaskInfo taskInfo) {
            mMainThreadHandler.sendMessage(mMainThreadHandler
                    .obtainMessage(ADDWAIT,
                            new Msg(mDownloadListener, taskInfo)));
        }


        @Override
        public void onPause(DownloadTaskInfo taskInfo) {
            mMainThreadHandler.sendMessage(mMainThreadHandler
                    .obtainMessage(PAUSE,
                            new Msg(mDownloadListener, taskInfo)));
        }

        @Override
        public void onStart(DownloadTaskInfo taskInfo) {
            mMainThreadHandler.sendMessage(mMainThreadHandler
                    .obtainMessage(START,
                            new Msg(mDownloadListener, taskInfo)));
        }

        @Override
        public void onFirstStart(DownloadTaskInfo taskInfo) {
            mMainThreadHandler.sendMessage(mMainThreadHandler
                    .obtainMessage(FIRSTSATRT,
                            new Msg(mDownloadListener, taskInfo)));
        }

        @Override
        public void onProgressUpdate(Integer progress, DownloadTaskInfo taskInfo) {
            Msg msg = new Msg(mDownloadListener, taskInfo);
            msg.mProgress = progress;
            mMainThreadHandler.sendMessage(mMainThreadHandler.obtainMessage(PROGRESSUPDATE, msg));
        }

        @Override
        public void onSuccess(DownloadTaskInfo result) {
            mMainThreadHandler.sendMessage(mMainThreadHandler
                    .obtainMessage(SUCCESS,
                            new Msg(mDownloadListener, result)));
            resfreshList(result);

        }

        @Override
        public void onError(DownloadTaskInfo result, int thr) {
            Msg msg = new Msg(mDownloadListener, result);
            msg.mError = thr;
            mMainThreadHandler.sendMessage(mMainThreadHandler.obtainMessage(ERROR, msg));
            resfreshList(result);

        }
    };

    /**
     * 传递信息类
     */
    private class Msg {
        /**
         * 下载监听
         */
        public DownloadListener mListener;
        /**
         * 任务信息
         */
        public DownloadTaskInfo mTask;
        /**
         * 任务进度
         */
        public int mProgress = 0;
        /**
         * 错误信息
         */
        public int mError = 0;

        /**
         * 构造方法
         *
         * @param listener 下载监听
         * @param taskInfo 任务信息
         */
        public Msg(DownloadListener listener, DownloadTaskInfo taskInfo) {
            this.mListener = listener;
            this.mTask = taskInfo;
        }
    }

    /**
     * 主线程Handler
     */
    private static class MainThreadHandler extends Handler {

        public MainThreadHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            Msg massage = (Msg) msg.obj;
            switch (msg.what) {
                case ADD:
                    massage.mListener.onAdd(massage.mTask);
                    break;
                case ADDED:
                    massage.mListener.onAdded(massage.mTask);
                    break;
                case ADDWAIT:
                    massage.mListener.onAddWait(massage.mTask);
                    break;
                case PAUSE:
                    massage.mListener.onPause(massage.mTask);
                    break;
                case START:
                    massage.mListener.onStart(massage.mTask);
                    break;
                case FIRSTSATRT:
                    massage.mListener.onFirstStart(massage.mTask);
                    break;
                case PROGRESSUPDATE:
                    massage.mListener.onProgressUpdate(massage.mProgress, massage.mTask);
                    break;
                case SUCCESS:
                    massage.mListener.onSuccess(massage.mTask);
                    break;
                case ERROR:
                    massage.mListener.onError(massage.mTask, massage.mError);
                    break;
            }
        }
    }


}
