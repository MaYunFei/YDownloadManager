package io.github.mayunfei.downloadlib.dao;

import android.database.Cursor;

/**
 * 数据库工具
 * Created by mayunfei on 17-3-9.
 */

class DBHelper {
  static int getInt(Cursor cursor, String columnName) {
    if (null != cursor) {
      return cursor.getInt(cursor.getColumnIndex(columnName));
    }
    return 0;
  }

  static long getLong(Cursor cursor, String columnName) {
    if (null != cursor) {
      return cursor.getLong(cursor.getColumnIndex(columnName));
    }
    return 0;
  }

  public static boolean getBoolean(Cursor cursor, String columnName) {
    return cursor != null && cursor.getInt(cursor.getColumnIndex(columnName)) > 0;
  }

  static String getString(Cursor cursor, String columnName) {
    if (null != cursor) {
      return cursor.getString(cursor.getColumnIndex(columnName));
    }
    return "";
  }
}
