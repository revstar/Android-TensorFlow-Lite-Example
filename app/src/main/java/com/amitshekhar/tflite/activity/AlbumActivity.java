package com.amitshekhar.tflite.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amitshekhar.tflite.Classifier;
import com.amitshekhar.tflite.R;
import com.amitshekhar.tflite.TFLiteObjectDetectionAPIModel;
import com.amitshekhar.tflite.adapter.PictureAdapter;
import com.amitshekhar.tflite.model.MediaBean;
import com.amitshekhar.tflite.model.TypePictureBean;
import com.amitshekhar.tflite.utils.DialogUtils;
import com.revstar.dialog.ToastDialog;


import java.io.File;
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

    Bitmap original;
    Bitmap bitmap;
    private DialogUtils mDialogUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        initView();
        initPictureList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPermission();
    }

    private void initPictureList() {

        if (mTypePictureBeanList == null) {
            mTypePictureBeanList = new ArrayList<>();
        }
        mTypePictureBeanList.add(new TypePictureBean("background", new ArrayList<>()));
        mTypePictureBeanList.add(new TypePictureBean("bag", new ArrayList<>()));
        mTypePictureBeanList.add(new TypePictureBean("belt", new ArrayList<>()));
        mTypePictureBeanList.add(new TypePictureBean("boots", new ArrayList<>()));
        mTypePictureBeanList.add(new TypePictureBean("footwear", new ArrayList<>()));
        mTypePictureBeanList.add(new TypePictureBean("outer", new ArrayList<>()));
        mTypePictureBeanList.add(new TypePictureBean("dress", new ArrayList<>()));
        mTypePictureBeanList.add(new TypePictureBean("sunglasses", new ArrayList<>()));
        mTypePictureBeanList.add(new TypePictureBean("pants", new ArrayList<>()));
        mTypePictureBeanList.add(new TypePictureBean("top", new ArrayList<>()));
        mTypePictureBeanList.add(new TypePictureBean("shorts", new ArrayList<>()));
        mTypePictureBeanList.add(new TypePictureBean("skirt", new ArrayList<>()));
        mTypePictureBeanList.add(new TypePictureBean("headwear", new ArrayList<>()));
        mTypePictureBeanList.add(new TypePictureBean("scarf_and_tie", new ArrayList<>()));

    }

    private void initView() {
        rvPicture = findViewById(R.id.rv_picture);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getDialogUtils().showDialog(DialogUtils.Type.LOADING, "正在导入中...", true);
                    }
                });
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
                            if (data!=null){
                                data.add(new MediaBean(path, size, displayName));
                            }
                            continue;
                        } else {
                            List<MediaBean> data = new ArrayList<>();
                            data.add(new MediaBean(path, size, displayName));
                            allPhotosTemp.put(dirPath, data);
                        }
                    }
                    mCursor.close();
                }
                queryType(mediaBeen);
                //更新界面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //...
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
                    getDialogUtils().showDialog(DialogUtils.Type.ERROR, e.getMessage(), true);
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
        if (mediaBeanList != null) {
            for (MediaBean item : mediaBeanList) {
                if (item != null && item.getLocalPath() != null) {
                    getPictureType(item.getLocalPath());
                }
            }
        }


    }

    private void setRvPicture() {
        if (rvPicture != null && mTypePictureBeanList != null) {
            getDialogUtils().showDialog(DialogUtils.Type.FINISH, "导入成功", true);
            removeEmptyPicture();
            rvPicture.setLayoutManager(new LinearLayoutManager(this));
            PictureAdapter adapter = new PictureAdapter(R.layout.picture_item, mTypePictureBeanList);
            rvPicture.setAdapter(adapter);
        }
    }

    private void removeEmptyPicture() {
        for (int i = mTypePictureBeanList.size() - 1; i >= 0; i--) {
            if (mTypePictureBeanList.get(i) != null && (mTypePictureBeanList.get(i).getPicturePaths() == null
                    || mTypePictureBeanList.get(i).getPicturePaths().size() == 0)) {
                mTypePictureBeanList.remove(i);
            }
        }
    }

    private void getPictureType(String picturePath) {


        original = BitmapFactory.decodeFile(picturePath);
        if (original!=null){
            bitmap = Bitmap.createScaledBitmap(original, INPUT_SIZE, INPUT_SIZE, false);

            if (bitmap != null && classifier != null) {
                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
                if (mTypePictureBeanList == null) {
                    mTypePictureBeanList = new ArrayList<>();
                }

                if (results != null && results.get(0) != null) {
                    String title = results.get(0).getTitle();
                    if (title != null) {
                        addPictureToList(title, picturePath);
                    }
                }
            }
        }

    }

    private void addPictureToList(String title, String picturePath) {
        try {
            for (int i = 0; i < mTypePictureBeanList.size(); i++) {
                if (mTypePictureBeanList.get(i).getType().equals(title)) {
                    mTypePictureBeanList.get(i).getPicturePaths().add(picturePath);
                }
            }

        } catch (Exception e) {
            getDialogUtils().showDialog(DialogUtils.Type.ERROR, e.getMessage(), true);
            e.printStackTrace();
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

    public DialogUtils getDialogUtils() {
        if (mDialogUtils == null) {
            mDialogUtils = new DialogUtils(this);
        }
        return mDialogUtils;
    }
}
