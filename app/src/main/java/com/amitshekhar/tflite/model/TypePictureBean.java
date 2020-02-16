package com.amitshekhar.tflite.model;

/**
 * Create on 2020-02-04 17:49
 * author revstar
 * Email 1967919189@qq.com
 */
public class TypePictureBean {

    private String type;
    private String picturePath;

    public TypePictureBean(String type, String picturePath) {
        this.type = type;
        this.picturePath = picturePath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

}
