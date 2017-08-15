package io.github.mayunfei.downloadlib;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";

    private Map<String, IDownloadTask> taskHashMap; //所有加载的任务

    private ExecutorService executor;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        taskHashMap = new ConcurrentHashMap<String, IDownloadTask>();
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand");
        if (intent!=null){
            BaseEntity baseEntity = (BaseEntity) intent.getSerializableExtra(Constants.DOWNLOAD_ENTITY);
            int action = intent.getIntExtra(Constants.ACTION, -1);
            doAction(action, baseEntity);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, BaseEntity entity) {
        switch (action) {
            case Constants.ACTION_ADD:
                entity.setStatus(DownloadEvent.DOWNLOADING);
                DataChanger.getInstance().postDownloadStatus(entity);
                add(entity);
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

    private void cancel(BaseEntity key) {
//        DownloadTask task = taskHashMap.remove(key);
//        if (task != null) {
//            task.cancel();
//        }
    }

    private void pause(BaseEntity entity) {
//        DownloadTask task = taskHashMap.remove(key);
//        if (task != null) {
//            task.pause();
//        }
    }

    private void add(BaseEntity baseEntity) {
        IDownloadTask task = taskHashMap.get(baseEntity.getKey());
        if (task == null) {
            if (baseEntity instanceof  DownloadEntity){
                task = new DownloadTask((DownloadEntity) baseEntity);
                taskHashMap.put(baseEntity.getKey(), task);
            }
            if (baseEntity instanceof MultiDownloadEntity){
                task = new MultiDownloadTask((MultiDownloadEntity) baseEntity);
                taskHashMap.put(baseEntity.getKey(),task);
            }

        }

        executor.submit(task);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
        super.onDestroy();
    }
}
