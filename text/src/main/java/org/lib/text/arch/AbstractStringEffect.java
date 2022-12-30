/***
 Copyright (c) 2008-2015 CommonsWare, LLC

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

package org.lib.text.arch;

import android.text.Spannable;
import android.text.style.CharacterStyle;

import org.lib.text.effect.InputText;

public abstract  class AbstractStringEffect<T extends CharacterStyle> extends Effect<String> {

    protected abstract T[] getStringSpans(Spannable str, Selection selection);

    protected abstract String getStringForSpan(T span);

    protected abstract T buildStringSpan(String value);

    @Override
    public boolean existsInSelection(InputText editor) {
        Selection selection = new Selection(editor);
        Spannable str = editor.getText();

        return (getStringSpans(str, selection).length > 0);
    }

    @Override
    public String valueInSelection(InputText editor) {
        Selection selection = new Selection(editor);
        Spannable str = editor.getText();

        T[] spans = getStringSpans(str, selection);

        if (spans.length > 0) {
            return (getStringForSpan(spans[0]));
        }

        return (null);
    }

    @Override
    public void applyToSelection(InputText editor, String value) {
        Selection selection = new Selection(editor);
        Spannable str = editor.getText();

        for (T span : getStringSpans(str, selection)) {
            str.removeSpan(span);
        }

        if (value != null) {
            str.setSpan(buildStringSpan(value), selection.getStart(),
                    selection.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
