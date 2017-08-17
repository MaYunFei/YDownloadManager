package io.github.mayunfei.downloadlib.task;

import io.github.mayunfei.downloadlib.event.DownloadEvent;

/**
 * Created by mayunfei on 17-7-26.
 */

public class SingleDownloadTask implements Runnable, IDownloadTask {
    private SingleDownloadEntity entity;
    private volatile boolean isPause;
    private volatile boolean isCancel;
    private DownloadTaskListener downloadListener;

    public SingleDownloadTask(SingleDownloadEntity entity, DownloadTaskListener onDownloadListener) {
        this.entity = entity;
        this.downloadListener = onDownloadListener;
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
                if(isPause){
                    downloadListener.onPause(entity);
                }else {
                    downloadListener.onCancel(entity);
                }
                return;
            }

            entity.currentSize = i += 1024;
            entity.speed = 1024; //更新速度
//            DataChanger.getInstance().postDownloadStatus(entity);
        }

        entity.status = DownloadEvent.FINISH;
        downloadListener.onFinish(entity);
//        DataChanger.getInstance().postDownloadStatus(entity);
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

    public interface DownloadTaskListener {
        void onUpdate(BaseDownloadEntity entity);

        void onPause(BaseDownloadEntity entity);

        void onCancel(BaseDownloadEntity entity);

        void onFinish(BaseDownloadEntity entity);

        void onError(BaseDownloadEntity entity);
    }
}
