package com.sd.lib.dialog.impl

import android.animation.Animator

internal class FVisibilityAnimatorHandler {
    private var _showAnimator: Animator? = null
    private var _hideAnimator: Animator? = null

    private val _showAnimatorListener = AnimatorListenerWrapper()
    private val _hideAnimatorListener = AnimatorListenerWrapper()

    //---------- Show start ----------

    /**
     * 设置显示动画
     */
    fun setShowAnimator(animator: Animator?) {
        val old = _showAnimator
        if (old !== animator) {
            old?.removeListener(_showAnimatorListener)
            _showAnimator = animator
            animator?.addListener(_showAnimatorListener)
        }
    }

    /**
     * 设置显示动画监听
     */
    fun setShowAnimatorListener(listener: Animator.AnimatorListener?) {
        _showAnimatorListener.original = listener
    }

    /**
     * 开始显示动画
     *
     * @return true-动画被执行或者已经在执行
     */
    fun startShowAnimator(): Boolean {
        val animator = _showAnimator ?: return false
        if (animator.isStarted) return true

        cancelHideAnimator()
        animator.start()
        return true
    }

    /**
     * 显示动画是否已经开始
     */
    val isShowAnimatorStarted: Boolean
        get() = _showAnimator?.isStarted ?: false

    /**
     * 取消显示动画
     */
    fun cancelShowAnimator() {
        _showAnimator?.cancel()
    }

    //---------- Show end ----------


    //---------- Hide start ----------

    /**
     * 设置隐藏动画
     */
    fun setHideAnimator(animator: Animator?) {
        val old = _hideAnimator
        if (old !== animator) {
            old?.removeListener(_hideAnimatorListener)
            _hideAnimator = animator
            animator?.addListener(_hideAnimatorListener)
        }
    }

    /**
     * 设置隐藏动画监听
     */
    fun setHideAnimatorListener(listener: Animator.AnimatorListener?) {
        _hideAnimatorListener.original = listener
    }

    /**
     * 开始隐藏动画
     *
     * @return true-动画被执行或者已经在执行
     */
    fun startHideAnimator(): Boolean {
        val animator = _hideAnimator ?: return false
        if (animator.isStarted) return true

        cancelShowAnimator()
        animator.start()
        return true
    }

    /**
     * 隐藏动画是否已经开始执行
     */
    val isHideAnimatorStarted: Boolean
        get() = _hideAnimator?.isStarted ?: false

    /**
     * 取消隐藏动画
     */
    fun cancelHideAnimator() {
        _hideAnimator?.cancel()
    }

    //---------- Hide end ----------
}

private class AnimatorListenerWrapper : Animator.AnimatorListener {
    var original: Animator.AnimatorListener? = null

    override fun onAnimationStart(animation: Animator) {
        original?.onAnimationStart(animation)
    }

    override fun onAnimationEnd(animation: Animator) {
        original?.onAnimationEnd(animation)
    }

    override fun onAnimationCancel(animation: Animator) {
        original?.onAnimationCancel(animation)
    }

    override fun onAnimationRepeat(animation: Animator) {
        original?.onAnimationRepeat(animation)
    }
}