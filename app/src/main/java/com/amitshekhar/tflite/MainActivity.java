package com.amitshekhar.tflite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.CameraView;


import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String MODEL_PATH = "detect.tflite";
    private static final boolean QUANT = false;
    private static final String LABEL_PATH = "file:///android_asset/labels.txt";
    private static final int INPUT_SIZE = 320;

    private Classifier classifier;

    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnToggleCamera;
    private ImageView imageViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageViewResult = findViewById(R.id.imageViewResult);
        textViewResult = findViewById(R.id.textViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());

        btnToggleCamera = findViewById(R.id.btnToggleCamera);

//        cameraView.addCameraKitListener(new CameraKitEventListener() {
//            @Override
//            public void onEvent(CameraKitEvent cameraKitEvent) {
//
//            }
//
//            @Override
//            public void onError(CameraKitError cameraKitError) {
//
//            }
//
//            @Override
//            public void onImage(CameraKitImage cameraKitImage) {
//
////                Bitmap bitmap = cameraKitImage.getBitmap();
//
//                Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.clothes);
//                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
//
//                imageViewResult.setImageBitmap(bitmap);
//
//                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
//
//                textViewResult.setText(results.toString());
//
//            }
//
//            @Override
//            public void onVideo(CameraKitVideo cameraKitVideo) {
//
//            }
//        });
//
        btnToggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureSelector.create(MainActivity.this)
                        .openGallery(PictureMimeType.ofImage())
                        .loadImageEngine(GlideEngine.createGlideEngine()) // 请参考Demo GlideEngine.java
                        .selectionMode(PictureConfig.SINGLE)
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
        });
//
//        btnDetectObject.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cameraView.captureImage();
//            }
//        });

        initTensorFlowAndLoadModel();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PictureConfig.CHOOSE_REQUEST) {
            List<LocalMedia> localMediaList = PictureSelector.obtainMultipleResult(data);
            if (localMediaList!=null&&localMediaList.get(0)!=null&&localMediaList.get(0).getPath()!=null){
                progress(localMediaList.get(0).getPath());
            }
        }
    }

    private void progress(String path){
                Bitmap original= BitmapFactory.decodeFile(path);
               Bitmap bitmap = Bitmap.createScaledBitmap(original, INPUT_SIZE, INPUT_SIZE, false);

                imageViewResult.setImageBitmap(bitmap);

                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

                if (results!=null&&results.get(0)!=null){
                    textViewResult.setText(results.toString());
                    RectF rectF=results.get(0).getLocation();
                    if (rectF!=null){
                        Bitmap drawRectBitmap=bitmap.copy(Bitmap.Config.ARGB_8888,true);
                        Canvas canvas=new Canvas(drawRectBitmap);
                        final Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(2.0f);
                        canvas.drawRect(rectF,paint);
                        if (imageViewResult!=null){
                            imageViewResult.setImageBitmap(drawRectBitmap);
                        }
                    }
                }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    private void initTensorFlowAndLoadModel() {
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
    }
}
