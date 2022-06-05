package com.sd.lib.dialog.animator;

import android.view.View;
import android.view.ViewParent;

/**
 * 向上滑入，向下滑出（相对于内容View父容器）
 */
public class SlideTopBottomParentCreator extends SlideVerticalCreator {
    @Override
    protected float getValueHidden(View view) {
        final ViewParent parent = view.getParent();
        if (parent instanceof View) {
            final View parentView = (View) parent;
            final int distance = parentView.getHeight() - view.getTop();
            if (distance > 0)
                return distance;
        }
        return view.getHeight();
    }

    @Override
    protected float getValueShown(View view) {
        return 0;
    }
}
