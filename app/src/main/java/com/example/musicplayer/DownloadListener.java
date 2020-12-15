package com.example.musicplayer;

/**
 * @author dhx
 * 用于下载的回调接口，对下载过程的各种状态进行监听
 */
public interface DownloadListener {

    /**
     * 显示下载进度
     * */
    void onProgress(int progress);

    /**
     * 下载成功
     * */
    void onSuccess();

    /**
     * 下载失败
     * */
    void onFailed();

}
