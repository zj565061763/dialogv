package com.sd.lib.dialog.animator;

import android.view.View;

/**
 * 向上滑入，向下滑出
 */
public class SlideTopBottomCreator extends SlideVerticalCreator
{
    @Override
    protected float getValueHidden(View view)
    {
        return view.getHeight();
    }

    @Override
    protected float getValueShown(View view)
    {
        return 0;
    }
}
