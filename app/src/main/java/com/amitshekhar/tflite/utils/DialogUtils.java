package com.amitshekhar.tflite.utils;



import androidx.fragment.app.FragmentActivity;

import com.revstar.dialog.ToastDialog;
import com.revstar.dialog.WaitDialog;
import com.revstar.dialog.base.BaseDialog;


public class DialogUtils {


    public FragmentActivity mActivity;
    private BaseDialog mDialog;


    public DialogUtils(FragmentActivity activity) {
        this.mActivity = activity;
    }


    /**
     * 显示对话框
     * @param type
     * @param isCancel
     */
    public void showDialog(Type type, String content, boolean isCancel) {
        if (mDialog!=null){
            mDialog.dismiss();
        }
        switch (type) {
            case FINISH:
                mDialog = new ToastDialog.Builder(mActivity)
                        .setType(ToastDialog.Type.FINISH)
                        .setMessage(content)
                        .setCancelable(isCancel)
                        .show();
                break;
            case WARN:
                mDialog = new ToastDialog.Builder(mActivity)
                        .setType(ToastDialog.Type.WARN)
                        .setMessage(content)
                        .setCancelable(isCancel)
                        .show();
                break;
            case ERROR:
                mDialog = new ToastDialog.Builder(mActivity)
                        .setType(ToastDialog.Type.ERROR)
                        .setMessage(content)
                        .setCancelable(isCancel)
                        .show();
                break;
            case LOADING:
                mDialog = new WaitDialog.Builder(mActivity)
                        // 消息文本可以不用填写
                        .setMessage(content)
                        .setCancelable(isCancel)
                        .show();
                break;
            default:
                break;
        }
    }

    /**
     * 显示完成对话框
     */
    public void showFinishDialog(String content){
        showDialog(Type.FINISH,content,true);
    }

    /**
     * 显示错误对话框
     * @param content
     */
    public void showErrorDialog(String content){
        showDialog(Type.ERROR,content,true);
    }
    /**
     * 显示警告对话框
     * @param content
     */
    public void showWarnDialog(String content){
        showDialog(Type.WARN,content,true);
    }

    /**
     * 显示加载对话框
     * @param content
     * @param isCancel
     */
    public void showLoadingDialog(String content,boolean isCancel){
        showDialog(Type.LOADING,content,isCancel);
    }


    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }

    }

    /**
     * 显示的类型
     */
    public enum Type {
        // 完成，错误，警告
        FINISH, ERROR, WARN, LOADING
    }


}
