package com.tzc.downloadmanager.downloader;

/**
 * 下载器配置类,包括过期时间,数据库名字等等...
 *
 * @author tzc
 */
public class DownLoaderConfig {
    /**
     * 清除历史下载任务时间间隔,default：14天
     */
    public static final int CLEAN_HISTORY_INTERVAL = 14 * 24 * 60 * 60 * 1000;

    /**
     * 清除过期 apk 时间
     */
    public static final int CLEAN_APK_OUTDATE = 7 * 24 * 60 * 60 * 1000;
    /**
     * 下载器数据库名字
     */
    public static final String DATABASE_NAME = "mobisagedownloaddb.db";
    /**
     * 通知 栏 是否可以点击
     *
     * @deprecated 没有用上
     */
    public static boolean IS_CLICK_ABLE = true;
    /**
     * 通知栏是否可以 取消
     */
    public static boolean NOTFACTION_CANCLEABLE = false;
    /**
     * 超时
     */
    public static int WIFI_TIME_OUT = 30 * 1000;
    /**
     * 下载版本
     *
     * @deprecated 没有用上
     */
    public static final String DOWNLOAD_VERSION = "2.0";
}
