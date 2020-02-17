package com.amitshekhar.tflite.model;

import java.util.List;

/**
 * Create on 2020-02-04 17:49
 * author revstar
 * Email 1967919189@qq.com
 */
public class TypePictureBean {

    private String type;
    private List<String> picturePaths;

    public TypePictureBean(String type, List<String> picturePaths) {
        this.type = type;
        this.picturePaths = picturePaths;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getPicturePaths() {
        return picturePaths;
    }

    public void setPicturePaths(List<String> picturePaths) {
        this.picturePaths = picturePaths;
    }
}
