package io.github.mayunfei.downloadlib.observer;

import java.util.Observable;
import java.util.Observer;

import io.github.mayunfei.downloadlib.task.DownloadEntity;

/**
 * Created by mayunfei on 17-8-14.
 */

 public abstract class DataWatcher implements Observer {
    @Override
    public void update(Observable o, Object data) {
        if (data instanceof DownloadEntity) {
            notifyUpdate((DownloadEntity) data);
        }
    }

    public abstract void notifyUpdate(DownloadEntity data);
}
