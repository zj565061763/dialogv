package com.sd.lib.dialog.animator

import android.animation.Animator
import android.view.View

interface AnimatorCreator {
    /**
     * 创建动画
     *
     * 注意：隐藏动画不能设置为无限循环，否则窗口将不能被移除
     *
     * @param show true-窗口显示，false-窗口隐藏
     * @param view 窗口内容view
     */
    fun createAnimator(show: Boolean, view: View): Animator?
}