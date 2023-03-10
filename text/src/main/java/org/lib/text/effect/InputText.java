/*
 Copyright (c) 2011-2014 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.lib.text.effect;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatEditText;

import com.bumptech.glide.Glide;

import org.lib.text.R;
import org.lib.text.arch.ActionModeListener;
import org.lib.text.arch.Effect;
import org.lib.text.arch.ToggleEffect;
import org.lib.text.html.BitmapTarget;
import org.lib.text.html.HtmlHandler;
import org.lib.text.html.LocalImageGetter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Custom widget that simplifies adding rich text editing
 * capabilities to Android activities. Serves as a drop-in
 * replacement for EditText. Full documentation can be found
 * on project Web site
 * (http://github.com/commonsguy/cwac-richedit). Concepts in
 * this editor were inspired by:
 * http://code.google.com/p/droid-writer
 *
 * ## How to use it?
 *
 * 1. Initialized 'InputText' Component.
 * 2. Set 'ImageDrawableGetter' interface for 'InputText' object.
 * 3. Handle 'Pick picture' or 'Take photo' to get bitmap of image.
 *    - Create file for copy image.
 *    - Copy image by uri, and scale image bitmap.
 *    - Handle image bitmap by 'ImagePostProcessor' object.
 */
public class InputText extends AppCompatEditText implements ActionModeListener {

    private static final String TAG = "InputText";

    private static final String LINK_PATTERN
            = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=-~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
    public final Effect<Boolean> BOLD = new StyleEffect(Typeface.BOLD);
    public final Effect<Boolean> BULLET = new BulletEffect();
    public final Effect<Boolean> STRIKE_LINE = new ToggleEffect<>(StrikeLineSpan.class);
    public final Effect<Boolean> MARK = new ToggleEffect<>(LabelSpan.class);
    public final Effect<String> URL = new URLEffect();
    public final Effect<ImageSpan> IMAGE = new ImageEffect();

    private float spacingMulti = 1f;
    private float spacingAdd = 0f;
    private int cursorColor = Color.RED;
    private int cursorWidth;
    private int cursorHeight = 25;

    private ClipboardManager clipboardManager;
    private ImageDrawableGetter imageDrawableGetter;

    @FunctionalInterface
    public interface ImageDrawableGetter {
        void getImage(GetImagePostProcessor postProcessor);
    }

    public interface GetImagePostProcessor {
        void handleImage(Bitmap bitmap, String path);
    }

