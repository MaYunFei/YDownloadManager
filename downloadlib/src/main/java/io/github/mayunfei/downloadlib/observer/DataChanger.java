package io.github.mayunfei.downloadlib.observer;

import android.os.Handler;
import android.os.Looper;

import java.util.Observable;

import io.github.mayunfei.downloadlib.dao.DownloadDao;
import io.github.mayunfei.downloadlib.task.BaseDownloadEntity;

/**
 * Created by mayunfei on 17-8-14.
 */

public class DataChanger extends Observable {

    private static DataChanger instance;

    public static synchronized DataChanger getInstance() {
        if (instance == null) {
            instance = new DataChanger();
        }
        return instance;
    }

    private Handler handler;

    private DataChanger() {
        handler = new Handler(Looper.myLooper());
    }

    public void runOnMainThread(Runnable runnable){
        handler.post(runnable);
    }

    public void postDownloadStatus(final BaseDownloadEntity downloadEntity) {
        DownloadDao.getInstance().update(downloadEntity);
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                setChanged();
                notifyObservers(downloadEntity);
            }
        });
//        notifyObservers(downloadEntity);
    }

//    public void addDataWatcher(DataWatcher dataWatcher) {
//        addObserver(dataWatcher);
//    }
//
//    public void removeDataWatcher(DataWatcher dataWatcher) {
//        deleteObserver(dataWatcher);
//    }


}
