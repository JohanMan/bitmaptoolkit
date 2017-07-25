package com.johan.bitmaptoolkit;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.johan.library.bitmaptoolkit.BitmapCompressor;

public class MainActivity extends AppCompatActivity {

    private static final int PICTURE = 10086;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.compressed_image);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }

    }

    public void takePicture(View view) {
        new AlertDialog.Builder(this)
                .setTitle("选择图片")
                .setItems(new String[]{"拍照", "相册"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0 : take();break;
                            case 1 : pick();break;
                        }
                    }
                })
                .show();
    }

    private void take() {

    }

    private void pick() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            this.finish();
            return;
        }
        switch (requestCode) {
            case PICTURE :
                final Uri uri = data.getData();
                Log.e(getClass().getName(), "uri : " + uri);
                BitmapCompressor.with(this)
                        .load(uri)
                        .limitByteSize(100 * 1024)
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
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
