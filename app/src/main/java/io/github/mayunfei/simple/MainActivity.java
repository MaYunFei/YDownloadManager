package io.github.mayunfei.simple;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.github.mayunfei.downloadlib.DownloadManager;
import io.github.mayunfei.downloadlib.SimpleDownload;
import io.github.mayunfei.downloadlib.event.DownloadEvent;
import io.github.mayunfei.downloadlib.observer.DataWatcher;
import io.github.mayunfei.downloadlib.task.BaseEntity;
import io.github.mayunfei.downloadlib.task.DownloadEntity;
import io.github.mayunfei.downloadlib.task.MultiDownloadEntity;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    DataWatcher watcher = new DataWatcher() {
        @Override
        public void notifyUpdate(BaseEntity data) {
            Log.e("TAG ", "Thread = " + Thread.currentThread().getName() +"   "+ data.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadManager.init(this);
//        SimpleDownload simpleDownload = DownloadManager.getInstance().simpleDownload("http://down.apps.sina.cn/sinasrc/f2/40/39e9780c0ab67c8494247515f6b540f2.apk", Environment.getExternalStorageDirectory().getAbsolutePath(), "helloworld.apk");
//        simpleDownload.start().subscribe(new Consumer<DownloadEvent>() {
//            @Override
//            public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
//                Log.e("111", downloadEvent.toString());
//            }
//        }, new Consumer<Throwable>() {
//            @Override
//            public void accept(@NonNull Throwable throwable) throws Exception {
//                Log.e("222", throwable.toString());
//            }
//        });


//        final DownloadEntity downloadEntity = new DownloadEntity(
//                "http://down.apps.sina.cn/sinasrc/f2/40/39e9780c0ab67c8494247515f6b540f2.apk",
//                "http://down.apps.sina.cn/sinasrc/f2/40/39e9780c0ab67c8494247515f6b540f2.apk",
//                "helloworld.apk", Environment.getExternalStorageDirectory().getAbsolutePath());
//        DownloadManager.getInstance().add(downloadEntity);


        MultiDownloadEntity multiDownloadEntity = new MultiDownloadEntity("http://down.apps.sina.cn/sinasrc/f2/40/39e9780c0ab67c8494247515f6b540f2"
                , "helo", Environment.getExternalStorageDirectory().getAbsolutePath());
        List<DownloadEntity> downloadEntities = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            DownloadEntity entity = new DownloadEntity("url " + i, "name", Environment.getExternalStorageDirectory().getAbsolutePath());
            downloadEntities.add(entity);
        }
        multiDownloadEntity.addAllEntity(downloadEntities);

        DownloadManager.getInstance().add(multiDownloadEntity);


        DownloadManager.getInstance().addObserver(watcher);

        //全局保存 downloadEntity 实现 重新下载

//        DownloadManager.getInstance().add(this, downloadEntity);
//        Disposable subscribe = DownloadManager.getInstance().getDownloadProcessor(downloadEntity.getKey())
//                .subscribe(new Consumer<DownloadEvent>() {
//                    @Override
//                    public void accept(@NonNull DownloadEvent downloadEvent) throws Exception {
//                        Log.e("YunFei", downloadEvent.toString());
//                    }
//                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadManager.getInstance().delectObserver(watcher);
    }
}
