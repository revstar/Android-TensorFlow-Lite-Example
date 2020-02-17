package com.amitshekhar.tflite.adapter;

import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

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
public class TypeAdapter extends BaseQuickAdapter<String, BaseViewHolder> {


    public TypeAdapter(int layoutResId, List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, String s) {

        Glide.with(getContext())
                .load(s)
                .fallback(R.color.picture_color_e)
                .placeholder(R.color.picture_color_e)
                .error(R.color.picture_color_e)
                .into((ImageView) holder.getView(R.id.ig_type));

    }
}
