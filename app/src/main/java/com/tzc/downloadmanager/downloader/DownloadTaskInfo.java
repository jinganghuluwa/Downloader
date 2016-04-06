package com.tzc.downloadmanager.downloader;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.URLUtil;

import java.io.File;


/**
 * 下载任务信息
 *
 * @author tzc
 * @version v1.1
 * @date 2015/8/17
 */

public class DownloadTaskInfo {
    /**
     * 上下文
     */
    private Context mContext = null;
    /**
     * id,为数据库中自生成id
     */
    private int mId = 0;
    /**
     * 任务完成时间
     */
    private long mFinishTime = 0;
    /**
     * 下载监听
     */
    private DownloadListener mListener = null;
    /**
     * 文件名字
     */
    private String mFileName = "";
    /**
     * 存储路径
     */
    private String mPath = "";
    /**
     * 文件总大小
     */
    private long mFileSize = 0;
    /**
     * 开始下载的时间
     */
    private long mStartTime = 0;
    /**
     * 下载状态
     *
     * @see DownloadStatus
     */
    private int mStatus = DownloadStatus.STATUS_PENDING;
    /**
     * 下载任务
     */
    private DownloadTask mDownLoadTask = null;
    /**
     * url链接
     */
    private String mUrl = "";
    /**
     * 未完成状态是否需要重启,如包名校验失败,则不需要重启任务
     */
    private boolean mReStart = true;

    /**
     * 获取任务id
     *
     * @return 任务id
     */
    public int getId() {
        return mId;
    }

    /**
     * 设置任务id
     *
     * @param id 任务id
     */
    public void setId(int id) {
        this.mId = id;
    }

    /**
     * 设置是否需要重启
     *
     * @param reStart 是否要重启
     * @param isSave  是否同步到数据库
     */
    public void setReStart(boolean reStart, boolean isSave) {
        mReStart = reStart;
        if (mContext == null || !isSave) {
            return;
        }
        ISqlImpl sageISql = new ISqlImpl(mContext);
        sageISql.addDownloadTask(this);
    }

    /**
     * 获取是否需要重启
     *
     * @return 需要重启返回true, 不需要返回false
     */
    public boolean getReStart() {
        return mReStart;
    }

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    DownloadTaskInfo(Context context) {
        super();
        this.mContext = context;
    }

    /**
     * 构造方法
     *
     * @param context 上下文
     * @param url     下载链接
     */
    public DownloadTaskInfo(Context context, String url) {
        super();
        this.mContext = context;
        this.mUrl = url;
    }

    /**
     * 获取hashcoe,用url的hashcode值
     *
     * @return hashcode值
     */
    @Override
    public int hashCode() {
        return mUrl.hashCode();
    }

    /**
     * 重写toString方法
     *
     * @return 文件名加url加路径
     * 例如:fileName:abc.apk,url:http:www.asdf.com/abc.apk,path:sdcard/12321334234324324.apk
     */
    @Override
    public String toString() {
        return "fileName:" + mFileName + ",url:" + mUrl + ",path:" + mPath;
    }

    /**
     * 获取上下文
     *
     * @return 上下文
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * 设置上下文
     *
     * @param context 上下文
     */
    public void setContext(Context context) {
        this.mContext = context;
    }

    /**
     * 获取任务完成时间
     *
     * @return 任务完成时间, System.currentTimeMillis()
     */
    public long getFinishTime() {
        return mFinishTime;
    }

    /**
     * 设置任务完成时间
     *
     * @param finishTime 任务完成时间, System.currentTimeMillis()
     */
    void setFinishTime(long finishTime) {
        this.mFinishTime = finishTime;
    }


    /**
     * 设置下载监听
     *
     * @param listener
     */
    void setListener(DownloadListener listener) {
        this.mListener = listener;
    }


    /**
     * 获取文件名,真实文件名,并非本地保存文件名字
     *
     * @return 文件名
     */
    String getFileName() {
        return mFileName;
    }

    /**
     * 设置文件名
     *
     * @param fileName 文件名
     */
    void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    /**
     * 获取本地文件路径
     *
     * @return 本地文件路径
     */
    public String getPath() {
        return mPath;
    }

    /**
     * 设置本地文件路径
     *
     * @param path 本地文件路径
     */
    void setPath(String path) {
        this.mPath = path;
    }

    /**
     * 获取文件大小
     *
     * @return 文件大小
     */
    public long getSize() {
        return mFileSize;
    }

    /**
     * 设置文件大小
     *
     * @param fileSize 文件大小
     */
    void setSize(long fileSize) {
        this.mFileSize = fileSize;
    }

    /**
     * 获取任务开始时间
     *
     * @return 任务开始时间
     */
    long getStartTime() {
        return mStartTime;
    }

    /**
     * 设置任务开始时间
     *
     * @param startTime 任务开始时间
     */
    void setStartTime(long startTime) {
        this.mStartTime = startTime;
    }

    /**
     * 获取下载状态
     *
     * @return 下载状态
     */
    public int getStatus() {
        return mStatus;
    }

    /**
     * 设置下载状态
     *
     * @param status 下载状态
     * @param save   是否需要同步到数据库
     */
    void setStatus(int status, boolean save) {
        this.mStatus = status;
        if (mContext == null || !save) {
            return;
        }
        ISqlImpl mSageISql = new ISqlImpl(mContext);

        mSageISql.addDownloadTask(this);
    }

    /**
     * 获取url链接
     *
     * @return url链接
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * 设置url链接
     *
     * @param url url链接
     */
    public void setUrl(String url) {
        this.mUrl = url;
    }


    /**
     * url校验
     *
     * @return url合法返回true, 否则返回false
     */
    public boolean urlIsValid() {
        return URLUtil.isNetworkUrl(mUrl);
    }

    /**
     * 设置下载任务状态,会将传入的任务信息中的属性赋值给自身
     *
     * @param taskInfo 下载任务信息
     */
    protected void setDownloadTask(DownloadTaskInfo taskInfo) {
        this.mStartTime = taskInfo.mStartTime;
        if (TextUtils.isEmpty(this.mFileName)) {
            this.mFileName = taskInfo.mFileName;
        }
        if (TextUtils.isEmpty(this.mPath)) {
            this.mPath = taskInfo.mPath;
        }
        this.mId = taskInfo.mId;
        this.mFileSize = taskInfo.mFileSize;
        this.mStatus = taskInfo.mStatus;
        this.mUrl = taskInfo.mUrl;
    }

    /**
     * 开启任务
     *
     * @param context  上下文
     * @param listener 下载状态监听
     */
    protected void start(Context context, DownloadListener listener) {
        if (this.mContext == null && context != null) {
            this.mContext = context;
        }

        if (mDownLoadTask != null) {
            mDownLoadTask.cancel(false);
        }
        mDownLoadTask = new DownloadTask(listener);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            mDownLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
        } else {
            mDownLoadTask.execute(this);
        }

    }

    /**
     * 暂停任务
     */
    protected void pause() {

        if (mDownLoadTask != null) {
            mDownLoadTask.cancel(false);
        }
    }


    /**
     * 删除任务
     */
    protected void delete() {

        if (mDownLoadTask != null) {
            mDownLoadTask.cancel(false);
            File file = new File(getPath());
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            ISqlImpl sageISql = new ISqlImpl(mContext);
            sageISql.deleteDownloadTask(this);
        }

    }
}
