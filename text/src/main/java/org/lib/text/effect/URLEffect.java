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

package org.lib.text.effect;

import android.text.Spannable;

import org.lib.text.arch.AbstractStringEffect;
import org.lib.text.arch.Selection;

public class URLEffect extends AbstractStringEffect<LinkSpan> {

    public LinkSpan[] getStringSpans(Spannable str, Selection selection) {
        return str.getSpans(
                selection.getStart(),
                selection.getEnd(),
                LinkSpan.class
        );
    }

    public String getStringForSpan(LinkSpan span) {
        return (span.getURL());
    }

    public LinkSpan buildStringSpan(String value) {
        return (new LinkSpan(value));
    }
}
