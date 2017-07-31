package io.github.mayunfei.downloadlib;

import okhttp3.OkHttpClient;

/**
 * Created by mayunfei on 17-7-31.
 */

public class DownloadManager {
    private static DownloadManager INSTANCE;
    private OkHttpClient okHttpClient;

    private DownloadManager() {
    }

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = new DownloadManager();
        } else {
            throw new IllegalArgumentException("you have call init");
        }
        INSTANCE.okHttpClient = new OkHttpClient.Builder().build();
    }

    public static void init(OkHttpClient okHttpClient) {
        if (INSTANCE == null) {
            INSTANCE = new DownloadManager();
        } else {
            throw new IllegalArgumentException("you have call init");
        }
        INSTANCE.okHttpClient = okHttpClient;
    }

    public static DownloadManager getInstance() {
        if (INSTANCE.okHttpClient == null) {
            throw new IllegalArgumentException("First call init");
        }
        return INSTANCE;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public SimpleDownload simpleDownload(String url, String path, String fileName) {
        return new SimpleDownload(url, path, fileName);
    }


}
