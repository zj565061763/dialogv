package com.sd.lib.dialog.animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

public abstract class BaseAnimatorCreator implements AnimatorCreator {
    @Override
    public final Animator createAnimator(final boolean show, final View view) {
        beforeCreateAnimator(show, view);

        final Animator animator = onCreateAnimator(show, view);
        if (animator != null) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    BaseAnimatorCreator.this.onAnimationStart(show, view);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animation.removeListener(this);
                    BaseAnimatorCreator.this.onAnimationEnd(show, view);
                }
            });
            onAnimatorCreated(show, view, animator);
        }
        return animator;
    }

    /**
     * 在动画要创建之前回调
     */
    protected void beforeCreateAnimator(boolean show, View view) {
    }

    /**
     * 创建动画
     */
    protected abstract Animator onCreateAnimator(boolean show, View view);

    /**
     * 动画被创建后回调
     */
    protected void onAnimatorCreated(boolean show, View view, Animator animator) {
    }

    /**
     * 动画开始回调
     */
    protected void onAnimationStart(boolean show, View view) {
    }

    /**
     * 动画结束回调
     */
    protected void onAnimationEnd(boolean show, View view) {
    }

    protected static long getScaledDuration(float value, float maxValue, long maxDuration) {
        if (value == 0) return 0;
        if (maxValue == 0) return 0;
        if (maxDuration <= 0) return 0;

        final float percent = Math.abs(value / maxValue);
        final long duration = (long) (percent * maxDuration);
        if (duration > maxDuration) {
            return maxDuration;
        }
        return duration;
    }
}
