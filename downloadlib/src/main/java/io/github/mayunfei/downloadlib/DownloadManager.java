package io.github.mayunfei.downloadlib;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.mayunfei.downloadlib.dao.DownloadDao;
import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.observer.DataChanger;
import io.github.mayunfei.downloadlib.observer.DataWatcher;
import io.github.mayunfei.downloadlib.task.BaseDownloadEntity;
import io.github.mayunfei.downloadlib.task.IBaseDownloadTask;
import io.github.mayunfei.downloadlib.task.IDownloadTask;
import io.github.mayunfei.downloadlib.task.MultiDownloadEntity;
import io.github.mayunfei.downloadlib.task.SingleDownloadEntity;
import io.github.mayunfei.downloadlib.task.SingleDownloadTask;
import io.github.mayunfei.downloadlib.utils.Constants;
import io.github.mayunfei.downloadlib.utils.Utils;
import okhttp3.OkHttpClient;

/**
 * 现在control
 * Created by mayunfei on 17-7-31.
 */

public class DownloadManager {
    private static final String TAG = "DownloadManager";
    private static DownloadManager INSTANCE;
    private OkHttpClient okHttpClient;
    private Context context;
    private String path;

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

    public interface AddDownloadListener {

        void onError();

        void success();
    }

    public ExecutorService getExecutor() {
        return executor;
    }

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
            downloadEntity.setPath(path + File.separator + Utils.encryptMD5ToString(downloadEntity.getKey()));
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


    public void addObserver(DataWatcher dataWatcher) {
        DataChanger.getInstance().addObserver(dataWatcher);
    }

    public void deleteObserver(DataWatcher dataWatcher) {
        DataChanger.getInstance().deleteObserver(dataWatcher);
    }

    public BaseDownloadEntity queryDownloadEntity(String key) {
        return DownloadDao.getInstance().query(key);
    }


    /**
     * 仅仅是下载
     */
    public IBaseDownloadTask addSimpleTask(final String url, final SingleDownloadTask.BaseDownloadTaskListener listener) {
        final SingleDownloadEntity entity = new SingleDownloadEntity(url, url);
        if (TextUtils.isEmpty(entity.getPath())) {
            //TODO 是否要配置呢
            entity.setPath(path + File.separator + Utils.encryptMD5ToString(entity.getKey()));
        }
        IBaseDownloadTask task = new IBaseDownloadTask() {

            SingleDownloadTask task = new SingleDownloadTask(entity, new SingleDownloadTask.DownloadTaskListener() {
                @Override
                public void onUpdate(final BaseDownloadEntity entity) {
                    DataChanger.getInstance().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onUpdate(entity);
                        }
                    });

                }

                @Override
                public void onPause(final BaseDownloadEntity entity) {
                    //不支持暂停
                }

                @Override
                public void onCancel(final BaseDownloadEntity entity) {
                    DataChanger.getInstance().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onCancel(entity);
                        }
                    });
                    Utils.delete(entity.getPath());
                }

                @Override
                public void onFinish(final BaseDownloadEntity entity) {
                    DataChanger.getInstance().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFinish(entity);
                        }
                    });

                    DownloadDao.getInstance().delete(entity.getKey());
                }

                @Override
                public void onError(final BaseDownloadEntity entity) {
                    DataChanger.getInstance().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(entity);
                        }
                    });

                }
            });

            @Override
            public void start() {
                executor.submit(task);
            }

            @Override
            public void cancel() {
                task.cancel();
            }
        };

        return task;
    }

}
