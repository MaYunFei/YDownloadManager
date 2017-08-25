package io.github.mayunfei.downloadlib.task;

import java.io.IOException;

import io.github.mayunfei.downloadlib.DownloadManager;
import io.github.mayunfei.downloadlib.dao.DownloadDao;
import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.progress.ProgressListener;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 单个文件多线程range
 * Created by mayunfei on 17-8-21.
 */

public class SingleMultPartDownloadTask implements IDownloadTask, ProgressListener {
    SingleDownloadEntity entity;
    private volatile boolean isPause;
    private volatile boolean isCancel;
    private SingleDownloadTask.DownloadTaskListener downloadListener;

    public SingleMultPartDownloadTask(SingleDownloadEntity entity, SingleDownloadTask.DownloadTaskListener downloadListener) {
        this.entity = entity;
        this.downloadListener = downloadListener;
    }

    @Override
    public void start() {
        if (entity.totalSize == 0){
            //第一次
            String url = entity.getUrl();
            OkHttpClient okHttpClient = DownloadManager.getInstance().getOkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                if (response.isSuccessful()){
                    ResponseBody body = response.body();
                    if (body!=null){
                        long contentLength = body.contentLength();
                        entity.setTotalSize(contentLength);
                    }else {
                        entity.status = DownloadEvent.ERROR;
                        downloadListener.onError(entity);
                    }
                }else {
                    entity.status = DownloadEvent.ERROR;
                    downloadListener.onError(entity);
                }
            } catch (IOException e) {
                entity.status = DownloadEvent.ERROR;
                downloadListener.onError(entity);
//                e.printStackTrace();
            }
        }
//        DownloadDao.getInstance().query()

        //分多个range

    }

    @Override
    public void pause() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void run() {
        start();
    }

    @Override
    public void update(long bytesRead, long contentLength, boolean done) {

    }
}
