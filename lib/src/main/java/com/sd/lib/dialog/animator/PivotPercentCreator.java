package com.sd.lib.dialog.animator;

import android.animation.Animator;
import android.view.View;

/**
 * 在动画开始的时候修改view的锚点，动画结束后还原view的锚点
 */
public class PivotPercentCreator extends BaseAnimatorCreator {
    private final float mPivotPercentX;
    private final float mPivotPercentY;

    private final PivotCreator mPivotCreator;

    /**
     * @param creator
     * @param pivotPercentX x方向锚点百分比[0-1]
     * @param pivotPercentY y方向锚点百分比[0-1]
     */
    public PivotPercentCreator(AnimatorCreator creator, float pivotPercentX, float pivotPercentY) {
        mPivotPercentX = pivotPercentX;
        mPivotPercentY = pivotPercentY;

        mPivotCreator = new PivotCreator(creator, new PivotCreator.PivotProvider() {
            @Override
            public float getPivot(boolean show, View view) {
                return mPivotPercentX * view.getWidth();
            }
        }, new PivotCreator.PivotProvider() {
            @Override
            public float getPivot(boolean show, View view) {
                return mPivotPercentY * view.getHeight();
            }
        });
    }

    @Override
    protected final Animator onCreateAnimator(boolean show, View view) {
        return mPivotCreator.createAnimator(show, view);
    }
}
