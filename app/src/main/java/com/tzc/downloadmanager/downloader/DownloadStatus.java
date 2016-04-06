package com.tzc.downloadmanager.downloader;

/**
 * 下载任务状态
 *
 * @author tzc
 * @version v1.1
 * @date 2015-8-17
 */
public class DownloadStatus {

    /**
     * 未开始
     */
    public static final int STATUS_PENDING = 0x00000001;

    /**
     * 运行中
     */
    public static final int STATUS_RUNNING = 0x00000002;

    /**
     * 停止
     */
    public static final int STATUS_PAUSE = 0x00000003;

    /**
     * 完成
     */
    public static final int STATUS_FINISHED = 0x00000005;

    /**
     * 任务失败 (不会重试).
     */
    public static final int STATUS_FAILED = 0x00000006;


}
