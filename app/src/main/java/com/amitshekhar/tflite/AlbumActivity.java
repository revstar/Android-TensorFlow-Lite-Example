package com.amitshekhar.tflite;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amitshekhar.tflite.adapter.PictureAdapter;
import com.amitshekhar.tflite.model.MediaBean;
import com.amitshekhar.tflite.model.TypePictureBean;

import org.tensorflow.lite.Tensor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AlbumActivity extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    private Classifier classifier;
    private static final String MODEL_PATH = "detect.tflite";
    private static final boolean QUANT = false;
    private static final String LABEL_PATH = "file:///android_asset/labels.txt";
    private static final int INPUT_SIZE = 320;
    private List<TypePictureBean> mTypePictureBeanList;
    private RecyclerView rvPicture;

    Bitmap original ;
    Bitmap bitmap ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        initView();
        getPermission();
    }

    private void initView() {
        rvPicture = findViewById(R.id.rv_picture);
    }

    private void getPermission() {

        final List<String> permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissionsList.size() == 0) {
                getAllPhotoInfo();
            } else {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 1);
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                Toast.makeText(this, "存储权限被拒绝", Toast.LENGTH_SHORT).show();
            } else {
                getAllPhotoInfo();
            }

        }
    }

    /**
     * 读取手机中所有图片信息
     */
    private void getAllPhotoInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<MediaBean> mediaBeen = new ArrayList<>();
                HashMap<String, List<MediaBean>> allPhotosTemp = new HashMap<>();//所有照片
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String[] projImage = {MediaStore.Images.Media._ID
                        , MediaStore.Images.Media.DATA
                        , MediaStore.Images.Media.SIZE
                        , MediaStore.Images.Media.DISPLAY_NAME};
                Cursor mCursor = getContentResolver().query(mImageUri,
                        projImage,
                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED + " desc");

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        // 获取图片的路径
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        int size = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media.SIZE)) / 1024;
                        String displayName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                        //用于展示相册初始化界面
                        mediaBeen.add(new MediaBean(path, size, displayName));
                        // 获取该图片的父路径名
                        String dirPath = new File(path).getParentFile().getAbsolutePath();
                        //存储对应关系
                        if (allPhotosTemp.containsKey(dirPath)) {
                            List<MediaBean> data = allPhotosTemp.get(dirPath);
                            data.add(new MediaBean(path, size, displayName));
                            continue;
                        } else {
                            List<MediaBean> data = new ArrayList<>();
                            data.add(new MediaBean(path, size, displayName));
                            allPhotosTemp.put(dirPath, data);
                        }
                    }
                    mCursor.close();
                }
                //更新界面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //...
                        queryType(mediaBeen);
                        setRvPicture();
                    }
                });
            }
        }).start();
    }

    private void queryType(List<MediaBean> mediaBeanList) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
        if (mediaBeanList != null) {
            for (MediaBean item : mediaBeanList) {
                if (item != null && item.getLocalPath() != null) {
                    getClassName(item.getLocalPath());
                }
            }
        }


    }

    private void setRvPicture() {
        if (rvPicture != null && mTypePictureBeanList != null) {
            rvPicture.setLayoutManager(new GridLayoutManager(this, 3, RecyclerView.VERTICAL, false));
            PictureAdapter adapter = new PictureAdapter(R.layout.picture_item, mTypePictureBeanList);
            rvPicture.setAdapter(adapter);
        }
    }

    private void getClassName(String picturePath) {


         original = BitmapFactory.decodeFile(picturePath);
         bitmap = Bitmap.createScaledBitmap(original, INPUT_SIZE, INPUT_SIZE, false);

        if (bitmap != null && classifier != null) {
            final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
            if (mTypePictureBeanList == null) {
                mTypePictureBeanList = new ArrayList<>();
            }

            if (results != null && results.get(0) != null && results.get(0).getTitle() != null) {

                mTypePictureBeanList.add(new TypePictureBean(results.get(0).getTitle(), picturePath));
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && classifier != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    classifier.close();
                }
            });
        }

    }
}
