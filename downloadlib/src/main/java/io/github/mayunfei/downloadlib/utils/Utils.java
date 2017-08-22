package io.github.mayunfei.downloadlib.utils;

import android.text.TextUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Created by mayunfei on 17-8-21.
 */

public class Utils {
    public static String getFileNameFromUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        return System.currentTimeMillis() + "";
    }

    /**
     * 关闭流
     *
     * @param closeables io
     */
    public static void close(Closeable... closeables) {
        for (Closeable io : closeables) {
            if (io != null) {
                try {
                    io.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void delete(String path) {
        deleteDir(path);
    }

    /**
     * 删除目录
     *
     * @param dirPath 目录路径
     * @return {@code true}: 删除成功<br>{@code false}: 删除失败
     */
    public static boolean deleteDir(String dirPath) {
        return deleteDir(getFileByPath(dirPath));
    }

    /**
     * 删除目录
     *
     * @param dir 目录
     * @return {@code true}: 删除成功<br>{@code false}: 删除失败
     */
    public static boolean deleteDir(File dir) {
        if (dir == null) return false;
        // 目录不存在返回true
        if (!dir.exists()) return true;
        // 不是目录返回false
        if (!dir.isDirectory()) return false;
        // 现在文件存在且是文件夹
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!deleteFile(file)) return false;
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * 删除文件
     *
     * @param file 文件
     * @return {@code true}: 删除成功<br>{@code false}: 删除失败
     */
    public static boolean deleteFile(File file) {
        return file != null && (!file.exists() || file.isFile() && file.delete());
    }

    public static File getFileByPath(String filePath) {
        return TextUtils.isEmpty(filePath) ? null : new File(filePath);
    }
}
