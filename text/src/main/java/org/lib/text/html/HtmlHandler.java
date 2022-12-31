package org.lib.text.html;

import android.graphics.Typeface;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

import org.lib.text.effect.LabelSpan;
import org.lib.text.effect.NewBulletSpan;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.lib.text.effect.StrikeLineSpan;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * This class processes HTML strings into displayable styled text.
 * Not all HTML tags are supported.
 */
public class HtmlHandler {

    /**
     * Lazy initialization holder for HTML parser. This class will
     * a) be preloaded by the zygote, or b) not loaded until absolutely
     * necessary.
     */
    private static class HtmlDocument {
        private static final HTMLSchema schema = new HTMLSchema();
    }

    /**
     * Returns displayable styled text from the provided HTML string. Any &lt;img&gt; tags in the
     * HTML will use the specified ImageGetter to request a representation of the image (use null
     * if you don't want this) and the specified TagHandler to handle unknown tags (specify null if
     * you don't want this).
     *
     * <p>This uses TagSoup to handle real HTML, including all of the brokenness found in the wild.
     */
    public static Spanned fromHtml(String source, Html.ImageGetter imageGetter) {
        Parser parser = new Parser();

        try {
            parser.setProperty(Parser.schemaProperty, HtmlDocument.schema);
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            // Should not happen.
            throw new RuntimeException(e);
        }

        ToSpannedConverter converter
                = new ToSpannedConverter(source, parser, imageGetter);

        return converter.convert();
    }


    /**
     * Returns an HTML representation of the provided Spanned text. A best effort is
     * made to add HTML tags corresponding to spans. Also note that HTML metacharacters
     * (such as "&lt;" and "&amp;") within the input text are escaped.
     *
     * @param text   input text to convert
     * @return string containing input converted to HTML
     */
    public static String toHtml(Spanned text) {
        StringBuilder out = new StringBuilder();
        withinHtml(out, text);
        return out.toString();
    }

    private static void withinHtml(StringBuilder out, Spanned text) {

        int len = text.length();
        boolean isInList = false;
        int next;

        for (int i = 0; i <= len; i = next) {
            next = TextUtils.indexOf(text, '\n', i, len);
            if (next < 0) {
                next = len;
            }

            if (next == i) {
                if (isInList) {
                    // Current paragraph is no longer a list item;
                    // close the previously opened list
                    isInList = false;
                    out.append("</ul>\n");
                    out.append("<br>\n");
                }

            } else {
                boolean isListItem = false;
                ParagraphStyle[] paragraphStyles = text.getSpans(i, next, ParagraphStyle.class);
                for (ParagraphStyle paragraphStyle : paragraphStyles) {
                    if (paragraphStyle instanceof NewBulletSpan) {
                        isListItem = true;
                        break;
                    }
                }

                if (isListItem && !isInList) {
                    // Current paragraph is the first item in a list
                    isInList = true;
                    out.append("<ul").append(">\n");
                }

                if (isInList && !isListItem) {
                    // Current paragraph is no longer a list item; close the previously opened list
                    isInList = false;
                    out.append("</ul>\n");
                }

                String tagType = isListItem ? "li" : "p";
                out.append("<").append(tagType).append(">");

                withinParagraph(out, text, i, next);

                out.append("</");
                out.append(tagType);
                out.append(">\n");

                if (next == len && isInList) {
                    isInList = false;
                    out.append("</ul>\n");
                }
            }
            next++;
        } // end for.
    }


    private static void withinParagraph(StringBuilder out, Spanned text, int start, int end) {
        int next;
        for (int i = start; i < end; i = next) {
            next = text.nextSpanTransition(i, end, CharacterStyle.class);
            CharacterStyle[] style = text.getSpans(i, next, CharacterStyle.class);

            for (CharacterStyle characterStyle : style) {
                if (characterStyle instanceof StyleSpan) {
                    int s = ((StyleSpan) characterStyle).getStyle();

                    if ((s & Typeface.BOLD) != 0) {
                        out.append("<b>");
                    }
                }
                if (characterStyle instanceof UnderlineSpan) {
                    out.append("<u>");
                }
                if (characterStyle instanceof StrikethroughSpan) {
                    out.append("<span style=\"text-decoration:line-through;\">");
                }
                if (characterStyle instanceof URLSpan) {
                    out.append("<a href=\"");
                    out.append(((URLSpan) characterStyle).getURL());
                    out.append("\">");
                }
                if (characterStyle instanceof LabelSpan) {
                    out.append("<cell style=\"background-color:#14985050;color:#985858;font-weight:normal;border-radius: 5px;text-decoration:none;padding:1px 5px 1px 5px;\">");
                }
                if (characterStyle instanceof StrikeLineSpan) {
                    out.append("<del>");
                }

                if (characterStyle instanceof ImageSpan) {
                    out.append("<img src=\"");
                    out.append(((ImageSpan) characterStyle).getSource());
                    out.append("\">");

                    // Don't output the dummy character underlying the image.
                    i = next;
                }
            }

            withinStyle(out, text, i, next);

            for (int j = style.length - 1; j >= 0; j--) {

                if (style[j] instanceof StrikeLineSpan) {
                    out.append("</del>");
                }

                if (style[j] instanceof LabelSpan) {
                    out.append("</cell>");
                }

                if (style[j] instanceof URLSpan) {
                    out.append("</a>");
                }
                if (style[j] instanceof StrikethroughSpan) {
                    out.append("</span>");
                }
                if (style[j] instanceof UnderlineSpan) {
                    out.append("</u>");
                }
                if (style[j] instanceof TypefaceSpan) {
                    String s = ((TypefaceSpan) style[j]).getFamily();

                    if (s.equals("monospace")) {
                        out.append("</tt>");
                    }
                }
                if (style[j] instanceof StyleSpan) {
                    int s = ((StyleSpan) style[j]).getStyle();

                    if ((s & Typeface.BOLD) != 0) {
                        out.append("</b>");
                    }
                }
            }
        }
    }

    private static void withinStyle(StringBuilder out, CharSequence text,
                                    int start, int end) {
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);

            if (c == '<') {
                out.append("&lt;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c == '&') {
                out.append("&amp;");
            } else if (c >= 0xD800 && c <= 0xDFFF) {
                if (c < 0xDC00 && i + 1 < end) {
                    char d = text.charAt(i + 1);
                    if (d >= 0xDC00 && d <= 0xDFFF) {
                        i++;
                        int codepoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
                        out.append("&#").append(codepoint).append(";");
                    }
                }
            } else if (c > 0x7E || c < ' ') {
                out.append("&#").append((int) c).append(";");
            } else if (c == ' ') {
                while (i + 1 < end && text.charAt(i + 1) == ' ') {
                    out.append("&nbsp;");
                    i++;
                }

                out.append(' ');
            } else {
                out.append(c);
            }
        }
    }
}