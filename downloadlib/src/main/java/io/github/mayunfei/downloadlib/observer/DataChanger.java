package io.github.mayunfei.downloadlib.observer;

import java.util.Observable;

import io.github.mayunfei.downloadlib.task.DownloadEntity;

/**
 * Created by mayunfei on 17-8-14.
 */

public class DataChanger extends Observable {

    private static DataChanger instance;

    private DataChanger() {

    }

    public static synchronized DataChanger getInstance() {
        if (instance == null) {
            instance = new DataChanger();
        }
        return instance;
    }

    public void postDownloadStatus(DownloadEntity downloadEntity) {
        notifyObservers(downloadEntity);
    }

//    public void addDataWatcher(DataWatcher dataWatcher) {
//        addObserver(dataWatcher);
//    }
//
//    public void removeDataWatcher(DataWatcher dataWatcher) {
//        deleteObserver(dataWatcher);
//    }


}
