package org.lib.text.html;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class BitmapTarget extends CustomTarget<Bitmap> {

    private final LevelListDrawable levelListDrawable;

    /**
     * Creates a new {@link CustomTarget} that will attempt to load the resource in its original size.
     *
     * <p>This constructor can cause very memory inefficient loads if the resource is large and can
     * cause OOMs. It's provided as a convenience for when you'd like to specify dimensions with
     * {@link RequestOptions#override(int)}.
     */
    public BitmapTarget(LevelListDrawable levelListDrawable) {
        this.levelListDrawable = levelListDrawable;
    }

    /**
     * The method that will be called when the resource load has finished.
     *
     * @param resource   the loaded resource.
     * @param transition
     */
    @Override
    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

        BitmapDrawable drawable = new BitmapDrawable(resource);
        levelListDrawable.addLevel(1,1, drawable);
        levelListDrawable.setLevel(1);
    }

    /**
     * A <b>mandatory</b> lifecycle callback that is called when a load is cancelled and its resources
     * are freed.
     *
     * <p>You <b>must</b> ensure that any current Drawable received in onResourceReady(Object,
     * Transition) is no longer used before redrawing the container (usually a View) or changing its
     * visibility.
     *
     * @param placeholder The placeholder drawable to optionally show, or null.
     */
    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {

    }
}
