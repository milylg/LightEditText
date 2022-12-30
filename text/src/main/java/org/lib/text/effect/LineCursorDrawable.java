package org.lib.text.effect;

import android.graphics.drawable.ShapeDrawable;

public class LineCursorDrawable extends ShapeDrawable {

    private final int mHeight;

    public LineCursorDrawable(int cursorColor, int cursorWidth, int cursorHeight) {
        mHeight = cursorHeight;
        setDither(false);
        getPaint().setColor(cursorColor);
        setIntrinsicWidth(cursorWidth);
    }

    public void setBounds(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
        super.setBounds(paramInt1, paramInt2, paramInt3, this.mHeight + paramInt2);
    }

}