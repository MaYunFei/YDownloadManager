package io.github.mayunfei.downloadlib.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

/**
 * Created by mayunfei on 17-7-26.
 */

public class FileUtils {
    private static final int DOWNLOAD_CACHE_SIZE = 2048;

    private FileUtils() {
    }

    public static void save(InputStream input, OutputStream output) throws IOException {
        byte[] block = new byte[DOWNLOAD_CACHE_SIZE];
        int len = 0;
        while ((len = input.read(block)) != -1) {
            output.write(block, 0, len);
        }
        output.flush();
    }
}
