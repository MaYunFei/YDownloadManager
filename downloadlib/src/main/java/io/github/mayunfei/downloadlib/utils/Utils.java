package io.github.mayunfei.downloadlib.utils;

import android.text.TextUtils;

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
}
