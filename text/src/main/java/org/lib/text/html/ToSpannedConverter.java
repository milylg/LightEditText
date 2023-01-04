package org.lib.text.html;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ParagraphStyle;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import org.ccil.cowan.tagsoup.Parser;
import org.lib.text.effect.LabelSpan;
import org.lib.text.effect.LinkSpan;
import org.lib.text.effect.NewBulletSpan;
import org.lib.text.effect.StrikeLineSpan;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT;

class ToSpannedConverter implements ContentHandler {

    private final String mSource;
    private final XMLReader mReader;
    private final SpannableStringBuilder mSpannableStringBuilder;
    /**
     * FROM_HTML_MODE_COMPACT：html块元素之间使用一个换行符分隔
     * FROM_HTML_MODE_LEGACY：html块元素之间使用两个换行符分隔
     */
    private final int mFlags;

    private static Pattern sForegroundColorPattern;
    private static Pattern sBackgroundColorPattern;
    private static Pattern sTextDecorationPattern;

    /**
     * Name-value mapping of HTML/CSS colors which have different values in {@link Color}.
     */
    private static final Map<String, Integer> sColorMap;

    static {
        sColorMap = new HashMap<>();
        sColorMap.put("darkgray", 0xFFA9A9A9);
        sColorMap.put("gray", 0xFF808080);
        sColorMap.put("lightgray", 0xFFD3D3D3);
        sColorMap.put("darkgrey", 0xFFA9A9A9);
        sColorMap.put("grey", 0xFF808080);
        sColorMap.put("lightgrey", 0xFFD3D3D3);
        sColorMap.put("green", 0xFF008000);
    }

    private static Pattern getForegroundColorPattern() {
        if (sForegroundColorPattern == null) {
            sForegroundColorPattern = Pattern.compile(
                    "(?:\\s+|\\A)color\\s*:\\s*(\\S*)\\b");
        }
        return sForegroundColorPattern;
    }

    private static Pattern getBackgroundColorPattern() {
        if (sBackgroundColorPattern == null) {
            sBackgroundColorPattern = Pattern.compile(
                    "(?:\\s+|\\A)background(?:-color)?\\s*:\\s*(\\S*)\\b");
        }
        return sBackgroundColorPattern;
    }

    private static Pattern getTextDecorationPattern() {
        if (sTextDecorationPattern == null) {
            sTextDecorationPattern = Pattern.compile(
                    "(?:\\s+|\\A)text-decoration\\s*:\\s*(\\S*)\\b");
        }
        return sTextDecorationPattern;
    }

    public ToSpannedConverter(String source, Parser parser) {
        mSource = source;
        mSpannableStringBuilder = new SpannableStringBuilder();
        mReader = parser;
        mFlags = FROM_HTML_MODE_COMPACT;
    }

    public Spanned convert() {

        mReader.setContentHandler(this);
        try {
            mReader.parse(new InputSource(new StringReader(mSource)));
        } catch (IOException | SAXException e) {
            // We are reading from a string. There should not be IO problems.
            // TagSoup doesn't throw parse exceptions.
            throw new RuntimeException(e);
        }


        // Fix flags and range for paragraph-type markup.
        Object[] obj = mSpannableStringBuilder.getSpans(
                0,
                mSpannableStringBuilder.length(),
                ParagraphStyle.class);

        for (int i = 0; i < obj.length; i++) {
            int start = mSpannableStringBuilder.getSpanStart(obj[i]);
            int end = mSpannableStringBuilder.getSpanEnd(obj[i]);

            // If the last line of the range is blank, back off by one.
            if (end - 2 >= 0) {
                if (mSpannableStringBuilder.charAt(end - 1) == '\n' &&
                        mSpannableStringBuilder.charAt(end - 2) == '\n') {
                    end--;
                }
            }

            if (end == start) {
                mSpannableStringBuilder.removeSpan(obj[i]);
            } else {
                mSpannableStringBuilder.setSpan(obj[i], start, end, Spannable.SPAN_PARAGRAPH);
            }
        }

        return mSpannableStringBuilder;
    }

