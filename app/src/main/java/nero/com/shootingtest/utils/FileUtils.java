package nero.com.shootingtest.utils;

import android.net.Uri;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by zhouyou on 2015/7/26.
 */
public class FileUtils {

    /**
     * 获取是否有此文件
     *
     * @param text
     * @return
     */
    public static File getFile(String text) {
        if (TextUtils.isEmpty(text)) return null;
        File file = new File(text);
        if (file != null && file.exists()) {
            return file;
        }
        return null;
    }

    public static Uri getFileUri(File file) {
        if (file == null || !file.exists()) return null;
        return Uri.parse("file://" + file.getAbsolutePath());
    }
}
