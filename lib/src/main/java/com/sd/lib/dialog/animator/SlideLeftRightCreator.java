package com.sd.lib.dialog.animator;

import android.view.View;

/**
 * 向左滑入，向右滑出
 */
public class SlideLeftRightCreator extends SlideHorizontalCreator {
    @Override
    protected float getValueHidden(View view) {
        return view.getWidth();
    }

    @Override
    protected float getValueShown(View view) {
        return 0;
    }
}