    /*
     * Standard one-parameter widget constructor, simply
     * chaining to superclass.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public InputText(Context context) {
        this(context, null);
    }

    /*
     * Standard two-parameter widget constructor, simply
     * chaining to superclass.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public InputText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    /*
     * Standard three-parameter widget constructor, simply
     * chaining to superclass.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public InputText(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        initTextAttrs(context,attrs,defStyle);
        initActionMode();
        // Fix edittext lineHeight and cursor effect
        // when set lineSpacingExtra or lineSpacingMultiplier
        initLineSpacingAddAndLineSpacingMulti();
        initCursorParameter();
        initTextCursorDrawable();
        listenTextChange();
    }

    private void initTextAttrs(Context context, AttributeSet attrs, int defStyle) {
        @SuppressLint("CustomViewStyleable")
        TypedArray typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.effect_text, defStyle, 0);
        String htmlText = typedArray.getString(R.styleable.effect_text_input);
        input(htmlText);
        typedArray.recycle();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initActionMode() {

        ActionModeCallback.Native effectAction = new ActionModeCallback.Native(
                R.menu.menu_effects,
                this,
                this
        );
        setCustomSelectionActionModeCallback(effectAction);
        setCustomInsertionActionModeCallback(effectAction);

        clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
    }

    private void initCursorParameter() {
        // Can't use theme() to get colorAccent.
        cursorColor = Color.parseColor("#B5BFF4");
        cursorHeight = (int) (1.25 * getTextSize());
        cursorWidth = 3;
    }

    private void initTextCursorDrawable() {
        try {
            @SuppressLint("DiscouragedPrivateApi")
            Method method = TextView.class.getDeclaredMethod("createEditorIfNeeded");
            method.setAccessible(true);
            method.invoke(this);
            Field field1 = TextView.class.getDeclaredField("mEditor");
            @SuppressLint("PrivateApi")
            Field field2 = Class.forName("android.widget.Editor").getDeclaredField("mCursorDrawable");
            field1.setAccessible(true);
            field2.setAccessible(true);
            Object arr = field2.get(field1.get(this));
            Array.set(arr, 0, new LineCursorDrawable(cursorColor, cursorWidth, cursorHeight));
            Array.set(arr, 1, new LineCursorDrawable(cursorColor, cursorWidth, cursorHeight));
        } catch (Exception exception) {
            Log.i(TAG, "InitTextCursorDrawable: " + exception.getMessage());
        }
    }

    private void initLineSpacingAddAndLineSpacingMulti() {
        try {
            Field mSpacingAddField = TextView.class.getDeclaredField("mSpacingAdd");
            Field mSpacingMultiField = TextView.class.getDeclaredField("mSpacingMult");
            mSpacingAddField.setAccessible(true);
            mSpacingMultiField.setAccessible(true);
            spacingAdd = mSpacingAddField.getFloat(this);
            spacingMulti = mSpacingMultiField.getFloat(this);
        } catch (Exception e) {
            Log.i(TAG, "InitLineSpacingAddAndLineSpacingMulti: " + e.getMessage());
        }
    }

    private void listenTextChange() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            /**
             * NOTE: EditText.onTextChanged is not same with TextWatcher.onTextChanged method.
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setLineSpacing(0f, 1f);
                setLineSpacing(spacingAdd, spacingMulti);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    /**
     * You shouldn't use LinkMovementMethod with editable text.
     * LinkMovementMethod is a movement method that lets you move around between links...
     * which is not what you want for a text editor,
     * since you should be using the movement method that moves the cursor around for editing.
     * <p>
     * What do you expect to do with links in editable text?
     * When you have editable text, tapping on the text puts the cursor at that point to edit it.
     * It doesn't open the link. These are two conflicting things.
     * If you want to have some other behavior,
     * you will need to customize the text view to do something special and figure out
     * how you are going to disambiguate between editing the link text
     * and opening it (for example Google Docs shows you a pop-up to select to open it
     * if you don't want to edit it).
     * But you will need to code this yourself,
     * there is no magic built-in thing that implements this in the current framework.
     *
     * @param event user touch event.
     * @return event handle chain continue flag.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= getTotalPaddingLeft();
            y -= getTotalPaddingTop();
            x += getScrollX();
            y += getScrollY();

            Layout layout = getLayout();

            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = getText().getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(this);
                    Selection.removeSelection(getText());
                } else {
                    Selection.setSelection(getText(),
                            getText().getSpanStart(link[0]), getText().getSpanEnd(link[0]));
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /*
     * If there is a registered OnSelectionChangedListener,
     * checks to see if there are any effects applied to the
     * current selection, and supplies that information to the
     * registrant.
     *
     * Uses isSelectionChanging to avoid updating anything
     * while this callback is in progress (e.g., registrant
     * updates a ToggleButton, causing its
     * OnCheckedChangeListener to fire, causing it to try to
     * update the RichEditText as if the user had clicked upon
     * it.
     *
     * @see android.widget.TextView#onSelectionChanged(int,int)
     */
    @Override
    public void onSelectionChanged(int start, int end) {
        super.onSelectionChanged(start, end);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER ||
                keyCode == KeyEvent.KEYCODE_DEL ||
                keyCode == KeyEvent.KEYCODE_FORWARD_DEL) {
            updateLineGroups();
        }

