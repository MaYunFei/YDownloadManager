package io.github.mayunfei.downloadlib.task;

import java.io.Serializable;


/**
 * Created by mayunfei on 17-8-1.
 */

public class SingleDownloadEntity extends BaseDownloadEntity implements Serializable {

    String name;
    String url;
    public SingleDownloadEntity(String key, String url) {
        super(key);
        this.url = url;
    }

//    public SingleDownloadEntity(String key, String url,String name) {
//        super(key);
//        this.url = url;
//        this.name = name;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
