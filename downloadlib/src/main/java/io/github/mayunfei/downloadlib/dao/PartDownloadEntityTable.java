package io.github.mayunfei.downloadlib.dao;

/**
 * Created by mayunfei on 17-8-21.
 */

public class PartDownloadEntityTable {
    public static final String TABLE_NAME = "PartDownloadEntity";
    public static final String F_KEY = "f_key";
    public static final String KEY = "key";
    public static final String STATUS = "status";
    public static final String PATH = "path";
    public static final String TOTAL_SIZE = "totalSize";
    public static final String CURRENT_SIZE = "currentSize";
    public static final String NAME = "name";
    public static final String URL = "url";


    public static final String CREATE_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + "("
            + KEY
            + " TEXT PRIMARY KEY,"
            + F_KEY
            + " TEXT,"
            + PATH
            + " TEXT NOT NULL,"
            + TOTAL_SIZE
            + " LONG,"
            + CURRENT_SIZE
            + " LONG,"
            + STATUS
            + " INTEGER,"
            + NAME
            + " TEXT,"
            + URL
            + " TEXT"
            + ")";

}
