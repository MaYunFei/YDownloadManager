package io.github.mayunfei.downloadlib.task;

import java.io.Serializable;

import static io.github.mayunfei.downloadlib.event.DownloadEvent.CANCEL;
import static io.github.mayunfei.downloadlib.event.DownloadEvent.DOWNLOADING;
import static io.github.mayunfei.downloadlib.event.DownloadEvent.ERROR;
import static io.github.mayunfei.downloadlib.event.DownloadEvent.FINISH;
import static io.github.mayunfei.downloadlib.event.DownloadEvent.PAUSE;
import static io.github.mayunfei.downloadlib.event.DownloadEvent.WAIT;

/**
 * Created by mayunfei on 17-8-15.
 */

public class BaseDownloadEntity implements Serializable {

    protected String key;
    protected long totalSize; //只在一个线程内 不用原子操作
    protected long currentSize;
    protected int status = WAIT;
    protected String path;
    protected float speed;


    public BaseDownloadEntity(String key) {
        this.key = key;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SingleDownloadEntity that = (SingleDownloadEntity) o;

        return key != null ? key.equals(that.key) : that.key == null;

    }

    @Override
    public String toString() {
        return "DownloadEntity{" +
                "key='" + key + '\'' +
                ", totalSize=" + totalSize +
                ", currentSize=" + currentSize +
                ", status=" + getStatus(status) +
                ", speed=" + speed +
                '}';
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    public static String getStatus(int status) {
        switch (status) {
            case WAIT:
                return "等待";

            case DOWNLOADING:
                return "下载中";

            case PAUSE:
                return "暂停";

            case FINISH:
                return "完成";

            case ERROR:
                return "错误";

            case CANCEL:
                return "取消";

        }
        return "未知";
    }


}
