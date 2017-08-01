package io.github.mayunfei.downloadlib.task;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.observer.DownloadProcessor;
import io.github.mayunfei.downloadlib.utils.Constants;
import io.reactivex.processors.FlowableProcessor;

/**
 * Created by mayunfei on 17-7-31.
 */

public class DownloadService extends Service {

    private Map<String, DownloadTask> taskHashMap;
    private ExecutorService executor;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        taskHashMap = new ConcurrentHashMap<String, DownloadTask>();
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DownloadEntity downloadEntity = (DownloadEntity) intent.getSerializableExtra(Constants.DOWNLOAD_ENTITY);
        int action = intent.getIntExtra(Constants.ACTION, -1);
        String key = intent.getStringExtra(Constants.KEY);
        donAction(action, downloadEntity, key);
        return super.onStartCommand(intent, flags, startId);
    }

    private void donAction(int action, DownloadEntity downloadEntity, String key) {
        switch (action) {
            case Constants.ACTION_ADD:
                add(downloadEntity);
                break;
            case Constants.ACTION_PAUSE:
                if (!TextUtils.isEmpty(key)) {
                    pause(key);
                }
                break;
            case Constants.ACTION_CANCEL:
                if (!TextUtils.isEmpty(key)) {
                    cancel(key);
                }
                break;
            default:
                break;
        }
    }

    private void cancel(String key) {
        DownloadTask task = taskHashMap.remove(key);
        if (task != null) {
            task.cancel();
        }
    }

    private void pause(String key) {
        DownloadTask task = taskHashMap.remove(key);
        if (task != null) {
            task.pause();
        }
    }

    private void add(DownloadEntity downloadEntity) {
        DownloadTask task = taskHashMap.get(downloadEntity.getKey());

        if (task == null) {
            task = new DownloadTask(downloadEntity);
            taskHashMap.put(downloadEntity.getKey(), task);
        }

        executor.submit(task);

    }
}
