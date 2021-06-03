package com.sd.lib.dialog.animator;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

public abstract class ObjectAnimatorCreator extends BaseAnimatorCreator
{
    @Override
    protected final Animator onCreateAnimator(boolean show, View view)
    {
        final ObjectAnimator animator = new ObjectAnimator();
        animator.setPropertyName(getPropertyName());

        final float valueHidden = getValueHidden(view);
        final float valueShown = getValueShown(view);
        final float[] values = show ? new float[]{valueHidden, valueShown} : new float[]{getValueCurrent(view), valueHidden};
        animator.setFloatValues(values);

        final long duration = getScaledDuration(values[0] - values[1], valueShown - valueHidden, getDuration());
        animator.setDuration(duration);

        animator.setTarget(view);
        return animator;
    }

    protected abstract String getPropertyName();

    /**
     * 返回动画执行到于隐藏状态的值
     *
     * @param view
     * @return
     */
    protected abstract float getValueHidden(View view);

    /**
     * 返回动画执行到于显示状态的值
     *
     * @param view
     * @return
     */
    protected abstract float getValueShown(View view);

    /**
     * 返回当前的值
     *
     * @param view
     * @return
     */
    protected abstract float getValueCurrent(View view);

    /**
     * 返回动画时长，默认200毫秒
     *
     * @return
     */
    protected long getDuration()
    {
        return 200;
    }
}
