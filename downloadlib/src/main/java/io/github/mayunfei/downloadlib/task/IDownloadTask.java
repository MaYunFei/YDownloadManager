package io.github.mayunfei.downloadlib.task;

public interface IDownloadTask extends IBaseDownloadTask, Runnable {
    void pause();
}
