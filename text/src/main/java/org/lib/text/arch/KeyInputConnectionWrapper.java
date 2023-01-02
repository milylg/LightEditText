package org.lib.text.arch;

import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

public class KeyInputConnectionWrapper extends InputConnectionWrapper {

    private BackspaceKeyListener backspaceKeyEvent;

    /**
     * Initializes a wrapper.
     *
     * <p><b>Caveat:</b> Although the system can accept {@code (InputConnection) null} in some
     * places, you cannot emulate such a behavior by non-null {@link InputConnectionWrapper} that
     * has {@code null} in {@code target}.</p>
     *
     * @param target  the {@link InputConnection} to be proxied.
     * @param mutable set {@code true} to protect this object from being reconfigured to target
     *                another {@link InputConnection}.  Note that this is ignored while the target is {@code null}.
     */
    public KeyInputConnectionWrapper(InputConnection target, boolean mutable) {
        super(target, mutable);
    }

    // For Google Input Method.
    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {

        if (backspaceKeyEvent != null) {
            backspaceKeyEvent.onBackspaceKey();
        }
        return super.deleteSurroundingText(beforeLength, afterLength);
    }

    /**
     * For Other Input Method Exclude Google Input Method.
     * 当在软件盘上点击某些按钮（比如退格键，数字键，回车键等），该方法可能会被触发（取决于输入法的开发者），
     * 所以也可以重写该方法并拦截这些事件，这些事件就不会被分发到输入框了
     * @param event
     * @return
     */
    @Override
    public boolean sendKeyEvent(KeyEvent event) {

        if (!(event.getKeyCode() == KeyEvent.KEYCODE_DEL
                && event.getAction() == KeyEvent.ACTION_DOWN)) {
            return super.sendKeyEvent(event);
        }

        if (backspaceKeyEvent != null) {
            backspaceKeyEvent.onBackspaceKey();
        }

        return super.sendKeyEvent(event);
    }

    public void setBackspaceKeyAction(BackspaceKeyListener listener) {
        this.backspaceKeyEvent = listener;
    }
}