    private void handleStartTag(String tag, Attributes attributes) {
        if (tag.equalsIgnoreCase("br")) {
            // We don't need to handle this. TagSoup will ensure that there's a </br> for each <br>
            // so we can safely emit the linebreaks when we handle the close tag.
        } else if (tag.equalsIgnoreCase("p")) {
            startBlockElement(mSpannableStringBuilder, 1);
            startCssStyle(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("ul")) {
            startBlockElement(mSpannableStringBuilder, 1);
        } else if (tag.equalsIgnoreCase("li")) {
            startLi(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("strong")) {
            start(mSpannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("b")) {
            start(mSpannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("a")) {
            startA(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("u")) {
            start(mSpannableStringBuilder, new Underline());
        } else if (tag.equalsIgnoreCase("cell")) {
            start(mSpannableStringBuilder, new Tag());
        } else if (tag.equalsIgnoreCase("del")) {
            start(mSpannableStringBuilder, new Del());
        }
    }

    private void handleEndTag(String tag) {
        if (tag.equalsIgnoreCase("br")) {
            handleBr(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("p")) {
            endCssStyle(mSpannableStringBuilder);
            endBlockElement(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("ul")) {
            endBlockElement(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("li")) {
            endLi(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("b")) {
            end(mSpannableStringBuilder, Bold.class, new StyleSpan(Typeface.BOLD));
        } else if (tag.equalsIgnoreCase("a")) {
            endA(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("u")) {
            end(mSpannableStringBuilder, Underline.class, new UnderlineSpan());
        } else if (tag.equalsIgnoreCase("cell")) {
            end(mSpannableStringBuilder, Tag.class, new LabelSpan());
        } else if (tag.equalsIgnoreCase("del")) {
            end(mSpannableStringBuilder, Del.class, new StrikeLineSpan());
        }
    }

    private int getMarginListItem() {
        return getMargin(android.text.Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM);
    }

    /**
     * Returns the minimum number of newline characters needed before and after a given block-level
     * element.
     *
     * @param flag the corresponding option flag defined in {@link android.text.Html} of a block-level element
     */
    private int getMargin(int flag) {
        if ((flag & mFlags) != 0) {
            return 1;
        }
        return 2;
    }

    private static void appendNewlines(Editable text, int minNewline) {
        final int len = text.length();

        if (len == 0) {
            return;
        }

        int existingNewlines = 0;
        for (int i = len - 1; i >= 0 && text.charAt(i) == '\n'; i--) {
            existingNewlines++;
        }

        for (int j = existingNewlines; j < minNewline; j++) {
            text.append("\n");
        }
    }

    private static void startBlockElement(Editable text, int margin) {
        if (margin > 0) {
            appendNewlines(text, margin);
            start(text, new Newline(margin));
        }
    }

    private static void endBlockElement(Editable text) {
        Newline n = getLast(text, Newline.class);
        if (n != null) {
            appendNewlines(text, n.mNumNewlines);
            text.removeSpan(n);
        }

        Alignment a = getLast(text, Alignment.class);
        if (a != null) {
            setSpanFromMark(text, a, new AlignmentSpan.Standard(a.mAlignment));
        }
    }

    private static void handleBr(Editable text) {
        text.append('\n');
    }

    private void startLi(Editable text, Attributes attributes) {
        startBlockElement(text, getMarginListItem());
        start(text, new NewBulletSpan());
        startCssStyle(text, attributes);
    }

    private static void endLi(Editable text) {
        endCssStyle(text);
        endBlockElement(text);
        end(text, NewBulletSpan.class, new NewBulletSpan());
    }

    private static <T> T getLast(Spanned text, Class<T> kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
        T[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    private static void setSpanFromMark(Spannable text, Object mark, Object... spans) {
        int where = text.getSpanStart(mark);
        text.removeSpan(mark);
        int len = text.length();
        if (where != len) {
            for (Object span : spans) {
                text.setSpan(span, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static void start(Editable text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    }

    private static void end(Editable text, Class kind, Object repl) {
        Object obj = getLast(text, kind);
        if (obj != null) {
            setSpanFromMark(text, obj, repl);
        }
    }

    private void startCssStyle(Editable text, Attributes attributes) {
        String style = attributes.getValue("", "style");
        if (style != null) {
            Matcher m = getForegroundColorPattern().matcher(style);
            if (m.find()) {
                int c = getHtmlColor(m.group(1));
                if (c != -1) {
                    start(text, new Foreground(c | 0xFF000000));
                }
            }

            m = getBackgroundColorPattern().matcher(style);
            if (m.find()) {
                int c = getHtmlColor(m.group(1));
                if (c != -1) {
                    start(text, new Background(c | 0xFF000000));
                }
            }

            m = getTextDecorationPattern().matcher(style);
            if (m.find()) {
                String textDecoration = m.group(1);
                if (textDecoration.equalsIgnoreCase("line-through")) {
                    start(text, new Strikethrough());
                }
            }
        }
    }

    private static void endCssStyle(Editable text) {
        Strikethrough s = getLast(text, Strikethrough.class);
        if (s != null) {
            setSpanFromMark(text, s, new StrikethroughSpan());
        }

        Background b = getLast(text, Background.class);
        if (b != null) {
            setSpanFromMark(text, b, new BackgroundColorSpan(b.mBackgroundColor));
        }

        Foreground f = getLast(text, Foreground.class);
        if (f != null) {
            setSpanFromMark(text, f, new ForegroundColorSpan(f.mForegroundColor));
        }
    }


    private static void startA(Editable text, Attributes attributes) {
        String href = attributes.getValue("", "href");
        start(text, new Href(href));
    }

    private static void endA(Editable text) {
        Href h = getLast(text, Href.class);
        if (h != null) {
            if (h.mHref != null) {
                setSpanFromMark(text, h, new LinkSpan((h.mHref)));
            }
        }
    }

    private int getHtmlColor(String color) {
        if ((mFlags & android.text.Html.FROM_HTML_OPTION_USE_CSS_COLORS)
                == android.text.Html.FROM_HTML_OPTION_USE_CSS_COLORS) {
            Integer i = sColorMap.get(color.toLowerCase(Locale.US));
            if (i != null) {
                return i;
            }
        }
        return Color.parseColor(color);
    }

    public void setDocumentLocator(Locator locator) {
        // do nothing.
    }

    @Override
    public void startDocument() {
        // do nothing.
    }

    public void endDocument() throws SAXException {
        // do nothing.
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // do nothing.
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // do nothing.
    }

    @Override
    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attributes) throws SAXException {
        handleStartTag(localName, attributes);
    }

    @Override
    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {
        handleEndTag(localName);
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        StringBuilder sb = new StringBuilder();

        /*
         * Ignore whitespace that immediately follows other whitespace;
         * newlines count as spaces.
         */

        for (int i = 0; i < length; i++) {
            char c = ch[i + start];

            if (c == ' ' || c == '\n') {
                char pred;
                int len = sb.length();

                if (len == 0) {
                    len = mSpannableStringBuilder.length();

                    if (len == 0) {
                        pred = '\n';
                    } else {
                        pred = mSpannableStringBuilder.charAt(len - 1);
                    }
                } else {
                    pred = sb.charAt(len - 1);
                }

                if (pred != ' ' && pred != '\n') {
                    sb.append(' ');
                }
            } else {
                sb.append(c);
            }
        }

        mSpannableStringBuilder.append(sb);
    }

    public void ignorableWhitespace(
            char ch[], int start, int length) throws SAXException {
        // do nothing.
    }


    public void processingInstruction(
            String target, String data) throws SAXException {
        // do nothing.
    }

    public void skippedEntity(String name) throws SAXException {
        // do nothing.
    }

    private static class Bold {
        // do nothing.
    }

    private static class Underline {
        // do nothing.
    }

    private static class Strikethrough {
        // do nothing.
    }

    private static class Tag {
        // do nothing.
    }

    private static class Del {
        // do nothing.
    }

    private static class Bullet {
        // do nothing.
    }

    private static class Href {
        public String mHref;

        public Href(String href) {
            mHref = href;
        }
    }

    private static class Foreground {
        private final int mForegroundColor;

        public Foreground(int foregroundColor) {
            mForegroundColor = foregroundColor;
        }
    }

    private static class Background {
        private final int mBackgroundColor;

        public Background(int backgroundColor) {
            mBackgroundColor = backgroundColor;
        }
    }

    private static class Newline {
        private final int mNumNewlines;

        public Newline(int numNewlines) {
            mNumNewlines = numNewlines;
        }
    }

    private static class Alignment {
        private final Layout.Alignment mAlignment;

        public Alignment(Layout.Alignment alignment) {
            mAlignment = alignment;
        }
    }
}