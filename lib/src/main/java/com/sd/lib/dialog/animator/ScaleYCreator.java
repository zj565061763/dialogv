package com.sd.lib.dialog.animator;

import android.view.View;

/**
 * 缩放y
 */
public class ScaleYCreator extends ObjectAnimatorCreator {
    @Override
    protected final String getPropertyName() {
        return View.SCALE_Y.getName();
    }

    @Override
    protected float getValueHidden(View view) {
        return 0.0f;
    }

    @Override
    protected float getValueShown(View view) {
        return 1.0f;
    }

    @Override
    protected float getValueCurrent(View view) {
        return view.getScaleY();
    }
}
