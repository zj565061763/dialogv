package com.sd.lib.dialog.animator;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.view.View;

public class CombineCreator extends BaseAnimatorCreator {
    private final AnimatorCreator[] mCreators;

    public CombineCreator(AnimatorCreator... creators) {
        if (creators == null || creators.length <= 0) {
            throw new IllegalArgumentException("creators is null or empty");
        }
        for (AnimatorCreator item : creators) {
            if (item == null) {
                throw new NullPointerException("creators array contains null item");
            }
        }
        mCreators = creators;
    }

    protected final AnimatorCreator[] getCreators() {
        return mCreators;
    }

    private Animator getAnimator(boolean show, View view) {
        final AnimatorCreator[] creators = getCreators();
        final AnimatorSet animatorSet = new AnimatorSet();

        Animator last = null;
        for (AnimatorCreator creator : creators) {
            final Animator animator = creator.createAnimator(show, view);
            if (animator == null) continue;

            if (last == null) {
                animatorSet.play(animator);
            } else {
                animatorSet.play(last).with(animator);
            }
            last = animator;
        }

        if (last == null) {
            return null;
        }
        return animatorSet;
    }

    @Override
    protected Animator onCreateAnimator(boolean show, View view) {
        return getAnimator(show, view);
    }
}
