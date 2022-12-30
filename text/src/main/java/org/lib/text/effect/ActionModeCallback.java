/***
 Copyright (c) 2012-2014 CommonsWare, LLC

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

import android.app.Activity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.lib.text.arch.ActionModeListener;
import org.lib.text.arch.Selection;

public class ActionModeCallback {
    protected int menuResource;
    protected InputText editor;
    protected Selection selection = null;
    protected ActionModeListener listener;

    ActionModeCallback(int menuResource, InputText editor, ActionModeListener listener) {
        this.menuResource = menuResource;
        this.editor = editor;
        this.listener = listener;
    }

    void setSelection(Selection selection) {
        this.selection = selection;
    }

    public static class Native extends ActionModeCallback implements ActionMode.Callback {



        public Native(int menuResource,
                      InputText editor,
                      ActionModeListener listener) {
            super(menuResource, editor, listener);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.clear();
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(menuResource, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (selection != null) {
                selection.apply(editor);
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            boolean ret = listener.doAction(item.getItemId());
            mode.finish();
            return ret;
        }
    }
}
