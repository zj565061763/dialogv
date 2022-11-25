package com.sd.lib.dialog.impl

import android.animation.Animator
import android.animation.AnimatorSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

internal object Utils {
    @JvmStatic
    fun getAnimatorDuration(animator: Animator): Long {
        var duration = animator.duration
        if (duration > 0) {
            return duration
        }

        if (animator is AnimatorSet) {
            val list: List<Animator> = animator.childAnimations
            for (item in list) {
                val itemDuration = getAnimatorDuration(item)
                if (itemDuration > duration) {
                    duration = itemDuration
                }
            }
        }
        return duration
    }

    @JvmStatic
    fun isViewUnder(view: View?, x: Int, y: Int): Boolean {
        return if (view == null) {
            false
        } else x >= view.left && x < view.right && y >= view.top && y < view.bottom
    }
}