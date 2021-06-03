package com.sd.lib.dialog.animator;

import android.view.View;

/**
 * 向右滑入，向左滑出（相对于内容View父容器）
 */
public class SlideRightLeftParentCreator extends SlideHorizontalCreator
{
    @Override
    protected float getValueHidden(View view)
    {
        return -view.getRight();
    }

    @Override
    protected float getValueShown(View view)
    {
        return 0;
    }
}
