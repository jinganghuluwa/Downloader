package com.tzc.downloadmanager.downloader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * task任务类
 *
 * @author tzc
 * @version v1.1
 * @date 2015-8-17
 */
public class DownloadTask extends AsyncTask<DownloadTaskInfo, Integer, DownloadTaskInfo> {

    /**
     * 下载监听
     */
    protected DownloadListener mListener = null;

    /**
     * 下载路径
     */
    private static final String ROOT_PATH = DownloadManager.getRootPath();
    /**
     * 真正的url,如果有跳转为跳转之后的
     */
    private String mTrueUrl = "";
    /**
     * 任务信息
     */
    private DownloadTaskInfo mTaskInfo;

    /**
     * 构造方法
     *
     * @param listener 下载监听
     */
    public DownloadTask(DownloadListener listener) {
        super();
        this.mListener = listener;
    }


    /**
     * 回调错误通知
     *
     * @param taskInfo  任务信息
     * @param errorCode 错误码,详见MSageDownloadException
     * @see DownloadException
     */
    private void sendError(DownloadTaskInfo taskInfo, int errorCode) {
        taskInfo.setStatus(DownloadStatus.STATUS_FAILED, true);
        mListener.onError(mTaskInfo, errorCode);
    }


    /**
     * AsyncTask 后台运行
     */
    protected DownloadTaskInfo doInBackground(DownloadTaskInfo... taskIfs) {
        if (taskIfs.length <= 0) {
            return null;
        }

        final DownloadTaskInfo taskInfo = taskIfs[0];
        //任务合法性校验
        if (taskInfo == null || !taskInfo.urlIsValid()) {
            sendError(taskInfo, DownloadException.DOWNLOAD_TASK_NOT_VALID);
            return null;
        }
        // 通知任务开始
        if (mListener != null) {
            taskInfo.setStatus(DownloadStatus.STATUS_PENDING, true);
            ISqlImpl mSageISql = new ISqlImpl(taskInfo.getContext());
            DownloadTaskInfo temptask;
            temptask = mSageISql.queryDownloadTask(taskInfo.getUrl());
            if (temptask != null) {
                taskInfo.setId(temptask.getId());
            }
            mListener.onStart(taskInfo);
        }
        mTaskInfo = taskInfo;
        String path = taskInfo.getPath();
        File file = new File(path);
        //modify by dyp 20151009 https不支持断点，如有缓存文件直接删除
        if (taskInfo.getUrl().startsWith("https://")) {
            file.delete();
        }
        InputStream in = null;
        RandomAccessFile out = null;
        HttpURLConnection connection = null;
        try {
            long range = file.length();
            long size = taskInfo.getSize();
            long curSize = range;
            if (range != 0 && size == range) {
                if (taskInfo.getStatus() != DownloadStatus.STATUS_FINISHED) {
                    taskInfo.setStatus(DownloadStatus.STATUS_FINISHED, true);
                }
                return taskInfo;
            } else if (size != 0 && range > size) {
                //文件大小大于任务中文件大小,这种情况应该不会发生,如果发生就说明文件写入有问题,将文件删掉.
                if (file.exists()) {
                    file.delete();
                }
                range = 0;
                curSize = range;
            }
            String urlString = taskInfo.getUrl();
            String cookies = null;
            //网络请求
            connection = getConnect(urlString, cookies, range, taskInfo);
            if (connection == null) {
                taskInfo.setReStart(true, false);
                taskInfo.setStatus(DownloadStatus.STATUS_FAILED, true);
                return null;
            }
            //文件当前大小=0,第一次下载,非断点,获取文件总大小
            if (range == 0) {
                size = connection.getContentLength();
                taskInfo.setSize(size);

                // 自动获取文件名
                if (TextUtils.isEmpty(path)) {
                    String name;
                    String disposition = connection.getHeaderField("Content-Disposition");
                    if (!TextUtils.isEmpty(disposition)) {
                        disposition = disposition.replace("\"", "");
                        final String FILENAME = "filename=";
                        final int startIdx = disposition.indexOf(FILENAME);
                        final int endIdx = disposition.indexOf(';', startIdx);
                        name = disposition.substring(startIdx + FILENAME.length(), endIdx > 0 ? endIdx : disposition.length());
                    } else {
                        if (TextUtils.isEmpty(mTrueUrl)) {
                            name = urlString.substring(urlString.lastIndexOf("/") + 1,
                                    urlString.length());
                        } else {
                            name = mTrueUrl.substring(mTrueUrl.lastIndexOf("/") + 1,
                                    mTrueUrl.length());
                        }

                    }
                    taskInfo.setFileName(name);
                    try {
                        path = ROOT_PATH + MD5Utils.getMD5(urlString);
                        String type = taskInfo.getFileName();
                        if (type.contains(".")) {
                            String[] ss = type.split("\\.");
                            type = ss[ss.length - 1];
                            path = path + "." + type;
                        }
                        file = new File(path);
                        // 创建路径文件夹们
                        File dir = file.getParentFile();
                        if (!dir.exists() && !dir.mkdirs()) {
                            sendError(taskInfo, DownloadException.MKDIRSEXCEPTION);
                            taskInfo.setReStart(true, false);
                            taskInfo.setStatus(DownloadStatus.STATUS_FAILED, true);
                            return null;
                        }
                        //modify by ydp 201510 https的历史文件删除
                        if (urlString.startsWith("https://")) {
                            file.delete();
                        }
                        boolean sucess = file.createNewFile();
                        taskInfo.setPath(path);
                        if (!sucess && file.isFile() && file.exists()) {
                            //数据库中没查到,但是文件存在,重新请求网络
                            range = file.length();
                            curSize = range;
                            if (connection != null) {
                                connection.disconnect();
                            }
                            connection = getConnect(urlString, cookies, range, taskInfo);
                            if (connection == null) {
                                taskInfo.setReStart(true, false);
                                taskInfo.setStatus(DownloadStatus.STATUS_FAILED, true);
                                return null;
                            } else {
                                if (curSize == connection.getContentLength()) {
                                    taskInfo.setStatus(DownloadStatus.STATUS_FINISHED, true);
                                    return taskInfo;
                                } else if (curSize > connection.getContentLength()) {
                                    file.delete();
                                    curSize = range = 0;
                                    if (connection != null) {
                                        connection.disconnect();
                                    }
                                    connection = getConnect(urlString, cookies, range, taskInfo);
                                    if (connection == null) {
                                        taskInfo.setReStart(true, false);
                                        taskInfo.setStatus(DownloadStatus.STATUS_FAILED, true);
                                        return null;
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        taskInfo.setReStart(true, false);
                        taskInfo.setStatus(DownloadStatus.STATUS_FAILED, true);
                        sendError(taskInfo, DownloadException.CREATEFILE_ERROR);
                        return null;
                    } finally {
                        taskInfo.setStatus(taskInfo.getStatus(), true);
                    }

                }
            }
            //设置任务开始时间
            taskInfo.setStartTime(System.currentTimeMillis());
            //创建路径文件夹们
            File dir = file.getParentFile();
            if (!dir.exists() && !dir.mkdirs()) {
                sendError(taskInfo, DownloadException.MKDIRSEXCEPTION);
                taskInfo.setReStart(true, false);
                taskInfo.setStatus(DownloadStatus.STATUS_FAILED, true);
                return null;
            }
            taskInfo.setStatus(DownloadStatus.STATUS_RUNNING, true);
            mTaskInfo = taskInfo;
            if (range == 0) {
                if (mListener != null) {
                    mListener.onFirstStart(taskInfo);
                }
            }
            //输出,在之前的位置之后写,会创建file文件
            out = new RandomAccessFile(file, "rw");
            out.seek(range);
            in = new BufferedInputStream(connection.getInputStream());
            byte[] buffer = new byte[1024 * 2];
            int nRead;
            int progress = -1;
            boolean isFinishDownloading = true;
            int pro = 0;
            //开始写文件之前更新一下进度条
            if (size != 0) {
                publishProgress((int) ((curSize * 100) / size));
            }
            while ((nRead = in.read(buffer, 0, 1024)) > 0) {
                out.write(buffer, 0, nRead);
                curSize += nRead;
                if (size != 0) {
                    progress = (int) ((curSize * 100) / size);
                }
                // 通知进度
                if (progress - pro >= 5 || progress == 100) {
                    publishProgress(progress);
                    pro = progress;
                }
                //取消任务,不会删除数据库和临时文件,再次开启任务会继续下载
                if (isCancelled()) {
                    taskInfo.setStatus(DownloadStatus.STATUS_PAUSE, true);
                    isFinishDownloading = false;
                    if (mListener != null) {
                        if (taskInfo != null) {
                            mListener.onPause(taskInfo);
                        }
                    }
                    break;
                }
                if (taskInfo.getStatus() != DownloadStatus.STATUS_RUNNING) {
                    isFinishDownloading = false;
                    break;
                }
            }

            if (!isFinishDownloading) {
                taskInfo.setReStart(true, false);
                taskInfo.setStatus(DownloadStatus.STATUS_FAILED, true);
                return null;
            }
            if (size == 0 && curSize != 0) {
                taskInfo.setSize(curSize);
            }
            range = file.length();
            size = taskInfo.getSize();
            if (range != 0 && 0 == size) {
                taskInfo.setSize(range);
            } else if ((range == 0) || (range != 0 && size != 0 && range != size)) {
                sendError(taskInfo, DownloadException.DOWNLOAD_TASK_FAILED);
                taskInfo.setReStart(true, false);
                taskInfo.setStatus(DownloadStatus.STATUS_FAILED, true);
                return null;
            }
            taskInfo.setFinishTime(System.currentTimeMillis());
            taskInfo.setStatus(DownloadStatus.STATUS_FINISHED, true);
            return taskInfo;
        } catch (MalformedURLException e) {
            sendError(taskInfo, DownloadException.MALFORMEDURLEXCEPTION);
            e.printStackTrace();
        } catch (ProtocolException e) {
            sendError(taskInfo, DownloadException.PROTOCOLEXCEPTION);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            sendError(taskInfo, DownloadException.FILENOTFOUNDEXCEPTION);
            e.printStackTrace();
        } catch (SocketException e) {
            sendError(taskInfo, DownloadException.SOCKET_EXCEPTION);
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            sendError(taskInfo, DownloadException.TIMEOUT_ERROR);
            e.printStackTrace();
        } catch (IOException e) {
            sendError(taskInfo, DownloadException.IOEXCEPTION);
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
        taskInfo.setReStart(true, false);
        taskInfo.setStatus(DownloadStatus.STATUS_FAILED, true);
        return null;
    }


    /**
     * 请求网络
     *
     * @param urlString url地址
     * @param cookies   cookoies
     * @param range     文件当前大小
     * @param task      下载任务
     * @return HttpURLConnection信息
     * @throws IOException 网络请求io异常
     */
    private HttpURLConnection getConnect(String urlString, String cookies, long range, DownloadTaskInfo task) throws IOException {
        if (((ConnectivityManager) task.getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null && ((ConnectivityManager) task.getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().isConnected()) {
            HttpURLConnection connection;
            while (true) {
                URL url = new URL(urlString);
                //关键代码
                //ignore https certificate validation |忽略 https 证书验证
                if (url.getProtocol().toUpperCase().equals("HTTPS")) {
                    FakeX509TrustManager.allowAllSSL();
                    HttpsURLConnection https = (HttpsURLConnection) url
                            .openConnection();
                    connection = https;
                } else {
                    connection = (HttpURLConnection) url.openConnection();
                }
                connection.setConnectTimeout(DownLoaderConfig.WIFI_TIME_OUT);
                connection.setReadTimeout(DownLoaderConfig.WIFI_TIME_OUT);
                connection.setRequestProperty("User-Agent", "MobiSageSDK");
                connection.setRequestProperty("Connection", "Keep-Alive");
                if (!TextUtils.isEmpty(cookies)) {
                    connection.setRequestProperty("Cookie", cookies);
                }
                connection.setRequestMethod("GET");
                //截取返回数据流,断点续传
                //modify by ydp 20151009 https不支持断点，
                // 此处判断为保证数据库未保存本地文件存储位置，但通过URL得到了文件
                if (range > 0 && !urlString.startsWith("https://")) {
                    connection.setRequestProperty("Range", "bytes=" + range +
                            "-");
                }
                boolean redirect = false;
                boolean success = false;
                int status = connection.getResponseCode();
                //modify ydp 20151027 当断点传入值不在此URL接受范围内，服务器返回416，此时将断点值传0再次请求
                if (status == 416) {
                    connection = getConnect(urlString, cookies, 0, task);
                    status = connection.getResponseCode();
                }
                switch (status) {
                    case HttpURLConnection.HTTP_OK:
                    case HttpURLConnection.HTTP_PARTIAL:
                        success = true;
                        mTrueUrl = connection.getURL().getPath();
                        String transfer_encoding = connection.getHeaderField("Transfer-Encoding");
                        String accept_ranges = connection.getHeaderField("Accept-Ranges");
                        if (!TextUtils.isEmpty(accept_ranges)
                                && accept_ranges.equalsIgnoreCase("bytes")) {
                        } else {
                            range = 0;
                        }
                        break;
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_SEE_OTHER:
                        redirect = true;
                        urlString = connection.getHeaderField("Location");
                        mTrueUrl = urlString;
                        cookies = connection.getHeaderField("Set-Cookie");
                        break;
                    default:
                        success = false;
                        break;
                }
                //是否转向,转向继续循环,非转向判断成功与否
                if (!redirect) {
                    if (!success) {
                        sendError(task, DownloadException.NETWORK_ERROR);
                        return null;
                    }
                    break;
                }

            }
            return connection;
        } else {
            sendError(task, DownloadException.START_WITH_NETWORKERR0R);
            return null;
        }

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onCancelled(DownloadTaskInfo result) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            super.onCancelled(result);
        } else {
            super.onCancelled();
        }

    }

    @Override
    protected void onPostExecute(DownloadTaskInfo result) {
        super.onPostExecute(result);
        if (mListener != null) {
            if (result != null) {
                mListener.onSuccess(result);
            }

        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (mListener != null) {
            mListener.onProgressUpdate(values[0], mTaskInfo);
        }
    }

}
