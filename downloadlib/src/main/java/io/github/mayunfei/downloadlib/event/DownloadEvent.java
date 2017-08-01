package io.github.mayunfei.downloadlib.event;

/**
 * Created by mayunfei on 17-7-26.
 */

public class DownloadEvent {
    public static final int WAIT = 0;
    public static final int DOWNLOADING = 1;
    public static final int PAUSE = 2;
    public static final int FINISH = 3;
    public static final int ERROR = 4;
    public static final int CANCEL = 5;

    public int status = WAIT;
    public long speed;
    public long totalSize;
    public long currentSize;
    public Exception exception;


    @Override
    public String toString() {
        return "DownloadEvent{" +
                "status=" + getStatus(status) +
                ", speed=" + getSpeed(speed) +
                ", totalSize=" + totalSize +
                ", currentSize=" + currentSize +
                ", exception=" + exception +
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

    /**
     * @param speed 每毫秒字节 byte/ms
     * @return kb/s m/s
     */
    public static String getSpeed(long speed) {
        long kbs = speed * 1000 / 1024;
        if (kbs < 1024) {
            return kbs + "kb/s";
        } else {
            return kbs / 1024 + "m/s";
        }
    }
}
