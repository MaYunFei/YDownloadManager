package io.github.mayunfei.downloadlib.task;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by mayunfei on 17-7-26.
 */

public interface DownloadTask {
    void start();
    void pause();
    void cancel();
}
