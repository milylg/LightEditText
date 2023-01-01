package org.lib.text.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;

public class LineImageSpan extends ReplacementSpan {

    private Drawable drawable;
    private String source;
    private int width;
    private int height;

    public LineImageSpan(@NonNull Context context, @NonNull Bitmap bitmap, String path, int screenWidth) {
        width = bitmap.getWidth();
        height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        float scale = ((float)screenWidth) / width;
        matrix.postScale(scale, scale);

        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = newBitmap.getWidth();
        height = newBitmap.getHeight();
        drawable = new BitmapDrawable(context.getResources(), newBitmap);
        drawable.setBounds(0, 0, width, height);
        source = path;
    }

    public LineImageSpan(Drawable drawable, String path) {
        this.drawable = drawable;
        this.source = path;
        Rect bounds = drawable.getBounds();
        this.width = bounds.width();
        this.height = bounds.height();
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end,
                                 Paint.FontMetricsInt fontMetricsInt) {

        Rect rect = drawable.getBounds();
        if (fontMetricsInt != null) {
            Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
            int fontHeight = fmPaint.descent - fmPaint.ascent;
            int drHeight = rect.bottom - rect.top;
            int centerY = fmPaint.ascent + fontHeight / 2;

            fontMetricsInt.ascent = centerY - drHeight / 2;
            fontMetricsInt.top = fontMetricsInt.ascent;
            fontMetricsInt.bottom = centerY + drHeight / 2;
            fontMetricsInt.descent = fontMetricsInt.bottom;
        }
        return rect.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y,
                     int bottom, Paint paint) {
        canvas.save();
        Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
        int fontHeight = fmPaint.descent - fmPaint.ascent;
        int centerY = y + fmPaint.descent - fontHeight / 2;
        int transY = centerY - (drawable.getBounds().bottom - drawable.getBounds().top) / 2;
        canvas.translate(x, transY);
        drawable.draw(canvas);
        canvas.restore();
    }

    public String source() {
        return source;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}