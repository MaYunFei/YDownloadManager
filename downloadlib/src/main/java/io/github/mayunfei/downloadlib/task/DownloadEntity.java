package io.github.mayunfei.downloadlib.task;

import java.io.Serializable;

import io.github.mayunfei.downloadlib.event.DownloadEvent;

import static io.github.mayunfei.downloadlib.event.DownloadEvent.CANCEL;
import static io.github.mayunfei.downloadlib.event.DownloadEvent.DOWNLOADING;
import static io.github.mayunfei.downloadlib.event.DownloadEvent.ERROR;
import static io.github.mayunfei.downloadlib.event.DownloadEvent.FINISH;
import static io.github.mayunfei.downloadlib.event.DownloadEvent.PAUSE;
import static io.github.mayunfei.downloadlib.event.DownloadEvent.WAIT;


/**
 * Created by mayunfei on 17-8-1.
 */

public class DownloadEntity implements Serializable {

    public DownloadEntity(String key, String url, String name, String path) {
        this.key = key;
        this.url = url;
        this.name = name;
        this.path = path;
    }

    String key;
    String url;
    String name;
    String path;

    long totalSize;
    long currentSize;
    public int status = WAIT;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadEntity that = (DownloadEntity) o;

        return key != null ? key.equals(that.key) : that.key == null;

    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "DownloadEntity{" +
                "key='" + key + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", totalSize=" + totalSize +
                ", currentSize=" + currentSize +
                ", status=" + getStatus(status) +
                '}';
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
