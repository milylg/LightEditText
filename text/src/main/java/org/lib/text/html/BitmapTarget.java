package org.lib.text.html;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class BitmapTarget extends CustomTarget<Bitmap> {

    private final BitmapDrawable target;

    public BitmapTarget(BitmapDrawable target) {
        this.target = target;
    }

    public BitmapTarget(int width, int height, BitmapDrawable target) {
        super(width, height);
        this.target = target;
    }

    @Override
    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

        Drawable drawable = new BitmapDrawable(resource);
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        target.setBounds(0, 0, width, height);
    }

//        public Bitmap toBitmap(Drawable drawable) {
//
//        // 获取 drawable 长宽
//        int width = drawable.getIntrinsicWidth();
//        int height = drawable.getIntrinsicHeight();
//
//        drawable.setBounds(0, 0, width, height);
//
//        // 获取drawable的颜色格式
//        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
//                : Bitmap.Config.RGB_565;
//        // 创建bitmap
//        Bitmap bitmap = Bitmap.createBitmap(width, heigh, config);
//        // 创建bitmap画布
//        Canvas canvas = new Canvas(bitmap);
//        // 将drawable 内容画到画布中
//        drawable.draw(canvas);
//        return bitmap;
//    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {

    }
}
