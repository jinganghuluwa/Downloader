package com.tzc.downloadmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.tzc.downloadmanager.downloader.DownloadListener;
import com.tzc.downloadmanager.downloader.DownloadManager;
import com.tzc.downloadmanager.downloader.DownloadTaskInfo;

public class MainActivity extends AppCompatActivity implements DownloadListener, View.OnClickListener {
    private DownloadManager manager;
    private Button start;
    private Button pause;
    private ProgressBar progress;
    DownloadTaskInfo downloadTaskInfo;
    private static final String URL = "http://sdlc-esd.oracle.com/ESD6/JSCDL/jdk/8u77-b03/jre-8u77-linux-x64.tar.gz?GroupName=JSC&FilePath=/ESD6/JSCDL/jdk/8u77-b03/jre-8u77-linux-x64.tar.gz&BHost=javadl.sun.com&File=jre-8u77-linux-x64.tar.gz&AuthParam=1459930559_05a0513af4286d748853eaa587ed0036&ext=.gz";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = (Button) findViewById(R.id.start);
        pause = (Button) findViewById(R.id.pause);
        progress = (ProgressBar) findViewById(R.id.progress);
        start.setOnClickListener(this);
        pause.setOnClickListener(this);
        manager = DownloadManager.getInstance(this, "sdcard/down/");
        manager.setListener(this);
        downloadTaskInfo = new DownloadTaskInfo(this, URL);
//        MediaMetadataRetriever media = new MediaMetadataRetriever();
//        media.setDataSource();
//        Bitmap bitmap = media.getFrameAtTime();
    }

    @Override
    public void onAdd(DownloadTaskInfo taskInfo) {
        Log.e("task", "onSuccess");
    }

    @Override
    public void onAdded(DownloadTaskInfo taskInfo) {
        Log.e("task", "onAdded");
    }

    @Override
    public void onAddWait(DownloadTaskInfo taskInfo) {
        Log.e("task", "onAddWait");
    }

    @Override
    public void onPause(DownloadTaskInfo taskInfo) {
        Log.e("task", "onPause");
    }

    @Override
    public void onStart(DownloadTaskInfo taskInfo) {
        Log.e("task", "onStart:" + System.currentTimeMillis());
    }

    @Override
    public void onFirstStart(DownloadTaskInfo taskInfo) {
        Log.e("task", "onFirstStart");
    }

    @Override
    public void onProgressUpdate(Integer progress, DownloadTaskInfo taskInfo) {
        this.progress.setProgress(progress);
    }

    @Override
    public void onSuccess(DownloadTaskInfo taskInfo) {
        Log.e("task", "onSuccess:" + System.currentTimeMillis());
    }

    @Override
    public void onError(DownloadTaskInfo taskInfo, int error) {
        Log.e("task", "onError:" + error);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                manager.start(downloadTaskInfo);
                break;
            case R.id.pause:
                manager.pause(downloadTaskInfo);
                break;
        }
    }


}
