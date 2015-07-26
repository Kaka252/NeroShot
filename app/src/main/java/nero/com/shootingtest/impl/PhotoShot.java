package nero.com.shootingtest.impl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.net.URI;

import nero.com.shootingtest.utils.BitmapUtils;
import nero.com.shootingtest.utils.FileUtils;

/**
 * Created by zhouyou on 15-1-15.
 */
public class PhotoShot {

    public static final int LOCAL_PHOTO = 111, SHOT_PHOTO = 112, CUT_PHOTO = 113;

    private static Uri mUri;

    private static File getPhotoFile() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = Environment.getExternalStorageDirectory();
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(file, "IMG_" + System.currentTimeMillis() + ".jpg");
            return file;
        }
        return null;
    }

    /**
     * 启动拍照
     *
     * @param context
     */
    public static void startShotPhoto(Activity context) {
        File file = getPhotoFile();
        Uri uri = null;
        if (file != null) {
            uri = Uri.fromFile(file);
        }
        if (uri != null) {
            mUri = uri;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            try {
                context.startActivityForResult(intent, SHOT_PHOTO);
            } catch (Exception e) {
                Toast.makeText(context, "没有找到可用的相机", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "未找到可用的照片存储路径", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 启动相册
     *
     * @param context
     */
    public static void startLocalPhoto(Activity context) {
        Intent intentAlumb = new Intent(Intent.ACTION_GET_CONTENT);
        intentAlumb.setType("image/*");
        try {
            context.startActivityForResult(intentAlumb, LOCAL_PHOTO);
        } catch (Exception e) {
            Toast.makeText(context, "没有找到可用的相册", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 启动剪裁
     *
     * @param context
     * @param requestCode
     * @param data
     */
    public static void startCutPhoto(Activity context, int requestCode, Intent data) {
        Uri uri = null;
        if (requestCode == LOCAL_PHOTO && data != null) {
            uri = data.getData();
            if (uri == null) {
                Bundle extras = data.getExtras();
                Bitmap bitmap = null;
                if (extras != null) {
                    bitmap = (Bitmap) extras.get("data");
                }
                String path = MediaStore.Images.Media.insertImage(
                        context.getContentResolver(), bitmap, null, null);
                if (!TextUtils.isEmpty(path)) {
                    uri = Uri.parse(path);
                }
            } else {
                String imgPath = getMediaFilePath(context, uri);
                File file = FileUtils.getFile(imgPath);
                if (file != null) {
                    uri = Uri.fromFile(file);
                }
            }
        } else if (requestCode == SHOT_PHOTO) {
            uri = mUri;
        }
        if (uri != null) {
            Intent intent = new Intent();
            intent.setAction("com.android.camera.action.CROP");
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);// 裁剪框比例
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 300);// 输出图片大小
            intent.putExtra("outputY", 300);
            intent.putExtra("return-data", true);
            try {
                context.startActivityForResult(intent, CUT_PHOTO);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "打开相册失败：无法获取手机相册", Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(context, "获取图片失败", Toast.LENGTH_SHORT).show();

        }
    }

    /**
     * 返回结果文件
     *
     * @param context
     * @param requestCode
     * @param data
     * @return
     */
    public static File getPhotoFile(Activity context, int requestCode, Intent data) {
        File file = null;
        if (requestCode == SHOT_PHOTO) {
            file = getPatPhoto(context, mUri, data);
        } else if (requestCode == LOCAL_PHOTO) {
            file = getLocalPhoto(context, data);
        } else if (requestCode == CUT_PHOTO) {
            file = getCutPhoto(context, data);
        }
        return file;
    }

    /**
     * 操作相删返回
     *
     * @param data
     */
    private static File getLocalPhoto(Activity context, Intent data) {
        if (data == null) {
            return null;
        }
        Bundle extras = data.getExtras();
        Bitmap bitmap = null;
        if (extras != null) {
            bitmap = (Bitmap) extras.get("data");
        }
        Uri uri = data.getData();
        if (uri == null && bitmap != null) {
            String path = MediaStore.Images.Media.insertImage(
                    context.getContentResolver(), bitmap, null, null);
            if (!TextUtils.isEmpty(path)) {
                uri = Uri.parse(path);
            }
        }
        File file = null;
        if (uri != null) {
            file = getMediaFile(context, uri);
        } else if (bitmap != null) {
            file = getPhotoFile();
            BitmapUtils.saveBitmap(bitmap, file);
        }
        if (file != null) {
            return file;
        }
        return null;
    }

    /**
     * 操作剪裁返回
     *
     * @param context
     * @param data
     * @return
     */
    private static File getCutPhoto(Activity context, Intent data) {
        return getLocalPhoto(context, data);
    }

    /**
     * 操作照相返回
     */
    private static File getPatPhoto(Activity context, Uri uri, Intent data) {
        if (uri == null) {
            return null;
        }
        File file = getMediaFile(context, uri);
        if (file != null) {
            return file;
        }
        return null;
    }

    /**
     * URI获取路径
     *
     * @param uri
     * @return
     */
    private static File getMediaFile(Activity context, Uri uri) {
        File result = null;
        String img_path = getMediaFilePath(context, uri);
        if (!TextUtils.isEmpty(img_path)) {
            result = new File(img_path);
        }
        if (result == null) {
            try {
                URI URI = new URI(uri.toString());
                if (URI != null) {
                    result = new File(URI);
                }
            } catch (Exception e) {
                result = null;
            }
        }
        return result;
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    private static String getMediaFilePath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= 19;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static void blur(int[] in, int[] out, int width, int height, float radius) {
        int widthMinus1 = width - 1;
        int r = (int) radius;
        int tableSize = 2 * r + 1;
        int divide[] = new int[256 * tableSize];

        for (int i = 0; i < 256 * tableSize; i++)
            divide[i] = i / tableSize;

        int inIndex = 0;

        for (int y = 0; y < height; y++) {
            int outIndex = y;
            int ta = 0, tr = 0, tg = 0, tb = 0;

            for (int i = -r; i <= r; i++) {
                int rgb = in[inIndex + clamp(i, 0, width - 1)];
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            for (int x = 0; x < width; x++) {
                out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb];

                int i1 = x + r + 1;
                if (i1 > widthMinus1) i1 = widthMinus1;
                int i2 = x - r;
                if (i2 < 0) i2 = 0;
                int rgb1 = in[inIndex + i1];
                int rgb2 = in[inIndex + i2];

                ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff) - (rgb2 & 0xff);
                outIndex += height;
            }
            inIndex += width;
        }
    }

    public static void blurFractional(int[] in, int[] out, int width, int height, float radius) {
        radius -= (int) radius;
        float f = 1.0f / (1 + 2 * radius);
        int inIndex = 0;

        for (int y = 0; y < height; y++) {
            int outIndex = y;

            out[outIndex] = in[0];
            outIndex += height;
            for (int x = 1; x < width - 1; x++) {
                int i = inIndex + x;
                int rgb1 = in[i - 1];
                int rgb2 = in[i];
                int rgb3 = in[i + 1];
                int a1 = (rgb1 >> 24) & 0xff;
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = rgb1 & 0xff;
                int a2 = (rgb2 >> 24) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = rgb2 & 0xff;
                int a3 = (rgb3 >> 24) & 0xff;
                int r3 = (rgb3 >> 16) & 0xff;
                int g3 = (rgb3 >> 8) & 0xff;
                int b3 = rgb3 & 0xff;
                a1 = a2 + (int) ((a1 + a3) * radius);
                r1 = r2 + (int) ((r1 + r3) * radius);
                g1 = g2 + (int) ((g1 + g3) * radius);
                b1 = b2 + (int) ((b1 + b3) * radius);
                a1 *= f;
                r1 *= f;
                g1 *= f;
                b1 *= f;
                out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
                outIndex += height;
            }
            out[outIndex] = in[width - 1];
            inIndex += width;
        }
    }

    public static int clamp(int x, int a, int b) {
        return (x < a) ? a : (x > b) ? b : x;
    }

}
