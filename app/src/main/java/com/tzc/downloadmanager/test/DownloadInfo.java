package com.tzc.downloadmanager.test;

/**
 * Created by tongzhichao on 16-4-6.
 */
public class DownloadInfo {

    private int fileSize;
    private int completeSize;
    private String urlString;

    public DownloadInfo(int fileSize, int completeSize, String urlString)
    {
        super();
        this.fileSize = fileSize;
        this.completeSize = completeSize;
        this.urlString = urlString;
    }

    public DownloadInfo()
    {
        super();
    }

    public int getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(int fileSize)
    {
        this.fileSize = fileSize;
    }

    public int getCompleteSize()
    {
        return completeSize;
    }

    public void setCompleteSize(int completeSize)
    {
        this.completeSize = completeSize;
    }

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
        return "DownloadInfo [fileSize=" + fileSize + ", completeSize="
                + completeSize + ", urlString=" + urlString + "]";
    }
}
