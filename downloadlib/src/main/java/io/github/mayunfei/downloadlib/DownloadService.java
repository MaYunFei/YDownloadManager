package io.github.mayunfei.downloadlib;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.mayunfei.downloadlib.dao.DownloadDao;
import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.observer.DataChanger;
import io.github.mayunfei.downloadlib.task.BaseDownloadEntity;
import io.github.mayunfei.downloadlib.task.SingleDownloadEntity;
import io.github.mayunfei.downloadlib.task.SingleDownloadTask;
import io.github.mayunfei.downloadlib.task.IDownloadTask;
import io.github.mayunfei.downloadlib.task.MultiDownloadEntity;
import io.github.mayunfei.downloadlib.task.MultiDownloadTask;
import io.github.mayunfei.downloadlib.utils.Constants;
import io.github.mayunfei.downloadlib.utils.Utils;

/**
 * Created by mayunfei on 17-7-31.
 */

public class DownloadService extends Service implements SingleDownloadTask.DownloadTaskListener {
    private static final String TAG = "DownloadService";

    private LinkedBlockingQueue<BaseDownloadEntity> waitQueue = new LinkedBlockingQueue<>();
    private Map<String, IDownloadTask> taskHashMap = new ConcurrentHashMap<String, IDownloadTask>(); //所有加载的任务

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if (intent != null) {
            BaseDownloadEntity baseEntity = (BaseDownloadEntity) intent.getSerializableExtra(Constants.DOWNLOAD_ENTITY);
            int action = intent.getIntExtra(Constants.ACTION, -1);
            doAction(action, baseEntity);
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void checkNext() {
        Log.i(TAG,"checkNext");
        BaseDownloadEntity nextEntity = waitQueue.poll();
        if (nextEntity != null) {
            startDownload(nextEntity);
        }
    }

    private void addDownloadEntity(BaseDownloadEntity baseEntity) {
        if (!DownloadDao.getInstance().exist(baseEntity.getKey())){
            DownloadDao.getInstance().install(baseEntity);
        }else {
            //TODO 存在记录
        }

        if (taskHashMap.size() >= Constants.MAX_DOWNLOADING) {
            waitQueue.offer(baseEntity);
            baseEntity.setStatus(DownloadEvent.WAIT);
            DataChanger.getInstance().postDownloadStatus(baseEntity);
        } else {
            startDownload(baseEntity);
        }

    }

    private void doAction(int action, BaseDownloadEntity entity) {
        switch (action) {
            case Constants.ACTION_ADD:
                addDownloadEntity(entity);
                break;
            case Constants.ACTION_PAUSE:
                if (!TextUtils.isEmpty(entity.getKey())) {
                    pause(entity);
                }
                break;
            case Constants.ACTION_CANCEL:
                if (!TextUtils.isEmpty(entity.getKey())) {
                    cancel(entity);
                }
                break;
            default:
                break;
        }
    }

    private void cancel(BaseDownloadEntity entity) {
//        DownloadTask task = taskHashMap.remove(key);
//        if (task != null) {
//            task.cancel();
//        }
        //取消清除
        IDownloadTask iDownloadTask = taskHashMap.remove(entity.getKey());

        if (iDownloadTask != null) {
            iDownloadTask.cancel();
        } else {
            // TODO 等待队列 直接删除
            //onCancel(entity);
        }
    }

    private void pause(BaseDownloadEntity entity) {
//        DownloadTask task = taskHashMap.remove(key);
//        if (task != null) {
//            task.pause();
//        }
        //暂停清除
        IDownloadTask iDownloadTask = taskHashMap.remove(entity.getKey());
        if (iDownloadTask != null) {
            iDownloadTask.pause();
        } else {
            waitQueue.remove(entity);
            entity.setStatus(DownloadEvent.PAUSE);
            onPause(entity);
        }
    }

    private void startDownload(BaseDownloadEntity baseEntity) {
        baseEntity.setStatus(DownloadEvent.DOWNLOADING);
        DataChanger.getInstance().postDownloadStatus(baseEntity);
        IDownloadTask task = taskHashMap.get(baseEntity.getKey());
        if (task == null) {
            if (baseEntity instanceof SingleDownloadEntity) {
                task = new SingleDownloadTask((SingleDownloadEntity) baseEntity, this);
                taskHashMap.put(baseEntity.getKey(), task);
            }
            if (baseEntity instanceof MultiDownloadEntity) {
                task = new MultiDownloadTask((MultiDownloadEntity) baseEntity, DownloadManager.getInstance().getExecutor(), this);
                taskHashMap.put(baseEntity.getKey(), task);
            }

        }
        DownloadManager.getInstance().getExecutor().submit(task);
    }

    @Override
    public void onDestroy() {
//        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onUpdate(BaseDownloadEntity entity) {
        if (entity.getCurrentSize() != entity.getTotalSize()) { //防止 finish update 同时
            DataChanger.getInstance().postDownloadStatus(entity);
        }
//        Log.i(TAG,"onUpdate " + entity.toString());
    }

    @Override
    public void onPause(BaseDownloadEntity entity) {
        DataChanger.getInstance().postDownloadStatus(entity);
        Log.i(TAG,"onPause " + entity.toString());
        checkNext();
    }

    @Override
    public void onCancel(BaseDownloadEntity entity) {
        DataChanger.getInstance().postDownloadStatus(entity);
        DownloadDao.getInstance().delete(entity.getKey());
        Utils.delete(entity.getPath());
        checkNext();
    }

    @Override
    public void onFinish(BaseDownloadEntity entity) {
//        Log.i(TAG,"onFinish" +
//                " " + entity.toString());
        taskHashMap.remove(entity.getKey());
        DataChanger.getInstance().postDownloadStatus(entity);
        checkNext();
    }

    @Override
    public void onError(BaseDownloadEntity entity) {
        DataChanger.getInstance().postDownloadStatus(entity);
        checkNext();
    }
}
