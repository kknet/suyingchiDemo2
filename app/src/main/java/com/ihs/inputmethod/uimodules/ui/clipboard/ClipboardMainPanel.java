package com.ihs.inputmethod.uimodules.ui.clipboard;

import android.view.View;

import com.ihs.panelcontainer.BasePanel;

/**
 * Created by Arthur on 17/11/24.
 */

public class ClipboardMainPanel extends BasePanel {
    @Override
    protected View onCreatePanelView() {
        ClipboardMainView clipboardMainView = new ClipboardMainView();
        return clipboardMainView;
    }
}