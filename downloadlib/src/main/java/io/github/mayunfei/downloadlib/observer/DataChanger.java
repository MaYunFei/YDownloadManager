package io.github.mayunfei.downloadlib.observer;

import android.os.Handler;
import android.os.Looper;

import java.util.Observable;

import io.github.mayunfei.downloadlib.task.BaseDownloadEntity;

/**
 * Created by mayunfei on 17-8-14.
 */

public class DataChanger extends Observable {

    private static DataChanger instance;
    private Handler handler;

    private DataChanger() {
        handler = new Handler(Looper.myLooper());
    }

    public static synchronized DataChanger getInstance() {
        if (instance == null) {
            instance = new DataChanger();
        }
        return instance;
    }

    public void postDownloadStatus(final BaseDownloadEntity downloadEntity) {

        handler.post(new Runnable() { //切换线程
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
