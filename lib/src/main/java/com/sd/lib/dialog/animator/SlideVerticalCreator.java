package com.sd.lib.dialog.animator;

import android.view.View;

/**
 * 竖直方向滑动
 */
public abstract class SlideVerticalCreator extends ObjectAnimatorCreator {
    @Override
    protected final String getPropertyName() {
        return View.TRANSLATION_Y.getName();
    }

    @Override
    protected final float getValueCurrent(View view) {
        return view.getTranslationY();
    }
}
