package org.lib.text.html;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;

import com.bumptech.glide.Glide;

public class LocalImageGetter implements Html.ImageGetter {

    private final Context context;

    public LocalImageGetter(Context context) {
        this.context = context;
    }

    /**
     * @param source image path.
     * @return drawable of image.
     */
    @Override
    public Drawable getDrawable(String source) {
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), source);
        Glide.with(context)
                .asBitmap()
                .load(source)
                .into(new BitmapTarget(drawable));
        return drawable;
    }
}