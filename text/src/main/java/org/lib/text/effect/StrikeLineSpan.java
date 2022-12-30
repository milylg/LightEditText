package org.lib.text.effect;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.StrikethroughSpan;

import androidx.annotation.NonNull;

public class StrikeLineSpan extends StrikethroughSpan {

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(Color.argb(70, 50, 80, 80));
    }
}
