package io.github.mayunfei.downloadlib.task;

import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import io.github.mayunfei.downloadlib.dao.DownloadDao;
import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.utils.Constants;

/**
 * 多个文件打包
 * Created by mayunfei on 17-8-15.
 */

public class MultiDownloadTask implements IDownloadTask,SingleDownloadTask.DownloadTaskListener {

    private static final String TAG = "MultiDownloadTask";
    private final ExecutorService executor;
    private MultiDownloadEntity multiDownloadEntity;

    private volatile boolean isPause;
    private volatile boolean isCancel;
    private ArrayMap<String, Float> speedMap = new ArrayMap<>();

    private AtomicLong completeSize = new AtomicLong(0);
    private AtomicLong failSize = new AtomicLong(0);
    private LinkedBlockingQueue<SingleDownloadEntity> waitQueue = new LinkedBlockingQueue<>();
    private Map<String, IDownloadTask> taskHashMap = new ConcurrentHashMap<String, IDownloadTask>(); //所有加载的任务


    private SingleDownloadTask.DownloadTaskListener downloadListener;

    public MultiDownloadTask(MultiDownloadEntity multiDownloadEntity, ExecutorService executor, SingleDownloadTask.DownloadTaskListener listener) {
        this.multiDownloadEntity = multiDownloadEntity;
        this.downloadListener = listener;
        this.executor = executor;
//        this.downloadTasks = new ArrayList<>();
    }

    private void startDownload(SingleDownloadEntity entity) {
        SingleDownloadTask downloadTask = new SingleDownloadTask(entity, this);
        taskHashMap.put(entity.getKey(), downloadTask);
        executor.submit(downloadTask);
    }

    private void addDownloadEntity(SingleDownloadEntity baseEntity) {
        if (taskHashMap.size() >= Constants.MAX_PART_COUNT) {
            waitQueue.offer(baseEntity);
        } else {
            startDownload(baseEntity);
        }
    }

    @Override
    public void start() {
        multiDownloadEntity.setStatus(DownloadEvent.DOWNLOADING);
        for (int i = 0; i < multiDownloadEntity.downloadEntities.size(); i++) {
            SingleDownloadEntity entity = multiDownloadEntity.downloadEntities.get(i);
            if (entity.status != DownloadEvent.FINISH) {
                addDownloadEntity(entity);
            } else {
                completeSize.incrementAndGet();//需要提高
            }
        }
    }

    /**
     * true 不再检测是否完成
     * false 真的没了，需要检测是否完成
     *
     * @return
     */
    private boolean checkNext() {
        //如果已经暂停或者取消 不在继续
        if (isCancel || isPause) { //不能再继续
            return true;
        }
        SingleDownloadEntity nextEntity = waitQueue.poll();
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
        waitQueue.clear(); //清空等待队列
        for (IDownloadTask task : taskHashMap.values()) {
            task.pause();
        }
    }

    @Override
    public void cancel() {
        isCancel = true;
        waitQueue.clear();//清空等待队列
        for (IDownloadTask task : taskHashMap.values()) {
            task.cancel();
        }
    }

    @Override
    public void run() {
        start();
    }

    @Override
    public void onUpdate(BaseDownloadEntity entity) {
        //TODO 更新速度 更新时间问题
        if (entity instanceof SingleDownloadEntity){
            DownloadDao.getInstance().updateMultiPartTable((SingleDownloadEntity) entity);
        }
        speedMap.put(entity.getKey(), entity.speed);
    }

    @Override
    public void onPause(BaseDownloadEntity entity) {
        //全部pause
        if (entity instanceof SingleDownloadEntity){
            DownloadDao.getInstance().updateMultiPartTable((SingleDownloadEntity) entity);
        }
        Log.i(TAG, "onPause");
        taskHashMap.remove(entity.getKey());
        speedMap.remove(entity.getKey());
        if (taskHashMap.size() == 0 ) {
            multiDownloadEntity.status = DownloadEvent.PAUSE;
            downloadListener.onPause(multiDownloadEntity);
        }

    }

    @Override
    public void onCancel(BaseDownloadEntity entity) {
        Log.i(TAG, "onCancel");
        taskHashMap.remove(entity.getKey());
        speedMap.remove(entity.getKey());
        if (taskHashMap.size() == 0) {
            multiDownloadEntity.status = DownloadEvent.CANCEL;
            downloadListener.onCancel(multiDownloadEntity);
        }
    }

    @Override
    public void onFinish(BaseDownloadEntity entity) {
        Log.i(TAG, "onUpdate");
        //从队列里去掉
        if (entity instanceof SingleDownloadEntity){
            DownloadDao.getInstance().updateMultiPartTable((SingleDownloadEntity) entity);
        }
        taskHashMap.remove(entity.getKey());

        completeSize.incrementAndGet();
//        multiDownloadEntity.speed = entity.speed;
        multiDownloadEntity.currentSize = completeSize.longValue();
        if (speedMap.size() > 0) {
            Log.i(TAG, "speed count = " + speedMap.size());
            float speedTemp = 0f;
            for (Float speed : speedMap.values()
                    ) {
                speedTemp += speed;
            }
            multiDownloadEntity.speed = speedTemp;
        }
        downloadListener.onUpdate(multiDownloadEntity);

        speedMap.remove(entity.getKey()); //防止累计
        if (!checkNext()) {
            checkFinish();
        }
    }

    @Override
    public void onError(BaseDownloadEntity entity) {
        Log.i(TAG, "onError");
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
