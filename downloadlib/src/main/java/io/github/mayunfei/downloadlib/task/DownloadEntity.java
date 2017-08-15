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

public class DownloadEntity extends BaseEntity implements Serializable {

    public DownloadEntity(String key, String url, String name, String path) {
        super(key, name, path);
        this.url = url;
    }

    public DownloadEntity(String url, String name, String path) {
        super(url, name, path);
        this.url = url;
    }

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
