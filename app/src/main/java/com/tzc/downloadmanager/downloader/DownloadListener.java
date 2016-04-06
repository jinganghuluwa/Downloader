/*
 * Copyright (C) 2013 Snowdream Mobile <yanghui1986527@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tzc.downloadmanager.downloader;


/**
 * 下载器监听
 *
 * @author tzc
 * @version v1.1
 * @date 2015-8-17
 */
public interface DownloadListener {
    /**
     * 数据库添加任务
     *
     * @param taskInfo 任务信息
     */
    void onAdd(DownloadTaskInfo taskInfo);


    /**
     * 已经存在的任务
     *
     * @param taskInfo 任务信息
     */
    void onAdded(DownloadTaskInfo taskInfo);

    /**
     * 添加到等待队列
     *
     * @param taskInfo 任务信息
     */
    void onAddWait(DownloadTaskInfo taskInfo);


    /**
     * 任务暂停
     *
     * @param taskInfo 任务信息
     */
    void onPause(DownloadTaskInfo taskInfo);

    /**
     * 开始运行,任务启动回调
     *
     * @param taskInfo 任务信息
     */
    void onStart(DownloadTaskInfo taskInfo);

    /**
     * 第一次运行,历史第一次运行回调
     *
     * @param taskInfo 任务信息
     */
    void onFirstStart(DownloadTaskInfo taskInfo);

    /**
     * 进度更新
     *
     * @param progress 进度值
     * @param taskInfo 任务信息
     */
    void onProgressUpdate(Integer progress, DownloadTaskInfo taskInfo);


    /**
     * 下载成功
     *
     * @param taskInfo 任务信息
     */
    void onSuccess(DownloadTaskInfo taskInfo);

    /**
     * 下载失败
     *
     * @param taskInfo 任务信息
     * @param error    错误类型,详见MSageDownloadException
     * @see DownloadException
     */
    void onError(DownloadTaskInfo taskInfo, int error);


}
