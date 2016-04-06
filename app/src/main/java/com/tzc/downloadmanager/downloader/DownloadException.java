package com.tzc.downloadmanager.downloader;

/**
 * 错误类型常量类
 */
public class DownloadException {
    /**
     * context为空
     */
    public static final int CONTEXT_NOT_VALID = 1;
    /**
     * 任务失败
     */
    public static final int DOWNLOAD_TASK_FAILED = 2;
    /**
     * 任务不合法.
     */
    public static final int DOWNLOAD_TASK_NOT_VALID = 3;
    /**
     * 创建文件错误
     */
    public static final int CREATEFILE_ERROR = 4;
    /**
     * 创建文件夹失败
     */
    public static final int CREATEFLODER_ERROR = 5;
    /**
     * 异常导致失败
     */
    public static final int MALFORMEDURLEXCEPTION = 6;
    public static final int PROTOCOLEXCEPTION = 7;
    public static final int FILENOTFOUNDEXCEPTION = 8;
    public static final int IOEXCEPTION = 9;
    /**
     * 文件类型不正确
     */
    public static final int TYPE_ERROR = 10;
    /**
     * 包名不正确
     */
    public static final int PACKAGE_ERROR = 11;
    /**
     * 没有任务
     */
    public static final int NO_TASK = 12;
    /**
     * 网络异常
     */
    public static final int NETWORK_ERROR = 13;
    /**
     * 网络超时
     */
    public static final int TIMEOUT_ERROR = 14;

    /**
     * 包损坏
     */
    public static final int PACKAGE_CANNT_INSTALL = 15;

    /**
     * 启动任务部网络
     */
    public static final int START_WITH_NETWORKERR0R = 16;

    /**
     * 创建文件夹失败
     */
    public static final int MKDIRSEXCEPTION = 17;
    /**
     * 创建文件夹失败
     */
    public static final int SOCKET_EXCEPTION = 18;
}
