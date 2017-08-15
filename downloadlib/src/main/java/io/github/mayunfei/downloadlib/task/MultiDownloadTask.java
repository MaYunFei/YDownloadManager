package io.github.mayunfei.downloadlib.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mayunfei on 17-8-15.
 */

public class MultiDownloadTask implements IDownloadTask {

    private MultiDownloadEntity multiDownloadEntity;
    private List<DownloadTask> downloadTasks;

    public MultiDownloadTask(MultiDownloadEntity multiDownloadEntity) {
        this.multiDownloadEntity = multiDownloadEntity;
        this.downloadTasks = new ArrayList<>();
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void run() {
        start();
    }
}
