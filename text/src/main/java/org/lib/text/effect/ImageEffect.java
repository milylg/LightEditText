package org.lib.text.effect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import org.lib.text.arch.Effect;
import org.lib.text.arch.Selection;

public class ImageEffect extends Effect<ImageSpan> {

    @Override
    public boolean existsInSelection(InputText editor) {
        Selection selection = new Selection(editor);
        Spannable str = editor.getText();
        boolean result = false;

        if (selection.getStart() != selection.getEnd()) {
            ImageSpan[] spans = str.getSpans(selection.getStart(), selection.getEnd(), ImageSpan.class);
            result = (spans.length > 0);
        } else {
            ImageSpan[] spansBefore =
                    str.getSpans(selection.getStart() - 1, selection.getEnd(), ImageSpan.class);
            ImageSpan[] spansAfter =
                    str.getSpans(selection.getStart(), selection.getEnd() + 1, ImageSpan.class);

            result = (spansBefore.length > 0 && spansAfter.length > 0);
        }

        return (result);
    }

    @Override
    public ImageSpan valueInSelection(InputText editor) {
        Selection selection = new Selection(editor);
        Spannable str = editor.getText();

        ImageSpan[] spans = str.getSpans(
                selection.getStart(),
                selection.getEnd(),
                ImageSpan.class
        );

        if (spans.length > 0) {
            return spans[0];
        }

        return (null);
    }

    @Override
    public void applyToSelection(InputText editor, ImageSpan value) {
        Selection selection = new Selection(editor);
        Editable str = editor.getText();

        ImageSpan[] spans = str.getSpans(
                selection.getStart(),
                selection.getEnd(),
                ImageSpan.class
        );

        for (ImageSpan span : spans) {
            str.removeSpan(span);
        }

        if (value != null) {
            str.insert(selection.getStart(), "\n");
            SpannableStringBuilder spanBuilder = new SpannableStringBuilder("[IMAGE]");
            spanBuilder.setSpan(value, 0, spanBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.insert(selection.getStart(), spanBuilder);
            editor.setSelection(str.length());
            str.insert(selection.getEnd(), "\n");
        }
    }
}
