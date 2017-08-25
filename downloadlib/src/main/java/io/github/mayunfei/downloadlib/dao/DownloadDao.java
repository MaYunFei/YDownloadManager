package io.github.mayunfei.downloadlib.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.github.mayunfei.downloadlib.observer.DataChanger;
import io.github.mayunfei.downloadlib.task.BaseDownloadEntity;
import io.github.mayunfei.downloadlib.task.MultiDownloadEntity;
import io.github.mayunfei.downloadlib.task.SingleDownloadEntity;

/**
 * Created by mayunfei on 17-8-21.
 */

public class DownloadDao {

    private static final String TAG = "DownloadDao";
    private SQLiteHelper sqLiteHelper;

    private static DownloadDao instance;

    private DownloadDao(Context context) {
        this.sqLiteHelper = new SQLiteHelper(context.getApplicationContext());
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new DownloadDao(context);
        }
    }

    public static DownloadDao getInstance() {
        return instance;
    }


    /**
     * 通过key 查询
     *
     * @param key 查询条件
     * @return 查询结构
     */
    public BaseDownloadEntity query(String key) {
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        BaseDownloadEntity entity = null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + DownloadEntityTable.TABLE_NAME + " WHERE " + DownloadEntityTable.KEY + " = ?", new String[]{key});
        try {
            int count = cursor.getCount();
            if (count == 0) {
                return null;
            }
            cursor.moveToFirst();
            int type = DBHelper.getInt(cursor, DownloadEntityTable.TYPE);
            return queryEntityByType(cursor, type, key);
        } finally {
            cursor.close();
        }
    }

    private BaseDownloadEntity queryEntityByType(Cursor cursor, int type, String key) {
        switch (type) {
            case DownloadEntityTable.TYPE_SINGLE:
                return querySingleEntity(cursor, key);
            case DownloadEntityTable.TYPE_MULTI_DOWNLOAD:
                return queryMultiEntity(cursor, key);
        }
        return null;
    }

    private BaseDownloadEntity queryMultiEntity(Cursor entityCursor, String key) {
        //TODO 查询
        int status = DBHelper.getInt(entityCursor, DownloadEntityTable.STATUS);
        String path = DBHelper.getString(entityCursor, DownloadEntityTable.PATH);
        long totalSize = DBHelper.getLong(entityCursor, DownloadEntityTable.TOTAL_SIZE);
        long currentSize = DBHelper.getLong(entityCursor, DownloadEntityTable.CURRENT_SIZE);
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        MultiDownloadEntity entity = null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + PartDownloadEntityTable.TABLE_NAME + " WHERE " + PartDownloadEntityTable.F_KEY + " = ?", new String[]{key});
        try {
            int count = cursor.getCount();
            if (count == 0) {
                return null;
            }
            entity = new MultiDownloadEntity(key);
            entity.setStatus(status);
            entity.setPath(path);
            entity.setTotalSize(totalSize);
            entity.setCurrentSize(currentSize);
            List<SingleDownloadEntity> list = new ArrayList<>();
            while (cursor.moveToNext()) {
                list.add(querySingleEntity(cursor));
            }
            entity.addAllEntity(list);
            return entity;
        } finally {
            cursor.close();
        }
    }

    private SingleDownloadEntity querySingleEntity(Cursor cursor) {
        String url = DBHelper.getString(cursor, PartDownloadEntityTable.URL);
        String key = DBHelper.getString(cursor, PartDownloadEntityTable.KEY);
        String f_key = DBHelper.getString(cursor, PartDownloadEntityTable.F_KEY);
        String path = DBHelper.getString(cursor, PartDownloadEntityTable.PATH);
        long totalSize = DBHelper.getLong(cursor, PartDownloadEntityTable.TOTAL_SIZE);
        long currentSize = DBHelper.getLong(cursor, PartDownloadEntityTable.CURRENT_SIZE);
        int status = DBHelper.getInt(cursor, PartDownloadEntityTable.STATUS);
        SingleDownloadEntity entity = new SingleDownloadEntity(key, url);
        entity.setPath(path);
        entity.setTotalSize(totalSize);
        entity.setCurrentSize(currentSize);
        entity.setStatus(status);
        return entity;
    }

    private BaseDownloadEntity querySingleEntity(Cursor entityCursor, String key) {

        int status = DBHelper.getInt(entityCursor, DownloadEntityTable.STATUS);
        String path = DBHelper.getString(entityCursor, DownloadEntityTable.PATH);
        long totalSize = DBHelper.getLong(entityCursor, DownloadEntityTable.TOTAL_SIZE);
        long currentSize = DBHelper.getLong(entityCursor, DownloadEntityTable.CURRENT_SIZE);

        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        BaseDownloadEntity entity = null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + PartDownloadEntityTable.TABLE_NAME + " WHERE " + PartDownloadEntityTable.F_KEY + " = ?", new String[]{key});
        int count = cursor.getCount();
        if (count == 0) {
            return null;
        }
        try {
            cursor.moveToFirst();
//            String url = DBHelper.getString(cursor, PartDownloadEntityTable.URL);
//            SingleDownloadEntity singleDownloadEntity = new SingleDownloadEntity(key, url);
//            singleDownloadEntity.setStatus(status);
//            singleDownloadEntity.setPath(path);
//            singleDownloadEntity.setCurrentSize(currentSize);
//            singleDownloadEntity.setTotalSize(totalSize);
//            singleDownloadEntity.setKey(key);
            return querySingleEntity(cursor);

        } finally {
            cursor.close();
        }
    }

    public void update(BaseDownloadEntity entity) {
        Log.i(TAG, "update");
        if (entity instanceof SingleDownloadEntity) {
            updateSingleEntity((SingleDownloadEntity) entity);
        }
        if (entity instanceof MultiDownloadEntity) {
            updateMultiEntity((MultiDownloadEntity) entity);
        }
    }

    private void updateSingleEntity(SingleDownloadEntity singleDownloadEntity) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        ContentValues baseContentValues = getBaseContentValues(singleDownloadEntity);
        baseContentValues.put(DownloadEntityTable.TYPE, DownloadEntityTable.TYPE_SINGLE);
        ContentValues partContentValues = getPartContentValues(singleDownloadEntity);
        try {
            db.beginTransaction();
            db.update(DownloadEntityTable.TABLE_NAME, baseContentValues, DownloadEntityTable.KEY + " = ? ", new String[]{singleDownloadEntity.getKey()});
            db.update(PartDownloadEntityTable.TABLE_NAME, partContentValues, PartDownloadEntityTable.KEY + " = ?", new String[]{singleDownloadEntity.getKey()}); //都是一个key
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void updateMultiEntity(MultiDownloadEntity entity) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues baseContentValues = getBaseContentValues(entity);
            baseContentValues.put(DownloadEntityTable.TYPE, DownloadEntityTable.TYPE_MULTI_DOWNLOAD);
            db.update(DownloadEntityTable.TABLE_NAME, baseContentValues, DownloadEntityTable.KEY + " = ? ", new String[]{entity.getKey()});
            //TODO 修改为对应的update
            //将逐个更新 修改为单个MultiTask中
//            for (SingleDownloadEntity singleDownloadEntity : entity.getDownloadEntities()) {
//                ContentValues partContentValues = getPartContentValues(singleDownloadEntity);
//                db.update(PartDownloadEntityTable.TABLE_NAME, partContentValues, PartDownloadEntityTable.KEY + " = ?", new String[]{singleDownloadEntity.getKey()});
//            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void updateMultiPartTable(SingleDownloadEntity singleDownloadEntity) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        ContentValues partContentValues = getPartContentValues(singleDownloadEntity);
        Log.i(TAG,"updatePartTable");
        try {
            db.beginTransaction();
            db.update(PartDownloadEntityTable.TABLE_NAME, partContentValues, PartDownloadEntityTable.KEY + " = ?", new String[]{singleDownloadEntity.getKey()}); //都是一个key
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private ContentValues getBaseContentValues(BaseDownloadEntity entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadEntityTable.KEY, entity.getKey());
        contentValues.put(DownloadEntityTable.STATUS, entity.getStatus());
        contentValues.put(DownloadEntityTable.PATH, entity.getPath());
        contentValues.put(DownloadEntityTable.TOTAL_SIZE, entity.getTotalSize());
        contentValues.put(DownloadEntityTable.CURRENT_SIZE, entity.getCurrentSize());
        return contentValues;
    }

    private ContentValues getPartContentValues(SingleDownloadEntity entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PartDownloadEntityTable.KEY, entity.getKey());
        contentValues.put(PartDownloadEntityTable.STATUS, entity.getStatus());
        contentValues.put(PartDownloadEntityTable.PATH, entity.getPath());
        contentValues.put(PartDownloadEntityTable.TOTAL_SIZE, entity.getTotalSize());
        contentValues.put(PartDownloadEntityTable.CURRENT_SIZE, entity.getCurrentSize());
        contentValues.put(PartDownloadEntityTable.URL, entity.getUrl());
        contentValues.put(PartDownloadEntityTable.NAME, entity.getName());
        return contentValues;
    }


    private void installSingleDownloadEntity(SingleDownloadEntity entity) {
        ContentValues contentValues = getBaseContentValues(entity);
        contentValues.put(DownloadEntityTable.TYPE, DownloadEntityTable.TYPE_SINGLE);
        ContentValues partContentValues = getPartContentValues(entity);
        partContentValues.put(PartDownloadEntityTable.F_KEY, entity.getKey());
        insertDownloadBundle(contentValues, partContentValues);
    }

    /**
     * 插入两个表
     *
     * @param contentValues     DownloadEntityTable
     * @param partContentValues PartDownloadEntityTable
     */
    private void insertDownloadBundle(ContentValues contentValues, ContentValues partContentValues) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            db.insert(DownloadEntityTable.TABLE_NAME, null, contentValues);
            db.insert(PartDownloadEntityTable.TABLE_NAME, null, partContentValues);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void install(BaseDownloadEntity baseEntity) {
        Log.i(TAG, "install");
        if (baseEntity instanceof SingleDownloadEntity) {
            installSingleDownloadEntity((SingleDownloadEntity) baseEntity);
        }
        if (baseEntity instanceof MultiDownloadEntity) {
            installMultiDownloadEntity((MultiDownloadEntity) baseEntity);
        }
    }

    private void installMultiDownloadEntity(MultiDownloadEntity entity) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        ContentValues contentValues = getBaseContentValues(entity);
        contentValues.put(DownloadEntityTable.TYPE, DownloadEntityTable.TYPE_MULTI_DOWNLOAD);
        try {
            db.beginTransaction();
            db.insert(DownloadEntityTable.TABLE_NAME, null, contentValues);
            for (SingleDownloadEntity singleEntity : entity.getDownloadEntities()) {
                ContentValues partContentValues = getPartContentValues(singleEntity);
                partContentValues.put(PartDownloadEntityTable.F_KEY, entity.getKey());
//                partContentValues.put(PartDownloadEntityTable.KEY, singleEntity.getUrl());
                db.insert(PartDownloadEntityTable.TABLE_NAME, null, partContentValues);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }


    }

    public boolean exist(String key) {
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DownloadEntityTable.TABLE_NAME, new String[]{DownloadEntityTable.KEY},
                    DownloadEntityTable.KEY + "=? ", new String[]{key}, null, null, null);

            return cursor.getCount() != 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void delete(String key) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete(DownloadEntityTable.TABLE_NAME, DownloadEntityTable.KEY + " = ? ", new String[]{key});
        db.delete(PartDownloadEntityTable.TABLE_NAME, PartDownloadEntityTable.F_KEY + " = ?", new String[]{key});
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
