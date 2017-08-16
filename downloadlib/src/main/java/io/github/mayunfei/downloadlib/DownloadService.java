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

import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.observer.DataChanger;
import io.github.mayunfei.downloadlib.task.BaseEntity;
import io.github.mayunfei.downloadlib.task.DownloadEntity;
import io.github.mayunfei.downloadlib.task.DownloadTask;
import io.github.mayunfei.downloadlib.task.IDownloadTask;
import io.github.mayunfei.downloadlib.task.MultiDownloadEntity;
import io.github.mayunfei.downloadlib.task.MultiDownloadTask;
import io.github.mayunfei.downloadlib.utils.Constants;

/**
 * Created by mayunfei on 17-7-31.
 */

public class DownloadService extends Service implements DownloadTask.DownloadTaskListener {
    private static final String TAG = "DownloadService";

    private ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        private AtomicInteger count = new AtomicInteger();

        @Override
        public Thread newThread(@NonNull final Runnable r) {
            final int c = count.incrementAndGet();
            Log.i(TAG, "new Thread ++++ " + c + "    ");
            return new Thread(new Runnable() {
                @Override
                public void run() {
//                    Log.i(TAG,"start +++ "+c);
                    r.run();
//                    Log.i(TAG,"end +++ "+c);
                }
            }, "download thread " + c);
        }
    });

    private LinkedBlockingQueue<BaseEntity> waitQueue = new LinkedBlockingQueue<>();
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
            BaseEntity baseEntity = (BaseEntity) intent.getSerializableExtra(Constants.DOWNLOAD_ENTITY);
            int action = intent.getIntExtra(Constants.ACTION, -1);
            doAction(action, baseEntity);
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void checkNext() {
        BaseEntity nextEntity = waitQueue.poll();
        if (nextEntity != null) {
            startDownload(nextEntity);
        }
    }

    private void addDownloadEntity(BaseEntity baseEntity) {
        if (taskHashMap.size() >= Constants.MAX_DOWNLOADING) {
            waitQueue.offer(baseEntity);
            baseEntity.setStatus(DownloadEvent.WAIT);
            DataChanger.getInstance().postDownloadStatus(baseEntity);
        } else {
            startDownload(baseEntity);
        }
    }

    private void doAction(int action, BaseEntity entity) {
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

    private void cancel(BaseEntity entity) {
//        DownloadTask task = taskHashMap.remove(key);
//        if (task != null) {
//            task.cancel();
//        }
        //取消清除
        IDownloadTask iDownloadTask = taskHashMap.remove(entity.getKey());

        if (iDownloadTask != null) {
            iDownloadTask.cancel();
        }else {
            //等待队列
        }
    }

    private void pause(BaseEntity entity) {
//        DownloadTask task = taskHashMap.remove(key);
//        if (task != null) {
//            task.pause();
//        }
        //暂停清除
        IDownloadTask iDownloadTask = taskHashMap.remove(entity.getKey());
        if (iDownloadTask != null) {
            iDownloadTask.pause();
        }else {
            waitQueue.remove(entity);
            entity.setStatus(DownloadEvent.PAUSE);
            onPause(entity);
        }
    }

    private void startDownload(BaseEntity baseEntity) {
        baseEntity.setStatus(DownloadEvent.DOWNLOADING);
        DataChanger.getInstance().postDownloadStatus(baseEntity);
        IDownloadTask task = taskHashMap.get(baseEntity.getKey());
        if (task == null) {
            if (baseEntity instanceof DownloadEntity) {
                task = new DownloadTask((DownloadEntity) baseEntity, this);
                taskHashMap.put(baseEntity.getKey(), task);
            }
            if (baseEntity instanceof MultiDownloadEntity) {
                task = new MultiDownloadTask((MultiDownloadEntity) baseEntity, executor, this);
                taskHashMap.put(baseEntity.getKey(), task);
            }

        }
        executor.submit(task);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onUpdate(BaseEntity entity) {
        DataChanger.getInstance().postDownloadStatus(entity);
    }

    @Override
    public void onPause(BaseEntity entity) {
        DataChanger.getInstance().postDownloadStatus(entity);
        checkNext();
    }

    @Override
    public void onCancel(BaseEntity entity) {
        DataChanger.getInstance().postDownloadStatus(entity);
        checkNext();
    }

    @Override
    public void onFinish(BaseEntity entity) {
        taskHashMap.remove(entity.getKey());
        DataChanger.getInstance().postDownloadStatus(entity);
        checkNext();
    }

    @Override
    public void onError(BaseEntity entity) {
        DataChanger.getInstance().postDownloadStatus(entity);
        checkNext();
    }
}
