package io.github.mayunfei.downloadlib.task;

import java.io.Serializable;

import io.github.mayunfei.downloadlib.event.DownloadEvent;


/**
 * Created by mayunfei on 17-8-1.
 */

public class DownloadEntity implements Serializable{

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
    public int status = DownloadEvent.WAIT;

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
}
