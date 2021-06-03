package com.sd.lib.dialog.animator;

import android.view.View;

/**
 * 向右滑入，向左滑出
 */
public class SlideRightLeftCreator extends SlideHorizontalCreator
{
    @Override
    protected float getValueHidden(View view)
    {
        return -view.getWidth();
    }

    @Override
    protected float getValueShown(View view)
    {
        return 0;
    }
}
