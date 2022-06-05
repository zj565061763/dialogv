package com.sd.lib.dialog.animator;

import android.view.View;

/**
 * 向下滑入，向上滑出
 */
public class SlideBottomTopCreator extends SlideVerticalCreator {
    @Override
    protected float getValueHidden(View view) {
        return -view.getHeight();
    }

    @Override
    protected float getValueShown(View view) {
        return 0;
    }
}
