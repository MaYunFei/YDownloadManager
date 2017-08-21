package io.github.mayunfei.downloadlib.task;

/**
 * 单个文件多线程range
 * Created by mayunfei on 17-8-21.
 */

public class SingleMultPartDownloadTask implements IDownloadTask {
    SingleDownloadEntity entity;

    public SingleMultPartDownloadTask(SingleDownloadEntity entity) {
        this.entity = entity;
    }

    @Override
    public void start() {
        //分多个range
    }

    @Override
    public void pause() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void run() {

    }
}
