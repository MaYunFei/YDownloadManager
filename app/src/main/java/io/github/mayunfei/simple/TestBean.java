package io.github.mayunfei.simple;

import io.github.mayunfei.downloadlib.task.BaseDownloadEntity;

/**
 * Created by mayunfei on 17-8-17.
 */

public class TestBean {

    private BaseDownloadEntity baseDownloadEntity;
    private String title;
    private String key;

    public BaseDownloadEntity getBaseDownloadEntity() {
        return baseDownloadEntity;
    }

    public void setBaseDownloadEntity(BaseDownloadEntity baseDownloadEntity) {
        this.baseDownloadEntity = baseDownloadEntity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestBean testBean = (TestBean) o;

        return key != null ? key.equals(testBean.key) : testBean.key == null;

    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
