package io.github.mayunfei.downloadlib.utils;

/**
 * Created by mayunfei on 17-7-31.
 */

public class Constants {

    public static final String ACTION = "action";
    public static final int ACTION_ADD = 0;
    public static final int ACTION_PAUSE = 1;
    public static final int ACTION_RESUME = 2;
    public static final int ACTION_CANCEL= 3; //delete

    public static final int MAX_DOWNLOADING = 3; //正在
    public static final int MAX_PART_COUNT = 3;  //单任务个数



    private Constants() {
    }

    public static final long TIME = 10 * 1000;
    public static final String KEY = "key";
    public static final String DOWNLOAD_ENTITY = "download_entity";

}
