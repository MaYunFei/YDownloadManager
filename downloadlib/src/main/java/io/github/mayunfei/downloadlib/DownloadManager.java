package io.github.mayunfei.downloadlib;

import android.content.Context;
import android.content.Intent;

import java.util.concurrent.TimeUnit;

import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.observer.DownloadProcessor;
import io.github.mayunfei.downloadlib.task.DownloadEntity;
import io.github.mayunfei.downloadlib.task.DownloadService;
import io.github.mayunfei.downloadlib.utils.Constants;
import io.reactivex.Flowable;
import okhttp3.OkHttpClient;

/**
 * 现在control
 * Created by mayunfei on 17-7-31.
 */

public class DownloadManager {
    private static DownloadManager INSTANCE;
    private OkHttpClient okHttpClient;

    private DownloadManager() {
    }

    public synchronized static void init() {
        if (INSTANCE == null) {
            INSTANCE = new DownloadManager();
        }
        INSTANCE.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(Constants.TIME, TimeUnit.MILLISECONDS)
                .writeTimeout(Constants.TIME, TimeUnit.MILLISECONDS)
                .readTimeout(Constants.TIME, TimeUnit.MILLISECONDS)
                .build();
    }

    public synchronized static void init(OkHttpClient okHttpClient) {
        if (INSTANCE == null) {
            INSTANCE = new DownloadManager();
        }
        INSTANCE.okHttpClient = okHttpClient;
    }

    public static DownloadManager getInstance() {
        if (INSTANCE.okHttpClient == null) {
            throw new IllegalArgumentException("First call init");
        }
        return INSTANCE;
    }

    OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public SimpleDownload simpleDownload(String url, String path, String fileName) {
        return new SimpleDownload(new DownloadEntity(url, url, fileName, path));
    }


    public void add(Context context, DownloadEntity downloadEntity) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.DOWNLOAD_ENTITY, downloadEntity);
        intent.putExtra(Constants.ACTION, Constants.ACTION_ADD);
        context.startService(intent);
    }




    public Flowable<DownloadEvent> getDownloadProcessor(String key) {
        return DownloadProcessor.getInstance().getDownloadProcessor(key).onBackpressureLatest();
    }
}
