package io.github.mayunfei.downloadlib.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.observer.DataChanger;
import io.github.mayunfei.downloadlib.observer.DataWatcher;
import io.github.mayunfei.downloadlib.utils.Constants;

/**
 * Created by mayunfei on 17-8-15.
 */

public class MultiDownloadTask implements IDownloadTask, DownloadTask.DownloadTaskListener {

    private final ExecutorService executor;
    private MultiDownloadEntity multiDownloadEntity;

    private volatile boolean isPause;
    private volatile boolean isCancel;

    private AtomicLong completeSize;
    private AtomicLong failSize;
    private LinkedBlockingQueue<DownloadEntity> waitQueue = new LinkedBlockingQueue<>();
    private Map<String, IDownloadTask> taskHashMap = new ConcurrentHashMap<String, IDownloadTask>(); //所有加载的任务


    private DownloadTask.DownloadTaskListener downloadListener;

    public MultiDownloadTask(MultiDownloadEntity multiDownloadEntity, ExecutorService executor, DownloadTask.DownloadTaskListener listener) {
        this.multiDownloadEntity = multiDownloadEntity;
        this.downloadListener = listener;
        this.executor = executor;
//        this.downloadTasks = new ArrayList<>();
    }

    private void startDownload(DownloadEntity entity) {
        DownloadTask downloadTask = new DownloadTask(entity, this);
        taskHashMap.put(entity.getKey(), downloadTask);
        executor.submit(downloadTask);
    }

    private void addDownloadEntity(DownloadEntity baseEntity) {
        if (taskHashMap.size() >= Constants.MAX_DOWNLOADING) {
            waitQueue.offer(baseEntity);
        } else {
            startDownload(baseEntity);
        }
    }

    @Override
    public void start() {

        multiDownloadEntity.setStatus(DownloadEvent.DOWNLOADING);
        for (int i = 0; i < multiDownloadEntity.downloadEntities.size(); i++) {
            DownloadEntity entity = multiDownloadEntity.downloadEntities.get(i);
            if (entity.status != DownloadEvent.FINISH) {
                addDownloadEntity(entity);
            }
        }
    }

    private boolean checkNext() {
        DownloadEntity nextEntity = waitQueue.poll();
        if (nextEntity != null) {
            startDownload(nextEntity);
            return true;
        }
        return false;
    }

//    private boolean checkPauseOrCancel() {
//        if (isPause || isCancel) {
//            multiDownloadEntity.status = isPause ? DownloadEvent.PAUSE : DownloadEvent.CANCEL;
//            DataChanger.getInstance().postDownloadStatus(multiDownloadEntity);
//            //TODO Cancel  需要删除
//            return true;
//        }
//        return false;
//    }

    @Override
    public void pause() {
        isPause = true;
        //TODO
    }

    @Override
    public void cancel() {
        isCancel = true;
        //TODO
    }

    @Override
    public void run() {
        start();
    }

    @Override
    public void onUpdate(BaseEntity entity) {

    }

    @Override
    public void onPause(BaseEntity entity) {
        //全部pause
        taskHashMap.remove(entity.getKey());
    }

    @Override
    public void onCancel(BaseEntity entity) {
        //全部pause
        taskHashMap.remove(entity.getKey());
    }

    @Override
    public void onFinish(BaseEntity entity) {
        taskHashMap.remove(entity.getKey());
        completeSize.incrementAndGet();
        multiDownloadEntity.currentSize = completeSize.longValue();
        downloadListener.onUpdate(multiDownloadEntity);
        if (!checkNext()) {
            checkFinish();
        }
    }

    @Override
    public void onError(BaseEntity entity) {
        taskHashMap.remove(entity.getKey());
        failSize.incrementAndGet();
        if (!checkNext()) {
            checkFinish();
        }
    }


    private void checkFinish() {
        if (failSize.longValue() + completeSize.longValue() == multiDownloadEntity.totalSize) {
            if (completeSize.longValue() == multiDownloadEntity.totalSize) {
                //finish
                multiDownloadEntity.status = DownloadEvent.FINISH;
                downloadListener.onFinish(multiDownloadEntity);
            } else {
                multiDownloadEntity.status = DownloadEvent.ERROR;
                downloadListener.onError(multiDownloadEntity);
            }
        }
    }
}
