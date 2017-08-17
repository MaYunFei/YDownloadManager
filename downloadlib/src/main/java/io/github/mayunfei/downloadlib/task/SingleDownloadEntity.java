package io.github.mayunfei.downloadlib.task;

import java.io.Serializable;


/**
 * Created by mayunfei on 17-8-1.
 */

public class SingleDownloadEntity extends BaseDownloadEntity implements Serializable {

    public SingleDownloadEntity(String key, String url, String name, String path) {
        super(key, name, path);
        this.url = url;
    }

    public SingleDownloadEntity(String url, String name, String path) {
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
