package io.github.mayunfei.downloadlib.task;

import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.observer.DataChanger;
import io.github.mayunfei.downloadlib.observer.DownloadProcessor;
import io.reactivex.processors.FlowableProcessor;

/**
 * Created by mayunfei on 17-7-26.
 */

public class DownloadTask implements Runnable, IDownloadTask {
    private DownloadEntity entity;
    private volatile boolean isPause;
    private volatile boolean isCancel;

    public DownloadTask(DownloadEntity entity) {
        this.entity = entity;
    }

    @Override
    public void start() {

        entity.status = DownloadEvent.DOWNLOADING;
        entity.totalSize = 1024 * 5;
        for (int i = 0; i < entity.totalSize; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isPause || isCancel) {
                entity.status = isPause ? DownloadEvent.PAUSE : DownloadEvent.CANCEL;
                return;
            }

            entity.currentSize = i += 1024;
            DataChanger.getInstance().postDownloadStatus(entity);
        }

        entity.status = DownloadEvent.FINISH;
        DataChanger.getInstance().postDownloadStatus(entity);
    }


    @Override
    public void pause() {
        isPause = true;
    }

    @Override
    public void cancel() {
        isCancel = true;
    }

    @Override
    public void run() {
        start();
    }
}
