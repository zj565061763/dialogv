package com.sd.lib.dialog.animator;

import android.view.View;
import android.view.ViewParent;

/**
 * 向左滑入，向右滑出（相对于内容View父容器）
 */
public class SlideLeftRightParentCreator extends SlideHorizontalCreator {
    @Override
    protected float getValueHidden(View view) {
        final ViewParent parent = view.getParent();
        if (parent instanceof View) {
            final View parentView = (View) parent;
            final int distance = parentView.getWidth() - view.getLeft();
            if (distance > 0)
                return distance;
        }
        return view.getWidth();
    }

    @Override
    protected float getValueShown(View view) {
        return 0;
    }
}
