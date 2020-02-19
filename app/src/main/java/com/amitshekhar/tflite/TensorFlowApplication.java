package com.amitshekhar.tflite;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.interfaces.BetaPatchListener;
import com.tencent.bugly.beta.ui.UILifecycleListener;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Create on 2020-02-19 12:06
 * author revstar
 * Email 1967919189@qq.com
 */
public class TensorFlowApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initBugly();

    }

    /**
     * 第三个参数测试设置true，发布false
     */
    private void initBugly() {
        //设置是否开启热更新，默认true
        Beta.enableHotfix=true;
        // 设置是否自动下载补丁，默认为true
        Beta.canAutoDownloadPatch = true;
        // 设置是否自动合成补丁，默认为true
        Beta.canAutoPatch = true;
        // 设置是否提示用户重启，默认为false
        Beta.canNotifyUserRestart = false;
        //获取包名
        String packageName=getApplicationContext().getPackageName();
        //获取当前进程名
        String processName=getProcessName(android.os.Process.myPid());
        //设置是否为上报进程
        CrashReport.UserStrategy strategy=new CrashReport.UserStrategy(getApplicationContext());
        strategy.setUploadProcess(processName==null||processName.equals(packageName));
        Bugly.init(getApplicationContext(), "310227e1bc", BuildConfig.DEBUG, strategy);


    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

}
