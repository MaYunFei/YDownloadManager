package io.github.mayunfei.downloadlib.dao;

/**
 * DownloadEntity 表
 * Created by mayunfei on 17-8-21.
 */

public class DownloadEntityTable {

    public static final int TYPE_SINGLE = 0;
    public static final int TYPE_MULTI_DOWNLOAD = 1;
    public static final int TYPE_PART_DOWNLOAD = 2
            ;

    public static final String TABLE_NAME = "DownloadEntity";
    public static final String KEY = "key";
    public static final String STATUS = "status";
    public static final String PATH = "path";
    public static final String TOTAL_SIZE = "totalSize";
    public static final String CURRENT_SIZE = "currentSize";
    public static final String TYPE = "type";

    public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + " ("
            + KEY
            + " TEXT PRIMARY KEY,"
            + PATH
            + " TEXT NOT NULL,"
            + TOTAL_SIZE
            + " LONG,"
            + CURRENT_SIZE
            + " LONG,"
            + STATUS
            + " INTEGER,"
            + TYPE
            + " INTEGER"
            + ")";

//    query()  //查询
//    insert()　//插入
//    delete()  //删除
//    update()  //更新


}
