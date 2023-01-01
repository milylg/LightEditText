package org.lib.text.html;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.Html;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;


/**
 * use [new BitmapDrawable(context.getResources(), source);] will cause problem
 * INFO: Displayed org.lib.text/org.demo.text.RenderActivity: +198ms
 *       Displayed org.lib.text/org.demo.text.MainActivity: +144ms
 * RESOLVE: use Glide async load image bitmap.
 *
 * Glide 缓存机制: 内存 -> 磁盘 -> 网络
 * 二级缓存（内存缓存，硬盘缓存）
 * - 内存缓存又分为2种，弱引用（正在使用的资源）和LruCache。主要作用是防止应用重复将图片数据读取到内存当中。
 * - 磁盘缓存（DiskLruCache），DiskLruCache算法和LruCache差不多。主要作用是防止应用重复从网络或其他地方下载和读取数据。
 *
 * 使用内存缓存可以获得更快的图片加载速度，因为减少了耗时的IO操作。
 * Bitmap是Android中的内存大户，频繁的创建和回收Bitmap必然会引起内存抖动。
 * Glide中有一个叫做 BitmapPool的类，可以复用其中的Bitmap对象，从而避免Bitmap对象的创建，减小内存开销。
 *
 * 配置内存缓存时，应该同时配置BitmapPool的大小,具体方法是通过自定义的GlideModule来实现。
 */
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
        final LevelListDrawable levelListDrawable = new LevelListDrawable();
        Glide.with(context)
                .asBitmap()
                .load(source)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new BitmapTarget(levelListDrawable));
        return levelListDrawable;
    }
}