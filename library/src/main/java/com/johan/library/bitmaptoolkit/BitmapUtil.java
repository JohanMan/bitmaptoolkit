package com.johan.library.bitmaptoolkit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2017/7/25.
 */

public class BitmapUtil {

    /**
     * 压缩图片
     * @param srcImage
     * @param tagImage
     * @param byteSize
     * @param tagWidth
     * @param tagHeight
     * @param config
     * @throws IOException
     */
    public static void compress(File srcImage, File tagImage, long byteSize, int tagWidth, int tagHeight, Bitmap.Config config) throws IOException {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(srcImage.getAbsolutePath(), options);

        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;

        if (tagHeight == 0 || tagWidth == 0) {
            options.inSampleSize = computeSize(srcWidth, srcHeight);
        } else {
            options.inSampleSize = computeSize(srcWidth, srcHeight, tagWidth, tagHeight);
        }

        if (config != null) {
            options.inPreferredConfig = config;
        }

        options.inJustDecodeBounds = false;
        Bitmap tagBitmap = BitmapFactory.decodeFile(srcImage.getAbsolutePath(), options);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        tagBitmap = rotatingImage(srcImage, tagBitmap);
        if (byteSize == 0) {
            tagBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        } else {
            int quality = 100;
            tagBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
            long dataSize = byteArrayOutputStream.toByteArray().length;
            while (quality > 30 && dataSize > byteSize) {
                quality = (int) (quality * 0.85);
                byteArrayOutputStream.reset();
                tagBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
                dataSize = byteArrayOutputStream.toByteArray().length;
            }
        }
        tagBitmap.recycle();

        FileOutputStream fileOutputStream = new FileOutputStream(tagImage);
        fileOutputStream.write(byteArrayOutputStream.toByteArray());
        fileOutputStream.flush();
        fileOutputStream.close();
        byteArrayOutputStream.close();

    }

    /**
     * 据说接近微信压缩算法
     * @param srcWidth
     * @param srcHeight
     * @return
     */
    public static int computeSize(int srcWidth, int srcHeight) {
        int sampleSize;
        srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
        srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;
        srcWidth = srcWidth > srcHeight ? srcHeight : srcWidth;
        srcHeight = srcWidth > srcHeight ? srcWidth : srcHeight;
        double scale = ((double) srcWidth / srcHeight);
        if (scale <= 1 && scale > 0.5625) {
            if (srcHeight < 1664) {
                sampleSize = 1;
            } else if (srcHeight >= 1664 && srcHeight < 4990) {
                sampleSize = 2;
            } else if (srcHeight >= 4990 && srcHeight < 10240) {
                sampleSize = 4;
            } else {
                sampleSize = srcHeight / 1280 == 0 ? 1 : srcHeight / 1280;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            sampleSize = srcHeight / 1280 == 0 ? 1 : srcHeight / 1280;
        } else {
            sampleSize = (int) Math.ceil(srcHeight / (1280.0 / scale));
        }
        return sampleSize;
    }

    /**
     * 规定压缩大小
     * @param srcWidth
     * @param srcHeight
     * @param tagWidth
     * @param tagHeight
     * @return
     */
    public static int computeSize(int srcWidth, int srcHeight, int tagWidth, int tagHeight) {
        int sampleSize = 1;
        if (tagWidth > tagHeight && srcWidth > tagWidth) {
            sampleSize = srcWidth / tagWidth;
        } else if (tagWidth < tagHeight && srcHeight > tagHeight) {
            sampleSize = srcHeight / tagHeight;
        }
        return sampleSize;
    }

    /**
     * 旋转图片角度
     * @param srcImage
     * @param bitmap
     * @return
     * @throws IOException
     */
    public static Bitmap rotatingImage(File srcImage, Bitmap bitmap) throws IOException  {
        if (!isJpeg(srcImage)) return bitmap;
        ExifInterface exifInterface = new ExifInterface(srcImage.getAbsolutePath());
        if (exifInterface == null) return bitmap;
        Matrix matrix = new Matrix();
        int angle = 0;
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                angle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                angle = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                angle = 270;
                break;
        }
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 是否是jpg图片
     * @param photo
     * @return
     */
    public static boolean isJpeg(File photo) {
        return photo.getAbsolutePath().contains("jpeg") || photo.getAbsolutePath().contains("jpg");
    }

}
