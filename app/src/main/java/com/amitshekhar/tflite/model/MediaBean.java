package com.amitshekhar.tflite.model;

/**
 * Create on 2020-02-04 16:48
 * author revstar
 * Email 1967919189@qq.com
 */
public class MediaBean {
    public String localPath;//本地文件路径
    public String thumbPath;//缩略图
    public int duration;//时长
    public long size;//大小，单位kb
    public String displayName;//名称，带后缀

    public MediaBean(String localPath, long size, String displayName) {
        this.localPath = localPath;
        this.size = size;
        this.displayName = displayName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
