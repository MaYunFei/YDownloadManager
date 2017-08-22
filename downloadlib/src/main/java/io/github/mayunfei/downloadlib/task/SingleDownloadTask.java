package io.github.mayunfei.downloadlib.task;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import io.github.mayunfei.downloadlib.DownloadManager;
import io.github.mayunfei.downloadlib.SimpleDownload;
import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.observer.DataChanger;
import io.github.mayunfei.downloadlib.progress.ProgressListener;
import io.github.mayunfei.downloadlib.progress.ProgressResponseBody;
import io.github.mayunfei.downloadlib.utils.Constants;
import io.github.mayunfei.downloadlib.utils.Utils;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http2.StreamResetException;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import static io.github.mayunfei.downloadlib.utils.Utils.getFileNameFromUrl;

/**
 * 单个文件
 * Created by mayunfei on 17-7-26.
 */

public class SingleDownloadTask implements Runnable, IDownloadTask, ProgressListener {
    private static final String TAG = "SingleDownloadTask";
    private SingleDownloadEntity entity;
    private volatile boolean isPause;
    private volatile boolean isCancel;
    private DownloadTaskListener downloadListener;


    //计算速度
    private long lastRefreshTime = 0L;
    private long lastBytesWritten = 0L;

    private Call mCall;
    private int retryCount = 0; //重试的次数


    public SingleDownloadTask(SingleDownloadEntity entity, DownloadTaskListener onDownloadListener) {
        this.entity = entity;
        this.downloadListener = onDownloadListener;
    }

    @Override
    public void start() {
        Log.d(TAG, " start retry = " + retryCount + "  " + entity.getUrl());
        entity.status = DownloadEvent.DOWNLOADING;
        String url = entity.getUrl();
        OkHttpClient okHttpClient = DownloadManager.getInstance().getOkHttpClient();
        Request request = new Request.Builder().url(url).build();
        mCall = okHttpClient.newCall(request);
        String path = entity.getPath();
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        BufferedSink sink = null;
        BufferedSource source = null;
        try {
            Response response = mCall.execute();
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body != null) {
                    entity.totalSize = body.contentLength();
                    if (TextUtils.isEmpty(entity.name)) {
                        entity.name = getFileNameFromUrl(entity.url);
                    }
                    File downloadedFile = new File(path, entity.name);
                    sink = Okio.buffer(Okio.sink(downloadedFile));
                    source = new ProgressResponseBody(response.body(), this).source();
                    sink.writeAll(source);
                    sink.flush();
                }
            } else {
                if (response.code() == 404) {
                    //400 文件为找到 log
                    Log.e(TAG, "文件未找到 " + entity.getUrl());
                }
                Log.e(TAG, "error code " + response.code() + " " + entity.getUrl());
                downloadListener.onError(entity);
            }

        } catch (IOException e) {
            if (e instanceof StreamResetException) { //取消
                return;
            }
            if (retry(e)) {
                //超时
                if (retryCount < Constants.RETRY_COUNT) {
                    retryCount++;
                    start();
                    return;
                }
            }
            Log.e(TAG, e.toString());
            downloadListener.onError(entity);
        } finally {
            Utils.close(sink, source);
        }

//        text();  测试用

//        DataChanger.getInstance().postDownloadStatus(entity);
    }


    private boolean retry(Exception e) {
        return e instanceof SocketTimeoutException
                || e instanceof SocketException
                || e instanceof ProtocolException;
    }

    @Override
    public void pause() {
        isPause = true;
    }

    @Override
    public void cancel() {
        isCancel = true;
    }

    @Override
    public void run() {
        start();
    }

    @Override
    public void update(long bytesRead, long contentLength, boolean done) {
        //暂停 或者取消
        if (isPause || isCancel) {
            if (mCall != null && !mCall.isCanceled()) {
                mCall.cancel();
            }
            entity.status = isPause ? DownloadEvent.PAUSE : DownloadEvent.CANCEL;
            if (isPause) {
                downloadListener.onPause(entity);
            } else {
                downloadListener.onCancel(entity);
            }
            return;
        }

        if (bytesRead == -1 && contentLength == -1) {
            //长度问题
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime >= Constants.UPDATE_PRE_TIME && bytesRead != contentLength && !done) {
            long intervalTime = (currentTime - lastRefreshTime);
            if (intervalTime == 0) {
                intervalTime += 1;
            }
            long updateBytes = bytesRead - lastBytesWritten;
            final long networkSpeed = updateBytes / intervalTime;
            Log.i(TAG, entity.getKey() + " speed = " + networkSpeed + "  updateBytes = " + updateBytes + "  time = " + intervalTime);
            update(bytesRead, contentLength, networkSpeed);
            entity.speed = networkSpeed;

            lastRefreshTime = System.currentTimeMillis();
            lastBytesWritten = bytesRead;
        }
        if (bytesRead == contentLength && done) {
            finished(bytesRead, contentLength);
        }

    }

    private void finished(long bytesRead, long contentLength) {
        entity.status = DownloadEvent.FINISH;
        entity.currentSize = bytesRead;
        downloadListener.onFinish(entity);
    }

    private void update(long bytesRead, long contentLength, float speed) {
        entity.status = DownloadEvent.DOWNLOADING;
        entity.currentSize = bytesRead;
        entity.speed = speed;
        downloadListener.onUpdate(entity);
    }

    public interface DownloadTaskListener {
        void onUpdate(BaseDownloadEntity entity);

        void onPause(BaseDownloadEntity entity);

        void onCancel(BaseDownloadEntity entity);

        void onFinish(BaseDownloadEntity entity);

        void onError(BaseDownloadEntity entity);
    }


//    private void text() {
//        entity.totalSize = 1024 * 5;
//        for (int i = 0; i < entity.totalSize; i++) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            if (isPause || isCancel) {
//                entity.status = isPause ? DownloadEvent.PAUSE : DownloadEvent.CANCEL;
//                if (isPause) {
//                    downloadListener.onPause(entity);
//                } else {
//                    downloadListener.onCancel(entity);
//                }
//                return;
//            }
//
//            entity.currentSize = i += 1024;
//            entity.speed = 1024; //更新速度
////            DataChanger.getInstance().postDownloadStatus(entity);
//        }
//    }
}
