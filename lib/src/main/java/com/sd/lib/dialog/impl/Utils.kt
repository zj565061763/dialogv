package com.sd.lib.dialog.impl

import android.animation.Animator
import android.animation.AnimatorSet

internal object Utils {
    @JvmStatic
    fun getAnimatorDuration(animator: Animator): Long {
        var duration = animator.duration
        if (duration > 0) return duration

        if (animator is AnimatorSet) {
            for (item in animator.childAnimations) {
                val itemDuration = getAnimatorDuration(item)
                if (itemDuration > duration) {
                    duration = itemDuration
                }
            }
        }
        return duration
    }
}