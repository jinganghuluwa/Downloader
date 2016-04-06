package com.tzc.downloadmanager.test;

/**
 * Created by tongzhichao on 16-4-6.
 */
public class ThreadDownloadInfo {

    private int threadId;// 开启的线程数
    private int startPos;// 该进程的起始位置
    private int endPos;// 该进程的终止位置
    private int completeSize;// 完成的进度
    private String urlString;// 当前任务的url

    public String getUrlString()
    {
        return urlString;
    }

    public void setUrlString(String urlString)
    {
        this.urlString = urlString;
    }

    @Override
    public String toString()
    {
        return "ThreadDownloadInfo [threadId=" + threadId + ", startPos="
                + startPos + ", endPos=" + endPos + ", completeSize="
                + completeSize + ", urlString=" + urlString + "]";
    }

    public ThreadDownloadInfo(int threadId, int startPos, int endPos,
                              int completeSize, String urlString)
    {
        this.threadId = threadId;
        this.startPos = startPos;
        this.endPos = endPos;
        this.completeSize = completeSize;
        this.urlString = urlString;
    }

    public ThreadDownloadInfo()
    {
    }

    public int getCompleteSize()
    {
        return completeSize;
    }

    public void setCompleteSize(int completeSize)
    {
        this.completeSize = completeSize;
    }

    public int getThreadId()
    {
        return threadId;
    }

    public void setThreadId(int threadId)
    {
        this.threadId = threadId;
    }

    public int getStartPos()
    {
        return startPos;
    }

    public void setStartPos(int startPos)
    {
        this.startPos = startPos;
    }

    public int getEndPos()
    {
        return endPos;
    }

    public void setEndPos(int endPos)
    {
        this.endPos = endPos;
    }
}
