package io.github.mayunfei.downloadlib.progress;

/**
 * Created by mayunfei on 17-7-31.
 * https://github.com/lizhangqu/CoreProgress
 */

public interface ProgressListener {

    void update(long bytesRead, long contentLength, boolean done);
}
