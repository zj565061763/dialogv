package com.sd.lib.dialog.animator;

import android.view.View;

/**
 * 水平方向滑动
 */
public abstract class SlideHorizontalCreator extends ObjectAnimatorCreator
{
    @Override
    protected final String getPropertyName()
    {
        return View.TRANSLATION_X.getName();
    }

    @Override
    protected final float getValueCurrent(View view)
    {
        return view.getTranslationX();
    }
}
