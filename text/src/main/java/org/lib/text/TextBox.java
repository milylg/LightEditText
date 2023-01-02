package org.lib.text;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

import org.lib.text.html.HtmlHandler;
import org.lib.text.html.LocalImageGetter;

@SuppressLint("AppCompatCustomView")
public class TextBox extends TextView {

    public TextBox(Context context) {
        super(context);
    }

    public TextBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTextAttrs(context, attrs, defStyleAttr);
    }

    private void initTextAttrs(Context context, AttributeSet attrs, int defStyle) {
        @SuppressLint("CustomViewStyleable")
        TypedArray typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.effect_text, defStyle, 0);
        String htmlText = typedArray.getString(R.styleable.effect_text_input);
        input(htmlText);
        typedArray.recycle();
    }

    public void input(String html) {
        if (html != null) {
            LocalImageGetter imageGetter = new LocalImageGetter(this);
            setText(HtmlHandler.fromHtml(html, imageGetter));
        }
    }
}
