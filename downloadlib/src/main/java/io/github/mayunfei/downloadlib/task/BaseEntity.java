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

public class BaseEntity implements Serializable{

    protected String key;
    protected String name;
    protected String path;
    protected long totalSize; //只在一个线程内 不用原子炒作
    protected long currentSize;
    protected    int status = WAIT;


    public BaseEntity(String key, String name, String path) {
        this.key = key;
        this.name = name;
        this.path = path;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

        DownloadEntity that = (DownloadEntity) o;

        return key != null ? key.equals(that.key) : that.key == null;

    }

    @Override
    public String toString() {
        return "DownloadEntity{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", totalSize=" + totalSize +
                ", currentSize=" + currentSize +
                ", status=" + getStatus(status) +
                '}';
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
