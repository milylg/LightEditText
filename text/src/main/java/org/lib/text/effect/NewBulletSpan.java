package org.lib.text.effect;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineHeightSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NewBulletSpan implements LeadingMarginSpan {

    // Bullet is slightly bigger to avoid aliasing artifacts on mdpi devices.
    private static final int STANDARD_BULLET_RADIUS = 4;
    public static final int STANDARD_GAP_WIDTH = 5;
    private static final int STANDARD_COLOR = 0;


    private final int mGapWidth;
    private final int mBulletRadius;
    private final int mColor;
    private final boolean mWantColor;

    /**
     * Creates a {@link NewBulletSpan} with the default values.
     */
    public NewBulletSpan() {
        this(STANDARD_GAP_WIDTH, STANDARD_COLOR, false, STANDARD_BULLET_RADIUS);
    }

    /**
     * Creates a {@link NewBulletSpan} based on a gap width
     *
     * @param gapWidth the distance, in pixels, between the bullet point and the paragraph.
     */
    public NewBulletSpan(int gapWidth) {
        this(gapWidth, STANDARD_COLOR, false, STANDARD_BULLET_RADIUS);
    }

    /**
     * Creates a {@link NewBulletSpan} based on a gap width and a color integer.
     *
     * @param gapWidth the distance, in pixels, between the bullet point and the paragraph.
     * @param color    the bullet point color, as a color integer
     */
    public NewBulletSpan(int gapWidth, @ColorInt int color) {
        this(gapWidth, color, true, STANDARD_BULLET_RADIUS);
    }

    /**
     * Creates a {@link BulletSpan} based on a gap width and a color integer.
     *
     * @param gapWidth     the distance, in pixels, between the bullet point and the paragraph.
     * @param color        the bullet point color, as a color integer.
     * @param bulletRadius the radius of the bullet point, in pixels.
     */
    public NewBulletSpan(int gapWidth, @ColorInt int color, @IntRange(from = 0) int bulletRadius) {
        this(gapWidth, color, true, bulletRadius);
    }

    private NewBulletSpan(int gapWidth, @ColorInt int color, boolean wantColor,
                       @IntRange(from = 0) int bulletRadius) {
        mGapWidth = gapWidth;
        mBulletRadius = bulletRadius;
        mColor = color;
        mWantColor = wantColor;
    }

    // TODO:set value in configuration.
    protected static final int LEADING_MARGIN = 20;

    @Override
    public int getLeadingMargin(boolean first) {
        return LEADING_MARGIN;
    }

    /**
     * Get the distance, in pixels, between the bullet point and the paragraph.
     *
     * @return the distance, in pixels, between the bullet point and the paragraph.
     */
    public int getGapWidth() {
        return mGapWidth;
    }

    /**
     * Get the radius, in pixels, of the bullet point.
     *
     * @return the radius, in pixels, of the bullet point.
     */
    public int getBulletRadius() {
        return mBulletRadius;
    }

    /**
     * Get the bullet point color.
     *
     * @return the bullet point color
     */
    public int getColor() {
        return mColor;
    }


    /**
     * Renders the leading margin.  This is called before the margin has been
     * adjusted by the value returned by {@link #getLeadingMargin(boolean)}.
     *
     * @param canvas the canvas
     * @param paint the paint. The this should be left unchanged on exit.
     * @param x the current position of the margin
     * @param dir the base direction of the paragraph; if negative, the margin
     * is to the right of the text, otherwise it is to the left.
     * @param top the top of the line
     * @param baseline the baseline of the line
     * @param bottom the bottom of the line
     * @param text the text
     * @param start the start of the line
     * @param end the end of the line
     * @param first true if this is the first line of its paragraph
     * @param layout the layout containing this line
     */
    @Override
    public void drawLeadingMargin(@NonNull Canvas canvas, @NonNull Paint paint, int x, int dir,
                                  int top, int baseline, int bottom,
                                  @NonNull CharSequence text, int start, int end,
                                  boolean first, @Nullable Layout layout) {

        if (((Spanned) text).getSpanStart(this) == start) {

            Paint.Style style = paint.getStyle();
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText("\u2022", x + STANDARD_GAP_WIDTH, baseline, paint);
            paint.setStyle(style);
        }
    }
}
