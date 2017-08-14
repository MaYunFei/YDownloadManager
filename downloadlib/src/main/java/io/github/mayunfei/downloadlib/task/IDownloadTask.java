package io.github.mayunfei.downloadlib.task;

/**
 * Created by mayunfei on 17-8-14.
 */

public interface IDownloadTask extends Runnable{
    void start();
    void pause();
    void cancel();
}
