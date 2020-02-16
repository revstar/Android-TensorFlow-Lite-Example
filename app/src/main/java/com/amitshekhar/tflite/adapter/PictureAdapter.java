package com.amitshekhar.tflite.adapter;

import android.widget.ImageView;

import com.amitshekhar.tflite.R;
import com.amitshekhar.tflite.model.TypePictureBean;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;


import java.util.List;

/**
 * Create on 2020-02-04 17:18
 * author revstar
 * Email 1967919189@qq.com
 */
public class PictureAdapter extends BaseQuickAdapter<TypePictureBean, BaseViewHolder> {

    public PictureAdapter(int layoutResId, List<TypePictureBean> data) {
        super(layoutResId, data);
    }


    @Override
    protected void convert(BaseViewHolder holder, TypePictureBean typePictureBean) {
        if (typePictureBean!=null){
            holder.setText(R.id.tv_type,typePictureBean.getType());
            Glide.with(getContext()).load(typePictureBean.getPicturePath()).into((ImageView) holder.getView(R.id.ig_picture));
        }
    }
}
