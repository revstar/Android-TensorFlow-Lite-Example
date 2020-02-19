package com.amitshekhar.tflite.activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Create on 2020-02-19 15:51
 * author revstar
 * Email 1967919189@qq.com
 */
public class Test_4 {
    /**
     * 多线程处理list
     *
     * @param data      数据list
     * @param threadNum 线程数
     */
    public synchronized void handleList(List<String> data, int threadNum) {
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
        private List<String> data;
        private int start;
        private int end;

        public HandleThread(String threadName, List<String> data, int start, int end) {
            this.threadName = threadName;
            this.data = data;
            this.start = start;
            this.end = end;
        }

        public void run() {
            //这里处理数据
            List<String> subList = data.subList(start, end);
            for (int a = 0; a < subList.size(); a++) {

                System.out.println(threadName + "处理了   " + subList.get(a) + "  ！");
                //	System.out.println(threadName+"处理了"+subList.size()+"条！");
            }
        }

    }

    public static void main(String[] args) {
        Test_4 test = new Test_4();
        // 准备数据
        List<String> data = new ArrayList<String>();
        for (int i = 0; i < 1517; i++) {
            data.add("item" + i);
        }
        test.handleList(data, 6);
        System.out.println(data.toString());
    }
}
