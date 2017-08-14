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
import io.github.mayunfei.downloadlib.task.DownloadEntity;
import io.github.mayunfei.downloadlib.task.DownloadTask;
import io.github.mayunfei.downloadlib.task.IDownloadTask;
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
            DownloadEntity downloadEntity = (DownloadEntity) intent.getSerializableExtra(Constants.DOWNLOAD_ENTITY);
            int action = intent.getIntExtra(Constants.ACTION, -1);
            doAction(action, downloadEntity);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownloadEntity entity) {
        switch (action) {
            case Constants.ACTION_ADD:
                entity.status = DownloadEvent.DOWNLOADING;
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

    private void cancel(DownloadEntity key) {
//        DownloadTask task = taskHashMap.remove(key);
//        if (task != null) {
//            task.cancel();
//        }
    }

    private void pause(DownloadEntity entity) {
//        DownloadTask task = taskHashMap.remove(key);
//        if (task != null) {
//            task.pause();
//        }
    }

    private void add(DownloadEntity downloadEntity) {
        IDownloadTask task = taskHashMap.get(downloadEntity.getKey());
        if (task == null) {
            task = new DownloadTask(downloadEntity);
                taskHashMap.put(downloadEntity.getKey(), task);
        }

        executor.submit(task);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
        super.onDestroy();
    }
}
