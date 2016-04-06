package com.tzc.downloadmanager.test;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tongzhichao on 16-4-6.
 */
public class DownLoader {
    private String urlString;// 下载地址
    private String localFile;// 保存的文件
    private int threadCount;// 开启的线程数
    private int fileSize;// 文件大小
    private Handler mHandler;// 同步进度条
    private DownloadDao dao;// 数据库操作类
    private List<ThreadDownloadInfo> infos;// 保存下载信息

    // 标记下载状态
    public static final int INIT = 1; // 初始状态
    public static final int DOWNLOADING = 2;// 正在下载
    public static final int PAUSE = 3;// 暂停
    private int state = INIT;

    public DownLoader(String urlString, String localFile, int threadCount,
                      Context context, Handler handler) {
        this.urlString = urlString;
        this.localFile = localFile;
        this.threadCount = threadCount;
        mHandler = handler;
        dao = new DownloadDao(context);
    }

    /**
     * 下载
     */
    public void download() {
        Log.e("down", "start:" + System.currentTimeMillis());
        if (infos != null) {
            Log.v("TAG", "download()------->infos != null");
            if (state == DOWNLOADING) {
                return;
            }
            state = DOWNLOADING;
            for (ThreadDownloadInfo info : infos) {
                new DownloadThread(info.getThreadId(), info.getStartPos(),
                        info.getEndPos(), info.getCompleteSize(), info.getUrlString()).start();
            }
        }
    }

    /**
     * 下载器是否正在下载 true： 正在下载
     */
    public boolean isDownloading() {
        return state == DOWNLOADING;
    }

    /**
     * 得到当前下载信息
     *
     * @return
     */
    public DownloadInfo getDownloadInfo() {
        if (isFirst(urlString)) {
            init();
            infos = new ArrayList<ThreadDownloadInfo>();
            int range = fileSize / threadCount;
            for (int i = 0; i < threadCount - 1; i++) {
                ThreadDownloadInfo info = new ThreadDownloadInfo(i, i * range,
                        (i + 1) * range - 1, 0, urlString);
                infos.add(info);
            }
            ThreadDownloadInfo info = new ThreadDownloadInfo(threadCount - 1,
                    (threadCount - 1) * range, fileSize - 1, 0, urlString);
            infos.add(info);
            dao.saveInfos(infos);
            return new DownloadInfo(fileSize, 0, urlString);
        } else {
            infos = dao.getInfos(urlString);
            int size = 0;
            int completeSize = 0;
            for (ThreadDownloadInfo info : infos) {
                completeSize += info.getCompleteSize();
                size += info.getEndPos() - info.getStartPos() + 1;
            }
            return new DownloadInfo(size, completeSize, urlString);
        }
    }

    /**
     * 初始化 连接网络，准备文件的保存路径等
     */
    private void init() {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            fileSize = conn.getContentLength();
            File file = new File(localFile);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            RandomAccessFile rFile = new RandomAccessFile(localFile, "rwd");
            rFile.setLength(fileSize);

            rFile.close();
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是不是断点续传（即是不是第一次下载） true：第一次下载
     *
     * @param urlString
     * @return
     */
    private boolean isFirst(String urlString) {
        return dao.unhasInfo(urlString);
    }

    /**
     * 暂停
     */
    public void pause() {
        state = PAUSE;
    }

    /**
     * 下载的线程类
     *
     * @author song
     */
    private class DownloadThread extends Thread {
        private int threadId;
        private int startPos;
        private int endPos;
        private int completeSize;
        private String urlString;

        public DownloadThread(int threadId, int startPos, int endPos,
                              int completeSize, String urlString) {
            this.threadId = threadId;
            this.startPos = startPos;
            this.endPos = endPos;
            this.completeSize = completeSize;
            this.urlString = urlString;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile rFile = null;
            InputStream is = null;

            try {
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(4000);
                conn.setRequestProperty("Range", "bytes="
                        + (startPos + completeSize) + "-" + endPos);

                rFile = new RandomAccessFile(localFile, "rwd");
                rFile.seek(startPos + completeSize);

                is = conn.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    rFile.write(buffer, 0, len);
                    completeSize += len;
                    dao.updateInfo(threadId, completeSize, urlString);
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.arg2 = len;
                    msg.obj = urlString;
                    mHandler.sendMessage(msg);
                    Log.v("TAG", "completeSize=" + completeSize);
                    if (state == PAUSE) {
                        return;
                    }
                }
                Log.e("down", "finish:" + System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                    rFile.close();
                    conn.disconnect();
                    dao.closeDB();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void deleteInfo(String urlString) {
        dao.deleteInfos(urlString);
    }

    public void closeDB() {
        dao.closeDB();
    }
}
