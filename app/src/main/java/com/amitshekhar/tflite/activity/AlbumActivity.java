package com.amitshekhar.tflite.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amitshekhar.tflite.Classifier;
import com.amitshekhar.tflite.R;
import com.amitshekhar.tflite.TFLiteObjectDetectionAPIModel;
import com.amitshekhar.tflite.adapter.PictureAdapter;
import com.amitshekhar.tflite.model.MediaBean;
import com.amitshekhar.tflite.model.TypePictureBean;
import com.amitshekhar.tflite.progressdialog.MProgressBarDialog;
import com.amitshekhar.tflite.utils.DialogUtils;
import com.amitshekhar.tflite.utils.DisplayUtils;
import com.amitshekhar.tflite.utils.MyUtils;


import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    private List<Classifier> classifierList;
    private static final String MODEL_PATH = "detect.tflite";
    private static final boolean QUANT = false;
    private static final String LABEL_PATH = "file:///android_asset/labels.txt";
    private static final int INPUT_SIZE = 320;
    private List<TypePictureBean> mTypePictureBeanList;
    private RecyclerView rvPicture;

    Bitmap bitmap;
    private DialogUtils mDialogUtils;

    private MProgressBarDialog mProgressBarDialog;
    private List<MediaBean> mediaBeen;
    private int handleSize = 0;//处理数据大小

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        initView();
        initProgressBar();
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

    private void initProgressBar() {
        mProgressBarDialog = new MProgressBarDialog.Builder(this)
                .setStyle(MProgressBarDialog.MProgressBarDialogStyle_Horizontal)
                //全屏背景窗体的颜色
                .setBackgroundWindowColor(MyUtils.getMyColor(this, R.color.picture_color_4d))
                //View背景的颜色
                .setBackgroundViewColor(MyUtils.getMyColor(this, R.color.import_color))
                //字体的颜色
                .setTextColor(MyUtils.getMyColor(this, R.color.white))
                //View边框的颜色
                .setStrokeColor(MyUtils.getMyColor(this, R.color.white))
                //View边框的宽度
                .setStrokeWidth(0)
                //ProgressBar背景色
                .setProgressbarBackgroundColor(MyUtils.getMyColor(this, R.color.import_progress_color))
                //ProgressBar 颜色
                .setProgressColor(Color.WHITE)
                //水平进度条Progress圆角
                .setProgressCornerRadius(DisplayUtils.dp2px(this, 3))
                //水平进度条的高度
                .setHorizontalProgressBarHeight(10)
                //dialog动画
                .build();
        mProgressBarDialog.showProgress(0, "正在加载模型...", true);
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

    private static class MyHandler extends Handler {
        private final WeakReference<AlbumActivity> mActivity;

        private MyHandler(AlbumActivity activity) {
            this.mActivity = new WeakReference<AlbumActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AlbumActivity activity = mActivity.get();
            if (activity != null && activity.mediaBeen != null&&msg.what==0x123) {
                int length = activity.mediaBeen.size();
                if (activity.mProgressBarDialog != null) {
                    ++activity.handleSize;
                    activity.mProgressBarDialog.showProgress(100 * activity.handleSize / length, activity.handleSize + "/" + activity.mediaBeen.size(), true);
                    if (activity.handleSize == length) {
                        activity.mProgressBarDialog.setTvImportState("完成");
                        activity.mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                activity.mProgressBarDialog.dismiss();
                                activity.isShowProgress(false);
                                activity.mHandler.removeCallbacksAndMessages(null);
                                activity.setRvPicture();
                            }
                        }, 500);
                    }
                }
            }
        }
    }

    private final MyHandler mHandler = new MyHandler(this);

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
        } else {
            getAllPhotoInfo();
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
                Log.d("开始读取照片", ">>>");
                mediaBeen = new ArrayList<>();
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
                            if (data != null) {
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
                if ((mediaBeen==null||mediaBeen.size()==0)||mProgressBarDialog!=null){
                    mProgressBarDialog.dismiss();
                    }
                int threadSize=(mediaBeen==null||mediaBeen.size()<10)?1:10;
                handleImageList(mediaBeen, threadSize);
            }
        }).start();
    }

    /**
     * 多线程处理list
     *
     * @param data      数据list
     * @param threadNum 线程数
     */
    public synchronized void handleImageList(List<MediaBean> data, int threadNum) {
        int length = data.size();
        int tl = length % threadNum == 0 ? length / threadNum : (length
                / threadNum + 1);

        for (int i = 0; i < threadNum; i++) {
            int end = (i + 1) * tl;
            HandleThread thread = new HandleThread("线程[" + (i + 1) + "] ", data, i * tl, end > length ? length : end);
            thread.start();
        }
    }

    class HandleThread extends Thread {
        private String threadName;
        private List<MediaBean> data;
        private int start;
        private int end;

        public HandleThread(String threadName, List<MediaBean> data, int start, int end) {
            this.threadName = threadName;
            this.data = data;
            this.start = start;
            this.end = end;
        }

        public void run() {
            Log.d("thread start", threadName);
            final Classifier classifier;
            try {
                classifier = TFLiteObjectDetectionAPIModel.create(
                        getAssets(),
                        MODEL_PATH,
                        LABEL_PATH,
                        INPUT_SIZE,
                        QUANT);
                if (classifierList == null) {
                    classifierList = new ArrayList<>();
                }
                classifierList.add(classifier);
            } catch (final Exception e) {
                isShowProgress(false);
                getDialogUtils().showDialog(DialogUtils.Type.ERROR, e.getMessage(), true);
                throw new RuntimeException("Error initializing TensorFlow!", e);
            }
            Log.d(threadName, "处理数据");

            //这里处理数据
            List<MediaBean> subList = data.subList(start, end);
            for (int a = 0; a < subList.size(); a++) {
                MediaBean item = subList.get(a);
                getPictureType(item.getLocalPath(), classifier);
                Message message = mHandler.obtainMessage();
                message.what=0x123;
                mHandler.sendMessage(message);
                System.out.println(threadName + "处理了   " + subList.get(a) + "  ！");
            }
        }

    }

    private void setRvPicture() {
        if (rvPicture != null && mTypePictureBeanList != null) {
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

    private void getPictureType(String picturePath, Classifier classifier) {
        try {
            bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(picturePath), INPUT_SIZE, INPUT_SIZE, false);
            if (bitmap != null && classifier != null) {
                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
                if (mTypePictureBeanList == null) {
                    mTypePictureBeanList = new ArrayList<>();
                }
                if (results != null && results.get(0) != null) {
                    Classifier.Recognition item = results.get(0);
                    if (item.getConfidence() > 0.7) {
                        String title = item.getTitle();
                        if (title != null) {
                            addPictureToList(title, picturePath);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addPictureToList(String title, String picturePath) {
        for (int i = 0; i < mTypePictureBeanList.size(); i++) {
            if (mTypePictureBeanList.get(i).getType().equals(title)) {
                mTypePictureBeanList.get(i).getPicturePaths().add(picturePath);
            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeClassifier();
        isShowProgress(false);

    }

    private void closeClassifier() {
        if (classifierList != null) {
            for (Classifier classifier : classifierList) {
                if (classifier != null) {
                    classifier.close();
                }
            }
        }
    }

    public DialogUtils getDialogUtils() {
        if (mDialogUtils == null) {
            mDialogUtils = new DialogUtils(this);
        }
        return mDialogUtils;
    }

    /**
     * @param isShow true 显示加载 fale 隐藏加载
     */
    public void isShowProgress(boolean isShow) {

        if (isShow) {
            if (mProgressBarDialog != null && !mProgressBarDialog.isShowing()) {
                mProgressBarDialog.dismiss();
            }
        } else {
            if (mProgressBarDialog != null && mProgressBarDialog.isShowing()) {
                mProgressBarDialog.dismiss();
            }
        }
    }

}
