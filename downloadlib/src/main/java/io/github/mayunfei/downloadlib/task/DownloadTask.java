package io.github.mayunfei.downloadlib.task;

import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.observer.DownloadProcessor;
import io.reactivex.processors.FlowableProcessor;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by mayunfei on 17-7-26.
 */

public class DownloadTask implements Runnable{
    private DownloadEntity downloadEntity;
    private volatile boolean isPause;
    private volatile boolean isCancel;
    private DownloadEvent downloadEvent;

    public DownloadTask(DownloadEntity downloadEntity) {
        this.downloadEntity = downloadEntity;
    }

    void start() {
        FlowableProcessor<DownloadEvent> processor = DownloadProcessor.getInstance().getDownloadProcessor(downloadEntity.getKey());

        downloadEntity.status = DownloadEvent.DOWNLOADING;
        downloadEntity.totalSize = 1024 * 5;
        for (int i = 0; i < downloadEntity.totalSize; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isPause || isCancel){
                downloadEntity.status = isPause?DownloadEvent.PAUSE:DownloadEvent.CANCEL;

                return;
            }

            downloadEntity.currentSize = i += 1024;
            processor.onNext(getDownloadEvent());
        }
        downloadEntity.status = DownloadEvent.FINISH;
        processor.onNext(getDownloadEvent());


    }

    private DownloadEvent getDownloadEvent() {
        if (downloadEvent == null) {
            downloadEvent = new DownloadEvent();
        }
        downloadEvent.totalSize = downloadEntity.totalSize;
        downloadEvent.currentSize = downloadEntity.currentSize;
        downloadEvent.status = downloadEntity.status;
        return downloadEvent;
    }


    void pause() {
        isPause = true;
    }

    void cancel() {
        isCancel = true;
    }

    @Override
    public void run() {
        start();
    }
}
