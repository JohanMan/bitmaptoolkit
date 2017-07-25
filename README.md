# bitmaptoolkit
Bitmap工具库

## bitmap压缩 使用实例
```
Uri uri = data.getData();
BitmapCompressor.with(MainActivity.this)
        .load(uri)
        .resize(480, 640)          
        .limitByteSize(100 * 1024)
        .config(Bitmap.Config.RGB_565)
        .target("文件保存路径")
        .compress(new BitmapCompressor.CompressCallback() {
            @Override
            public void onComplete(String filePath) {
                imageView.setImageBitmap(BitmapFactory.decodeFile(filePath));
            }
            @Override
            public void onError(Exception exception) {
                Log.e(getClass().getName(), exception.getMessage());
            }
        });
```
