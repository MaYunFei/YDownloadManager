package io.github.mayunfei.downloadlib;

import java.io.File;
import java.io.IOException;

import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.progress.ProgressListener;
import io.github.mayunfei.downloadlib.progress.ProgressResponseBody;
import io.github.mayunfei.downloadlib.task.SingleDownloadEntity;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.subjects.PublishSubject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.BufferedSink;
import okio.Okio;

/**
 * Created by mayunfei on 17-7-26.
 */

public class SimpleDownload implements ProgressListener {

    private final BehaviorProcessor<DownloadEvent> downloadProcessor;
    private SingleDownloadEntity downloadEntity;
    private OkHttpClient httpClient;
    private Call call;
    private DownloadEvent event;

    //计算速度
    private long lastRefreshTime = 0L;
    private long lastBytesWritten = 0L;
    private int minTime = 100;//最小回调时间100ms，避免频繁回调

    SimpleDownload(SingleDownloadEntity downloadEntity) {
        this.downloadEntity = downloadEntity;
        httpClient = DownloadManager.getInstance().getOkHttpClient();
        downloadProcessor = BehaviorProcessor.create();
        event = new DownloadEvent();
    }

    public Flowable<DownloadEvent> start() {

        Request request = new Request.Builder().url(downloadEntity.getUrl()).build();

        call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //TODO event 需要工厂模式
                //删除文件
//                error(e);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    File downloadedFile = new File(downloadEntity.getPath(), downloadEntity.getName());
                    BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
                    sink.writeAll(new ProgressResponseBody(response.body(), SimpleDownload.this).source());
                    sink.close();
                } else {
                    downloadProcessor.onError(new Exception(response.message()));
                }
            }
        });

        PublishSubject<DownloadEvent> publishSubject = PublishSubject.create();


        return downloadProcessor.observeOn(AndroidSchedulers.mainThread()).onBackpressureLatest().observeOn(AndroidSchedulers.mainThread()).onBackpressureLatest();
    }


    public void cancel() {
        call.cancel();
    }

    @Override
    public void update(long bytesRead, long contentLength, boolean done) {

//        if (bytesRead == -1 && contentLength == -1) {
//            //长度问题
//            return;
//        }
//        long currentTime = System.currentTimeMillis();
//        if (currentTime - lastRefreshTime >= minTime || bytesRead == contentLength || done) {
//            long intervalTime = (currentTime - lastRefreshTime);
//            if (intervalTime == 0) {
//                intervalTime += 1;
//            }
//            long updateBytes = bytesRead - lastBytesWritten;
//            final long networkSpeed = updateBytes / intervalTime;
//            update(bytesRead, contentLength, networkSpeed);
//            lastRefreshTime = System.currentTimeMillis();
//            lastBytesWritten = bytesRead;
//        }
//        if (bytesRead == contentLength && done) {
//            finished(bytesRead, contentLength);
//        }
    }

//    private void error(Exception e) {
//        event.status = DownloadEvent.ERROR;
//        event.exception = e;
//        downloadProcessor.onNext(event);
//    }
//
//    private void update(long bytesRead, long contentLength, long speed) {
//        event.status = DownloadEvent.DOWNLOADING;
//        event.totalSize = contentLength;
//        event.currentSize = bytesRead;
//        event.speed = speed;
//        downloadProcessor.onNext(event);
//    }
//
//    private void finished(long bytesRead, long contentLength) {
//        event.status = DownloadEvent.FINISH;
//        event.totalSize = contentLength;
//        event.currentSize = bytesRead;
//        downloadProcessor.onNext(event);
//    }

}