        return super.onKeyUp(keyCode, event);
    }

    /*
     * Call this to have an effect applied to the current
     * selection. You get the Effect object via the static
     * data members (e.g., RichEditText.BOLD). The value for
     * most effects is a Boolean, indicating whether to add or
     * remove the effect.
     */
    public <T> void applyEffect(Effect<T> effect, T value) {
        effect.applyToSelection(this, value);
    }

    /*
     * Returns true if a given effect is applied somewhere in
     * the current selection. This includes the effect being
     * applied in a subset of the current selection.
     */
    public boolean hasEffect(Effect<?> effect) {
        return (effect.existsInSelection(this));
    }

    /*
     * Returns the value of the effect applied to the current
     * selection. For Effect<Boolean> (e.g.,
     * RichEditText.BOLD), returns the same value as
     * hasEffect(). Otherwise, returns the highest possible
     * value, if multiple occurrences of this effect are
     * applied to the current selection. Returns null if there
     * is no such effect applied.
     */
    public <T> T getEffectValue(Effect<T> effect) {
        return (effect.valueInSelection(this));
    }

    /*
     * If the effect is presently applied to the current
     * selection, removes it; if the effect is not presently
     * applied to the current selection, adds it.
     */
    public void toggleEffect(Effect<Boolean> effect) {
        effect.applyToSelection(this, !effect.valueInSelection(this));
        this.setSelection(getSelectionEnd());
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean result = super.onTextContextMenuItem(id);

        if (id == android.R.id.cut || id == android.R.id.paste) {
            updateLineGroups();
        }

        return (result);
    }

    /*
     * Context menu, and long-press select text popup menu,
     * different from the style of a single, list form.
     * @param menu
     */
    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        super.onCreateContextMenu(menu);
    }

    /*
     * @return true the edit area is editable status. else not editable.
     */
    @Override
    protected boolean getDefaultEditable() {
        return true;
    }

    @Override
    public boolean doAction(int itemId) {
        if (itemId == R.id.text_bold) {
            toggleEffect(BOLD);
        } else if (itemId == R.id.text_bullet) {
            toggleEffect(BULLET);
        } else if (itemId == R.id.text_link) {
            affixLink();
        } else if (itemId == R.id.text_tag) {
            toggleEffect(MARK);
        } else if (itemId == R.id.text_strike_line) {
            toggleEffect(STRIKE_LINE);
        } else if (itemId == R.id.text_image) {
            applyImage();
        } else {
            return false;
        }
        return true;
    }

    // TODO: Redesign and refactor.
    public void affixLink() {
        int selStart = getSelectionStart();
        int selEnd = getSelectionEnd();
        setSelection(selStart, selEnd);
        String link = getClipText();
        if (verifyLink(link)) {
            applyEffect(URL, link);
        } else {
            setError("This is not a link address.");
        }
    }


    private String getClipText() {
        ClipData clipDatas = clipboardManager.getPrimaryClip();
        if (clipDatas == null) {
            return "";
        }

        int itemCount = clipDatas.getItemCount();
        if (itemCount > 0) {
            ClipData.Item leastTextCoped = clipDatas.getItemAt(itemCount - 1);
            return leastTextCoped.getText().toString();
        }
        return "";
    }

    private boolean verifyLink(String url) {
        if ("".equals(url)) {
            return false;
        }
        return url.matches(LINK_PATTERN);
    }

    private void applyImage() {
        if (imageDrawableGetter != null) {
            imageDrawableGetter.getImage((bitmap, path) -> {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                Matrix matrix = new Matrix();
                float scale = ((float)getWidth() / width);
                matrix.postScale(scale, scale);

                Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                BitmapDrawable drawable = new BitmapDrawable(getResources(), newBitmap);
                drawable.setBounds(0, 0, newBitmap.getWidth(), newBitmap.getHeight());
                applyEffect(IMAGE, new ImageSpan(drawable, path));
            });
        }
    }

    public void input(String html) {
        if (html != null) {
            setText(HtmlHandler.fromHtml(html, new LocalImageGetter(getContext())));
        }
    }

    public String output() {
        return HtmlHandler.toHtml(getEditableText());
    }

    private void updateLineGroups() {
        ((BulletEffect) BULLET).updateBullets(this);
    }

    public void setImageDrawableGetter(ImageDrawableGetter imageGetter) {
        this.imageDrawableGetter = imageGetter;
    }
}
