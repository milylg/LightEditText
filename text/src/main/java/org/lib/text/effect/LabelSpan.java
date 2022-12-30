package org.lib.text.effect;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LabelSpan extends ReplacementSpan {

    private int horizonPadding;
    private int spanWidth;
    private final int bgColor;
    private final int textColor;
    private final float radius;

    public LabelSpan() {
        this.bgColor = Color.parseColor("#FFF0E0");
        this.textColor = Color.parseColor("#F0A060");
        this.radius = 6;
        this.horizonPadding = 4;
    }

    public LabelSpan(int bgColor, int textColor, float radius) {
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.radius = radius;
    }

    @Override
    public int getSize(@NonNull Paint paint,
                       CharSequence text, int start, int end,
                       @Nullable Paint.FontMetricsInt fm) {
        spanWidth = (int) (paint.measureText(text,start,end) + 2 * radius) + 8; // # is 8.
        return spanWidth + horizonPadding * 2 + 2; // right margin is 2 for cursor.
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text,
                     int start, int end, float x, int top, int y, int bottom,
                     @NonNull Paint paint) {

        drawTagRect(canvas, x, y, paint);
        drawTagText(canvas, text, start, end, x, y, paint);
   }

    private void drawTagRect(Canvas canvas, float x, int y, Paint paint) {
        int color = paint.getColor();
        paint.setColor(bgColor);
        paint.setAntiAlias(true);
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        final float strokeWidth = paint.getStrokeWidth();
        RectF oval = new RectF(x + strokeWidth + 2, y + fontMetrics.ascent,
                x + spanWidth + strokeWidth + horizonPadding * 2, y + fontMetrics.descent);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRoundRect(oval, radius, radius, paint);
        paint.setColor(color);
    }
    private void drawTagText(Canvas canvas, CharSequence text,
                             int start, int end,float x, int y, Paint paint) {
        int color = paint.getColor();
        paint.setColor(textColor);
        // TODO: 除了'#'，应该可扩展其他符号，可以通过Unicode符号表达，例如，警告，正确，错误等emoji图标符号。
        final String tag = "#" + text.subSequence(start, end).toString();
        canvas.drawText(tag, x + horizonPadding * 2,  y, paint);
        paint.setColor(color);
    }
}
