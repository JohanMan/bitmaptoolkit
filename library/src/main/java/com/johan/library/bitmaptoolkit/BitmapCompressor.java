package com.johan.library.bitmaptoolkit;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/7/25.
 */

public class BitmapCompressor implements Handler.Callback {

    private static final String DEFAULT_CACHE_DIR = "bitmap_cache";
    private static final int NOTIFY_COMPLETE = 1;
    private static final int NOTIFY_ERROR = 2;

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Context context;
    private File file;
    private long byteSize;
    private int width, height;
    private Bitmap.Config config;
    private String saveFilePath;
    private CompressCallback compressCallback;

    private Handler handler;

    public BitmapCompressor(Context context, File file, long byteSize, int width, int height, Bitmap.Config config, String saveFilePath, CompressCallback compressCallback) {
        this.context = context;
        this.file = file;
        this.byteSize = byteSize;
        this.width = width;
        this.height = height;
        this.config = config;
        this.saveFilePath = saveFilePath;
        this.compressCallback = compressCallback;
        this.handler = new Handler(Looper.getMainLooper(), this);
    }

    /**
     * 初始化Builder
     * @param context
     * @return
     */
    public static Builder with(Context context) {
        return new Builder(context);
    }

    /**
     * 开始压缩
     */
    public void compress() {
        if (file == null) {
            notifyError(new Exception("file is null"));
            return;
        }
        if (saveFilePath == null) {
            saveFilePath = getImageCacheFilePath();
            if (saveFilePath == null) {
                notifyError(new Exception("save file path is null"));
                return;
            }
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BitmapUtil.compress(file, new File(saveFilePath), byteSize, width, height, config);
                    notifyComplete();
                } catch (IOException e) {
                    e.printStackTrace();
                    notifyError(e);
                }
            }
        });
    }

    /**
     * 获取压缩后保存文件的路径
     * @return
     */
    private String getImageCacheFilePath() {
        if (getImageCacheDir() != null) {
            return getImageCacheDir() + "/" + System.currentTimeMillis() + (int) (Math.random() * 1000) + ".jpg";
        }
        return null;
    }

    /**
     * 获取压缩后保存文件的文件夹
     * @return
     */
    private File getImageCacheDir() {
        return getImageCacheDir(DEFAULT_CACHE_DIR);
    }

    private File getImageCacheDir(String cacheName) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                return null;
            }
            return result;
        }
        return null;
    }

    /**
     * 压缩完成
     */
    private void notifyComplete() {
        handler.obtainMessage(NOTIFY_COMPLETE).sendToTarget();
    }

    /**
     * 压缩失败
     * @param exception
     */
    private void notifyError(Exception exception) {
        handler.obtainMessage(NOTIFY_ERROR, exception).sendToTarget();
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case NOTIFY_COMPLETE :
                if (compressCallback != null) {
                    compressCallback.onComplete(saveFilePath);
                }
                break;
            case NOTIFY_ERROR :
                Exception exception = (Exception) message.obj;
                if (compressCallback != null) {
                    compressCallback.onError(exception);
                }
                break;
        }
        return false;
    }

    public static class Builder {

        private Context context;
        private File file;
        private long byteSize;
        private int width, height;
        private Bitmap.Config config;
        private String saveFilePath;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 加载图片
         * @param file
         * @return
         */
        public Builder load(File file) {
            this.file = file;
            return this;
        }

        /**
         * 加载图片
         * @param filePath
         * @return
         */
        public Builder load(String filePath) {
            this.file = new File(filePath);
            return this;
        }

        /**
         * 加载图片
         * @param uri
         * @return
         */
        public Builder load(Uri uri) {
            String absolutePath = UriToPathUtil.getImageAbsolutePath(context, uri);
            if (absolutePath != null) this.file = new File(absolutePath);
            return this;
        }

        /**
         * 限制压缩大小，尽量做到，不一定做到
         * @param byteSize
         * @return
         */
        public Builder limitByteSize(long byteSize) {
            this.byteSize = byteSize;
            return this;
        }

        /**
         * 压缩图片大小
         * @param width
         * @param height
         * @return
         */
        public Builder resize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * 配置色彩模式
         * @param config
         * ALPHA_8      表示8位Alpha位图,即A=8,一个像素点占用1个字节,它没有颜色,只有透明度
         * ARGB_4444    表示16位ARGB位图，即A=4,R=4,G=4,B=4,一个像素点占4+4+4+4=16位，2个字节
         * ARGB_8888    表示32位ARGB位图，即A=8,R=8,G=8,B=8,一个像素点占8+8+8+8=32位，4个字节
         * RGB_565      表示16位RGB位图,即R=5,G=6,B=5,它没有透明度,一个像素点占5+6+5=16位，2个字节
         * @return
         */
        public Builder config(Bitmap.Config config) {
            this.config = config;
            return this;
        }

        /**
         * 设置压缩之后文件路径
         * @param saveFilePath
         * @return
         */
        public Builder target(String saveFilePath) {
            this.saveFilePath = saveFilePath;
            return this;
        }

        /**
         * 开始压缩
         */
        public void compress(CompressCallback compressCallback) {
            BitmapCompressor compressor = new BitmapCompressor(context, file, byteSize, width, height, config, saveFilePath,compressCallback);
            compressor.compress();
        }

    }

    public interface CompressCallback {
        void onComplete(String filePath);
        void onError(Exception exception);
    }

}
