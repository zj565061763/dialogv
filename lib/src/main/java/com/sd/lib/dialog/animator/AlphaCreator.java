package com.sd.lib.dialog.animator;

import android.view.View;

/**
 * 透明度
 */
public class AlphaCreator extends ObjectAnimatorCreator
{
    @Override
    protected final String getPropertyName()
    {
        return View.ALPHA.getName();
    }

    @Override
    protected float getValueHidden(View view)
    {
        return 0.0f;
    }

    @Override
    protected float getValueShown(View view)
    {
        return 1.0f;
    }

    @Override
    protected float getValueCurrent(View view)
    {
        return view.getAlpha();
    }
}
