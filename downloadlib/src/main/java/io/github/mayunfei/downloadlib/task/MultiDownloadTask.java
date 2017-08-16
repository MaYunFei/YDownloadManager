package io.github.mayunfei.downloadlib.task;

import android.util.Log;

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

    private static final String TAG = "MultiDownloadTask";
    private final ExecutorService executor;
    private MultiDownloadEntity multiDownloadEntity;

    private volatile boolean isPause;
    private volatile boolean isCancel;

    private AtomicLong completeSize = new AtomicLong(0);
    private AtomicLong failSize = new AtomicLong(0);
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
            }else {
                completeSize.incrementAndGet();//需要提高
            }
        }
    }

    private boolean checkNext() {
        //如果已经暂停或者取消 不在继续
        if (isCancel || isPause) {
            return true;
        }
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
        for (IDownloadTask task : taskHashMap.values()) {
            task.pause();
        }
    }

    @Override
    public void cancel() {
        isCancel = true;
        for (IDownloadTask task : taskHashMap.values()) {
            task.cancel();
        }
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
        if (taskHashMap.size() == 0) {
            multiDownloadEntity.status = DownloadEvent.PAUSE;
            downloadListener.onPause(multiDownloadEntity);
        }

    }

    @Override
    public void onCancel(BaseEntity entity) {
        //全部pause
        taskHashMap.remove(entity.getKey());
        if (taskHashMap.size() == 0) {
            multiDownloadEntity.status = DownloadEvent.CANCEL;
            downloadListener.onCancel(multiDownloadEntity);
        }
    }

    @Override
    public void onFinish(BaseEntity entity) {
        Log.i(TAG,"onFinish");
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
        if (failSize.longValue() + completeSize.longValue() == multiDownloadEntity.totalSize) { //过滤暂停取消情况
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