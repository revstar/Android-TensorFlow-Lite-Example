package com.amitshekhar.tflite.progressdialog;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.amitshekhar.tflite.R;
import com.amitshekhar.tflite.utils.DisplayUtils;

/**
 * 带进度条的Dialog
 */

public class MProgressBarDialog {

    //水平方向的
    public final static int MProgressBarDialogStyle_Horizontal = 0;
    //圆形的
    public final static int MProgressBarDialogStyle_Circle = 1;

    //动画时长
    private long mDuration = 300;


    private Context mContext;
    private BaseDialog mDialog;

    private MProgressBarDialog.Builder mBuilder;

    private RelativeLayout dialog_window_background;
    private RelativeLayout dialog_view_bg;
    private TextView tvShow;
    private ProgressBar horizontalProgressBar;

    private TextView tvImportState;

    public MProgressBarDialog(Context context) {
        this(context, new MProgressBarDialog.Builder(context));
    }

    public MProgressBarDialog(Context context, MProgressBarDialog.Builder builder) {
        mContext = context;
        mBuilder = builder;
        if (mBuilder == null) {
            mBuilder = new MProgressBarDialog.Builder(mContext);
        }
        //初始化
        initDialog();
    }

    private void initDialog() {
        checkDialogConfig();
        try {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View mProgressDialogView = inflater.inflate(R.layout.mn_progress_bar_dialog_layout, null);
            mDialog = new BaseDialog(mContext, R.style.MNCustomDialog);
            mDialog.setContentView(mProgressDialogView);

            //获取布局
            dialog_window_background = (RelativeLayout) mProgressDialogView.findViewById(R.id.dialog_window_background);
            dialog_view_bg = (RelativeLayout) mProgressDialogView.findViewById(R.id.dialog_view_bg);
            tvShow = (TextView) mProgressDialogView.findViewById(R.id.tvShow);
            horizontalProgressBar = (ProgressBar) mProgressDialogView.findViewById(R.id.horizontalProgressBar);
            tvImportState=mProgressDialogView.findViewById(R.id.tv_import_state);

            horizontalProgressBar.setVisibility(View.GONE);

            horizontalProgressBar.setProgress(0);
            horizontalProgressBar.setSecondaryProgress(0);
            tvShow.setText("");

            //默认配置
            configView();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkDialogConfig() {
        if (mBuilder == null) {
            mBuilder = new MProgressBarDialog.Builder(mContext);
        }
    }



    public void setTvImportState(String state){
        if (tvImportState!=null){
            tvImportState.setText(state);
        }
    }
    public int getProgress(){
        if (horizontalProgressBar!=null){
            return horizontalProgressBar.getProgress();
        }
        return 0;
    }
    private void configView() {
        try {
            //设置动画
            if (mBuilder != null && mBuilder.animationID != 0 && mDialog.getWindow() != null) {
                mDialog.getWindow().setWindowAnimations(mBuilder.animationID);
            }
        } catch (Exception e) {

        }
        dialog_window_background.setBackgroundColor(mBuilder.backgroundWindowColor);
        tvShow.setTextColor(mBuilder.textColor);

        GradientDrawable myGrad = (GradientDrawable) dialog_view_bg.getBackground();
        myGrad.setColor(mBuilder.backgroundViewColor);
        myGrad.setStroke(DisplayUtils.dp2px(mContext, mBuilder.strokeWidth), mBuilder.strokeColor);
        myGrad.setCornerRadius(DisplayUtils.dp2px(mContext, mBuilder.cornerRadius));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dialog_view_bg.setBackground(myGrad);
        } else {
            dialog_view_bg.setBackgroundDrawable(myGrad);
        }

        //horizontalProgressBar 配置
        //背景
        GradientDrawable progressBarBackgroundDrawable = new GradientDrawable();
        progressBarBackgroundDrawable.setColor(mBuilder.progressbarBackgroundColor);
        progressBarBackgroundDrawable.setCornerRadius(DisplayUtils.dp2px(mContext, mBuilder.progressCornerRadius));
        //二级进度条
        GradientDrawable secondProgressDrawable = new GradientDrawable();
        secondProgressDrawable.setColor(mBuilder.progressbarBackgroundColor);
        secondProgressDrawable.setCornerRadius(DisplayUtils.dp2px(mContext, mBuilder.progressCornerRadius));
        ClipDrawable hProgressBar02 = new ClipDrawable(secondProgressDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        //一级进度条
        GradientDrawable progressDrawable = new GradientDrawable();
        progressDrawable.setColor(mBuilder.progressColor);
        progressDrawable.setCornerRadius(DisplayUtils.dp2px(mContext, mBuilder.progressCornerRadius));
        ClipDrawable hProgressBar03 = new ClipDrawable(progressDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        //组合
        Drawable[] layers = {progressBarBackgroundDrawable, hProgressBar02, hProgressBar03};
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.secondaryProgress);
        layerDrawable.setId(2, android.R.id.progress);
        horizontalProgressBar.setProgressDrawable(layerDrawable);

        ViewGroup.LayoutParams layoutParams = horizontalProgressBar.getLayoutParams();
        layoutParams.height = DisplayUtils.dp2px(mContext, mBuilder.horizontalProgressBarHeight);
        horizontalProgressBar.setLayoutParams(layoutParams);


    }

    public void showProgress(int progress, String message) {
        showProgress(progress, 0, message, true);
    }

    public void showProgress(int progress, String message, boolean animate) {
        showProgress(progress, 0, message, animate);
    }

    public void showProgress(final int progress, final int secondProgress, String message) {
        showProgress(progress, secondProgress, message, true);
    }

    /**
     * 显示dialog
     *
     * @param progress       当前进度
     * @param secondProgress 二级进度
     * @param message        消息体
     * @param animate        是否平滑过度动画
     */
    public void showProgress(final int progress, final int secondProgress, String message, boolean animate) {
        try {
            if (mDialog == null) {
                return;
            }
            checkDialogConfig();
            if (mBuilder.style == MProgressBarDialogStyle_Horizontal) {
                if (horizontalProgressBar.getVisibility() == View.GONE) {
                    horizontalProgressBar.setVisibility(View.VISIBLE);
                }
                if (!animate) {
                    horizontalProgressBar.setProgress(progress);
                    horizontalProgressBar.setSecondaryProgress(secondProgress);
                } else {
                    //动画形式：一级进度
                    ValueAnimator progressAnim = ValueAnimator.ofInt(horizontalProgressBar.getProgress(), progress);
                    progressAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                    progressAnim.setDuration(mDuration);
                    progressAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int progressCurrent = (int) valueAnimator.getAnimatedValue();
                            if (horizontalProgressBar!=null){
                                horizontalProgressBar.setProgress(progressCurrent);
                            }
                        }
                    });
                    progressAnim.start();
                    //动画形式：二级进度
                    ValueAnimator progressSecondAnim = ValueAnimator.ofInt(horizontalProgressBar.getSecondaryProgress(), secondProgress);
                    progressSecondAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                    progressSecondAnim.setDuration(mDuration);
                    progressSecondAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int progressCurrent = (int) valueAnimator.getAnimatedValue();
                            if (horizontalProgressBar!=null){
                                horizontalProgressBar.setSecondaryProgress(progressCurrent);
                            }
                        }
                    });
                    progressSecondAnim.start();
                }
            }
            tvShow.setText(message);
            mDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean isShowing() {
        if (mDialog != null) {
            return mDialog.isShowing();
        } else {
            return false;
        }
    }

    public void dismiss() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(">>>MProgress>>>", "MProgressBarDialog-dismiss异常:" + e.toString());
        }finally {
            mDialog = null;
            mContext = null;
            mBuilder = null;
            dialog_window_background = null;
            dialog_view_bg = null;
            tvShow = null;
            horizontalProgressBar = null;
        }
    }

    public void refreshBuilder(MProgressBarDialog.Builder builder) {
        mBuilder = builder;
        configView();
    }

    public static final class Builder {

        private Context mContext;

        //窗体背景色
        int backgroundWindowColor;
        //View背景色
        int backgroundViewColor;
        //View边框的颜色
        int strokeColor;
        //View背景圆角
        float cornerRadius;
        //View边框的宽度
        float strokeWidth;
        //文字的颜色
        int textColor;
        //Progressbar 背景色
        int progressbarBackgroundColor;
        //Progressbar 条颜色
        int progressColor;
        //水平进度条Progress圆角
        float progressCornerRadius;
        //style:0:水平，1:圆形
        int style;
        // CircleProgressbar宽度
        int circleProgressBarWidth;
        int circleProgressBarBackgroundWidth;
        // horizontalProgressBar 宽度
        int horizontalProgressBarHeight;
        //Dialog进出动画
        int animationID;

        public Builder(Context context) {
            mContext = context;
            //默认配置

            cornerRadius = 6;
            strokeWidth = 0;
            progressCornerRadius = 2;
            style = MProgressBarDialogStyle_Horizontal;
            circleProgressBarWidth = 3;
            circleProgressBarBackgroundWidth = 1;
            horizontalProgressBarHeight = 4;
            animationID = 0;
        }

        public MProgressBarDialog build() {
            return new MProgressBarDialog(mContext, this);
        }


        public Builder setBackgroundWindowColor(@Nullable int backgroundWindowColor) {
            this.backgroundWindowColor = backgroundWindowColor;
            return this;
        }

        public Builder setBackgroundViewColor(@Nullable int backgroundViewColor) {
            this.backgroundViewColor = backgroundViewColor;
            return this;
        }

        public Builder setStrokeColor(@Nullable int strokeColor) {
            this.strokeColor = strokeColor;
            return this;
        }

        public Builder setStrokeWidth(@Nullable float strokeWidth) {
            this.strokeWidth = strokeWidth;
            return this;
        }

        public Builder setCornerRadius(@Nullable float cornerRadius) {
            this.cornerRadius = cornerRadius;
            return this;
        }

        public Builder setTextColor(@Nullable int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder setProgressbarBackgroundColor(@Nullable int progressbarBackgroundColor) {
            this.progressbarBackgroundColor = progressbarBackgroundColor;
            return this;
        }

        public Builder setProgressColor(@Nullable int progressColor) {
            this.progressColor = progressColor;
            return this;
        }

        public Builder setProgressCornerRadius(@Nullable int progressCornerRadius) {
            this.progressCornerRadius = progressCornerRadius;
            return this;
        }

        public Builder setStyle(@Nullable int style) {
            this.style = style;
            return this;
        }

        public Builder setCircleProgressBarWidth(@Nullable int circleProgressBarWidth) {
            this.circleProgressBarWidth = circleProgressBarWidth;
            return this;
        }

        public Builder setCircleProgressBarBackgroundWidth(@Nullable int circleProgressBarBackgroundWidth) {
            this.circleProgressBarBackgroundWidth = circleProgressBarBackgroundWidth;
            return this;
        }

        public Builder setHorizontalProgressBarHeight(@Nullable int horizontalProgressBarHeight) {
            this.horizontalProgressBarHeight = horizontalProgressBarHeight;
            return this;
        }

        public Builder setAnimationID(@StyleRes int resId) {
            this.animationID = resId;
            return this;
        }
    }

}
