package org.lib.text.effect;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Locale;

public class LinkSpan extends URLSpan {
    /**
     * Constructs a {@link URLSpan} from a url string.
     *
     * @param url the url string
     */
    public LinkSpan(String url) {
        super(url);
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        ds.setTextLocale(Locale.CHINESE);
    }

    @Override
    public void onClick(View widget) {
        Uri uri = Uri.parse(getURL());

        // 若uri是网络链接 => 打开浏览器
        Context context = widget.getContext();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w("LinkSpan", "Not found for intent, " + intent.toString());
        }

        // 若uri是自定义的文本链接 => 打开文本内容
    }
}
