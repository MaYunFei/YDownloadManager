package io.github.mayunfei.downloadlib;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.github.mayunfei.downloadlib.dao.DownloadDao;
import io.github.mayunfei.downloadlib.observer.DataChanger;
import io.github.mayunfei.downloadlib.observer.DataWatcher;
import io.github.mayunfei.downloadlib.task.BaseDownloadEntity;
import io.github.mayunfei.downloadlib.task.MultiDownloadEntity;
import io.github.mayunfei.downloadlib.task.SingleDownloadEntity;
import io.github.mayunfei.downloadlib.utils.Constants;
import okhttp3.OkHttpClient;

/**
 * 现在control
 * Created by mayunfei on 17-7-31.
 */

public class DownloadManager {
    private static DownloadManager INSTANCE;
    private OkHttpClient okHttpClient;
    private Context context;
    private String path;

    private DownloadManager() {
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "YUNFEI";
    }

    public String getPath() {
        return path;
    }

    public synchronized static void init(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DownloadManager();
        }
        INSTANCE.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(Constants.TIME, TimeUnit.MILLISECONDS)
                .writeTimeout(Constants.TIME, TimeUnit.MILLISECONDS)
                .readTimeout(Constants.TIME, TimeUnit.MILLISECONDS)
                .build();
        INSTANCE.context = context.getApplicationContext();
        DownloadDao.init(context.getApplicationContext());
    }

    public synchronized static void init(Context context, OkHttpClient okHttpClient) {
        if (INSTANCE == null) {
            INSTANCE = new DownloadManager();
        }
        INSTANCE.okHttpClient = okHttpClient;
        INSTANCE.context = context.getApplicationContext();
        DownloadDao.init(context.getApplicationContext());
    }

    public static DownloadManager getInstance() {
        if (INSTANCE.okHttpClient == null) {
            throw new IllegalArgumentException("First call init");
        }
        return INSTANCE;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

//    public SimpleDownload simpleDownload(String url, String path, String fileName) {
//        return new SimpleDownload(new SingleDownloadEntity(url, url, fileName, path));
//    }


    public void add(BaseDownloadEntity downloadEntity) {
        if (TextUtils.isEmpty(downloadEntity.getPath())) {
            //TODO 是否要配置呢
            downloadEntity.setPath(path + File.separator + downloadEntity.getKey());
        }
        if (downloadEntity instanceof MultiDownloadEntity) {
            for (SingleDownloadEntity singleDownloadEntity : ((MultiDownloadEntity) downloadEntity).getDownloadEntities()) {
                if (!TextUtils.isEmpty(singleDownloadEntity.getPath())) { //第一次添加应该设置
                    break;
                }
                singleDownloadEntity.setPath(downloadEntity.getPath());
            }
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.DOWNLOAD_ENTITY, downloadEntity); //不能传太大数据，intent最多40k
        intent.putExtra(Constants.ACTION, Constants.ACTION_ADD);
        context.startService(intent);
    }

    public void pause(BaseDownloadEntity downloadEntity) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.DOWNLOAD_ENTITY, downloadEntity);
        intent.putExtra(Constants.ACTION, Constants.ACTION_PAUSE);
        context.startService(intent);
    }

    public void cancel(BaseDownloadEntity downloadEntity) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.DOWNLOAD_ENTITY, downloadEntity);
        intent.putExtra(Constants.ACTION, Constants.ACTION_CANCEL);
        context.startService(intent);
    }

    public void resume(BaseDownloadEntity downloadEntity) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.DOWNLOAD_ENTITY, downloadEntity);
        intent.putExtra(Constants.ACTION, Constants.ACTION_RESUME);
        context.startService(intent);
    }


//    public Flowable<DownloadEvent> getDownloadProcessor(String key) {
//        return DownloadProcessor.getInstance().getDownloadProcessor(key).onBackpressureLatest();
//    }

    public void addObserver(DataWatcher dataWatcher) {
        DataChanger.getInstance().addObserver(dataWatcher);
    }

    public void deleteObserver(DataWatcher dataWatcher) {
        DataChanger.getInstance().deleteObserver(dataWatcher);
    }

    public BaseDownloadEntity queryDownloadEntity(String key) {
        return DownloadDao.getInstance().query(key);
    }
}
