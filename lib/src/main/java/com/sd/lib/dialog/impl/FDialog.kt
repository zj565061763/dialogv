package com.sd.lib.dialog.impl

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.sd.lib.dialog.IDialog
import com.sd.lib.dialog.ITargetDialog
import com.sd.lib.dialog.R
import com.sd.lib.dialog.animator.*

open class FDialog : IDialog {
    private val _activity: Activity

    private val _dialogView: InternalDialogView
    private var _contentView: View? = null

    private var _state = State.Dismissed
    private var _gravity = Gravity.NO_GRAVITY
    private var _canceledOnTouchOutside = true
    private var _isBackgroundDim = false

    private var _animatorCreator: AnimatorCreator? = null
    private var _animatorDuration: Long = 0

    private var _lockDialog = false
    private var _tryStartShowAnimator = false
    private var _isAnimatorCreatorModifiedInternal = false

    private var _onDismissListener: IDialog.OnDismissListener? = null
    private var _onShowListener: IDialog.OnShowListener? = null

    private val _dialogHandler by lazy { Handler(Looper.getMainLooper()) }

    constructor(activity: Activity) {
        _activity = activity
        _dialogView = InternalDialogView(activity)

        val defaultPadding = (activity.resources.displayMetrics.widthPixels * 0.1f).toInt()
        setPadding(defaultPadding, 0, defaultPadding, 0)

        setBackgroundDim(true)
        gravity = Gravity.CENTER
    }

    override var isDebug: Boolean = false

    override val context: Context get() = _activity

    override val ownerActivity: Activity get() = _activity

    override fun getContentView(): View? {
        return _contentView
    }

    override fun setContentView(layoutId: Int) {
        val view = if (layoutId == 0) {
            null
        } else {
            LayoutInflater.from(ownerActivity).inflate(layoutId, _dialogView.containerView, false)
        }
        setContentView(view)
    }

    override fun setContentView(view: View?) {
        setContentViewInternal(view)
    }

    private fun setContentViewInternal(view: View?) {
        val old = _contentView
        if (old === view) return

        _contentView = view
        if (old != null) {
            _dialogView.containerView.removeView(old)
        }

        if (view != null) {
            val p = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.layoutParams?.let {
                p.width = it.width
                p.height = it.height
            }
            _dialogView.containerView.addView(view, p)
        }

        if (isDebug) {
            Log.i(IDialog::class.java.simpleName, "onContentViewChanged:${old} , ${view}")
        }

        onContentViewChanged(old, view)
    }

    override fun <T : View> findViewById(id: Int): T? {
        return _contentView?.findViewById(id)
    }

    override fun setBackgroundDim(dim: Boolean) {
        _isBackgroundDim = dim
        if (dim) {
            val color = ownerActivity.resources.getColor(R.color.lib_dialog_background_dim)
            _dialogView.backgroundView.setBackgroundColor(color)
        } else {
            _dialogView.backgroundView.setBackgroundColor(0)
        }
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        _canceledOnTouchOutside = cancel
    }

    override fun setOnDismissListener(listener: IDialog.OnDismissListener?) {
        _onDismissListener = listener
    }

    override fun setOnShowListener(listener: IDialog.OnShowListener?) {
        _onShowListener = listener
    }

    override var animatorCreator: AnimatorCreator?
        get() = _animatorCreator
        set(value) {
            _animatorCreator = value
            _isAnimatorCreatorModifiedInternal = false
        }

    override var gravity: Int
        get() = _gravity
        set(value) {
            _dialogView.containerView.gravity = value
        }

    override fun setAnimatorDuration(duration: Long) {
        _animatorDuration = duration
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        _dialogView.containerView.setPadding(left, top, right, bottom)
    }

    override val paddingLeft: Int get() = _dialogView.containerView.paddingLeft

    override val paddingTop: Int get() = _dialogView.containerView.paddingTop

    override val paddingRight: Int get() = _dialogView.containerView.paddingRight

    override val paddingBottom: Int get() = _dialogView.containerView.paddingBottom

    override val isShowing: Boolean get() = _state == State.Shown

    override fun show() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _showRunnable.run()
        } else {
            _dialogHandler.removeCallbacks(_showRunnable)
            _dialogHandler.post(_showRunnable)
        }
    }

    override fun dismiss() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _dismissRunnable.run()
        } else {
            _dialogHandler.removeCallbacks(_dismissRunnable)
            _dialogHandler.post(_dismissRunnable)
        }
    }

    private val _showRunnable = Runnable {
        val isFinishing = ownerActivity.isFinishing
        if (isDebug) {
            Log.i(IDialog::class.java.simpleName, "try show isFinishing:${isFinishing}")
        }
        if (isFinishing) {
            return@Runnable
        }

        if (_state.isShowPart) {
            return@Runnable
        }

        setState(State.TryShow)
        if (_animatorHandler.isHideAnimatorStarted) {
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "cancel HideAnimator before show")
            }
            _animatorHandler.cancelHideAnimator()
        }
        showDialog()
    }

    private val _dismissRunnable = Runnable {
        val isFinishing = ownerActivity.isFinishing
        if (isDebug) {
            Log.i(IDialog::class.java.simpleName, "try dismiss isFinishing:${isFinishing}")
        }

        if (isFinishing) {
            if (_animatorHandler.isShowAnimatorStarted) {
                _animatorHandler.cancelShowAnimator()
            }
            if (_animatorHandler.isHideAnimatorStarted) {
                _animatorHandler.cancelHideAnimator()
            }
            setLockDialog(true)
            dismissDialog(false)
            return@Runnable
        }

        if (_state.isDismissPart) {
            return@Runnable
        }

        setState(State.TryDismiss)
        if (_animatorHandler.isShowAnimatorStarted) {
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "cancel ShowAnimator before dismiss")
            }
            _animatorHandler.cancelShowAnimator()
        }

        setLockDialog(true)
        _animatorHandler.setHideAnimator(createAnimator(false))
        if (_animatorHandler.startHideAnimator()) {
            // 等待动画结束后让窗口消失
        } else {
            dismissDialog(false)
        }
    }

    private fun setState(state: State) {
        if (_state != state) {
            _state = state
            if (isDebug) {
                Log.e(IDialog::class.java.simpleName, "setState:${state}")
            }
            if (state.isDismissPart) {
                setTryStartShowAnimator(false)
            }
        }
    }

    private fun setLockDialog(lock: Boolean) {
        if (_lockDialog != lock) {
            _lockDialog = lock
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "setLockDialog:${lock}")
            }
        }
    }

    private fun setTryStartShowAnimator(tryShow: Boolean) {
        if (_tryStartShowAnimator != tryShow) {
            _tryStartShowAnimator = tryShow
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "setTryStartShowAnimator:${tryShow}")
            }
        }
    }

    private fun startShowAnimator() {
        if (_tryStartShowAnimator) {
            val width = _dialogView.containerView.width
            val height = _dialogView.containerView.height
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "startShowAnimator width:${width} height:${height}")
            }

            if (width > 0 && height > 0) {
                setTryStartShowAnimator(false)
                _animatorHandler.setShowAnimator(createAnimator(true))
                _animatorHandler.startShowAnimator()
            }
        }
    }

    override fun startDismissRunnable(delay: Long) {
        stopDismissRunnable()
        _dialogHandler.postDelayed(_delayedDismissRunnable, delay)
    }

    override fun stopDismissRunnable() {
        _dialogHandler.removeCallbacks(_delayedDismissRunnable)
    }

    private val _delayedDismissRunnable = Runnable { dismiss() }

    private fun setDefaultConfigBeforeShow() {
        if (_animatorCreator == null) {
            when (_gravity) {
                Gravity.CENTER -> {
                    animatorCreator = AlphaCreator()
                    _isAnimatorCreatorModifiedInternal = true
                }
                Gravity.LEFT,
                Gravity.LEFT or Gravity.CENTER -> {
                    animatorCreator = SlideRightLeftParentCreator()
                    _isAnimatorCreatorModifiedInternal = true
                }
                Gravity.TOP,
                Gravity.TOP or Gravity.CENTER -> {
                    animatorCreator = SlideBottomTopParentCreator()
                    _isAnimatorCreatorModifiedInternal = true
                }
                Gravity.RIGHT,
                Gravity.RIGHT or Gravity.CENTER -> {
                    animatorCreator = SlideLeftRightParentCreator()
                    _isAnimatorCreatorModifiedInternal = true
                }
                Gravity.BOTTOM,
                Gravity.BOTTOM or Gravity.CENTER -> {
                    animatorCreator = SlideTopBottomParentCreator()
                    _isAnimatorCreatorModifiedInternal = true
                }
            }
        }
    }

    private val _animatorHandler: FVisibilityAnimatorHandler by lazy {
        FVisibilityAnimatorHandler().also {
            it.setShowAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    if (isDebug) {
                        Log.i(IDialog::class.java.simpleName, "show onAnimationStart ")
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    if (isDebug) {
                        Log.i(IDialog::class.java.simpleName, "show onAnimationCancel ")
                    }
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (isDebug) {
                        Log.i(IDialog::class.java.simpleName, "show onAnimationEnd ")
                    }
                }
            })

            it.setHideAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    if (isDebug) {
                        Log.i(IDialog::class.java.simpleName, "dismiss onAnimationStart ")
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    if (isDebug) {
                        Log.i(IDialog::class.java.simpleName, "dismiss onAnimationCancel ")
                    }
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (isDebug) {
                        Log.i(IDialog::class.java.simpleName, "dismiss onAnimationEnd ")
                    }
                    dismissDialog(true)
                }
            })
        }
    }

    private fun createAnimator(show: Boolean): Animator? {
        // 背景View动画
        val animatorBackground = if (_isBackgroundDim) {
            _backgroundViewAnimatorCreator.createAnimator(show, _dialogView.backgroundView)
        } else {
            null
        }

        // 内容View动画
        val creator = _animatorCreator
        val view = _contentView
        val animatorContent = if (creator == null || view == null) {
            null
        } else {
            creator.createAnimator(show, view)
        }

        val animator = if (animatorBackground != null && animatorContent != null) {
            val duration = Utils.getAnimatorDuration(animatorContent)
            if (duration < 0) throw RuntimeException("Illegal duration:${duration}")
            animatorBackground.duration = duration

            AnimatorSet().apply {
                this.play(animatorBackground).with(animatorContent)
            }
        } else {
            animatorContent
        }

        if (_animatorDuration > 0) {
            animator?.duration = _animatorDuration
        }

        if (isDebug) {
            val showOrDismiss = if (show) "show" else "dismiss"
            Log.i(IDialog::class.java.simpleName, "createAnimator ${showOrDismiss} animator ${animator}")
        }
        return animator
    }

    private val _backgroundViewAnimatorCreator: AnimatorCreator by lazy {
        object : ObjectAnimatorCreator() {
            override fun getPropertyName(): String {
                return View.ALPHA.name
            }

            override fun getValueHidden(view: View): Float {
                return 0.0f
            }

            override fun getValueShown(view: View): Float {
                return 1.0f
            }

            override fun getValueCurrent(view: View): Float {
                return view.alpha
            }

            override fun onAnimationStart(show: Boolean, view: View) {
                super.onAnimationStart(show, view)
                view.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(show: Boolean, view: View) {
                super.onAnimationEnd(show, view)
                if (!show) {
                    view.visibility = View.INVISIBLE
                }
            }
        }
    }

    protected open fun onContentViewChanged(oldView: View?, contentView: View?) {}

    protected open fun onCreate(savedInstanceState: Bundle?) {}

    protected open fun onSaveInstanceState(bundle: Bundle?) {}

    protected open fun onStart() {}

    protected open fun onStop() {}

    protected open fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    protected open fun onTouchOutside(event: MotionEvent?) {}

    private val _targetDialog: SimpleTargetDialog by lazy {
        SimpleTargetDialog(this)
    }

    override fun target(): ITargetDialog {
        return _targetDialog
    }

    private inner class InternalDialogView : FrameLayout {
        private val KEY_SUPER_STATE = "InternalDialogView_super_onSaveInstanceState"

        val backgroundView: View
        val containerView: LinearLayout

        private var _shouldNotifyCreate = true
        private var _savedInstanceState: Bundle? = null

        constructor(context: Context) : super(context) {
            backgroundView = InternalBackgroundView(context)
            addView(backgroundView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

            containerView = InternalContainerView(context)
            addView(containerView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }

        override fun onSaveInstanceState(): Parcelable? {
            return Bundle().also { bundle ->
                this@FDialog.onSaveInstanceState(bundle)
                bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
            }
        }

        override fun onRestoreInstanceState(state: Parcelable) {
            if (state is Bundle) {
                super.onRestoreInstanceState(state.getParcelable(KEY_SUPER_STATE))
                _savedInstanceState = state
                notifyCreate()
            } else {
                super.onRestoreInstanceState(state)
            }
        }

        private fun notifyCreate() {
            if (_shouldNotifyCreate) {
                _shouldNotifyCreate = false
                onCreate(_savedInstanceState)
            }
        }

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            return if (_lockDialog) {
                true
            } else {
                super.onInterceptTouchEvent(ev)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (_lockDialog) {
                return true
            }

            if (event.action == MotionEvent.ACTION_DOWN) {
                val isViewUnder = Utils.isViewUnder(_contentView, event.x.toInt(), event.y.toInt())
                if (isViewUnder) {
                    // 不处理
                } else {
                    onTouchOutside(event)
                    if (_canceledOnTouchOutside) {
                        if (isDebug) {
                            Log.i(IDialog::class.java.simpleName, "touch outside try dismiss")
                        }
                        dismiss()
                        return true
                    }
                }
            }

            if (this@FDialog.onTouchEvent(event)) {
                return true
            }

            super.onTouchEvent(event)
            return true
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "onAttachedToWindow")
            }
            notifyCreate()
            notifyStart()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "onDetachedFromWindow")
            }
            notifyStop()
        }

        override fun onViewAdded(child: View) {
            super.onViewAdded(child)
            if (child !== backgroundView && child !== containerView) {
                throw RuntimeException("can not add view to dialog view")
            }
        }

        override fun onViewRemoved(child: View) {
            super.onViewRemoved(child)
            if (child === backgroundView || child === containerView) {
                throw RuntimeException("can not remove dialog child")
            }
        }

        override fun setVisibility(visibility: Int) {
            if (visibility == GONE || visibility == INVISIBLE) {
                throw RuntimeException("can not hide dialog")
            }
            super.setVisibility(visibility)
        }
    }

    private inner class InternalContainerView : LinearLayout {
        constructor(context: Context) : super(context)

        override fun setGravity(gravity: Int) {
            if (_gravity != gravity) {
                _gravity = gravity
                super.setGravity(gravity)
            }
        }

        override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
            val finalLeft = if (left < 0) paddingLeft else left
            val finalTop = if (top < 0) paddingTop else top
            val finalRight = if (right < 0) paddingRight else right
            val finalBottom = if (bottom < 0) paddingBottom else bottom

            if (finalLeft != paddingLeft ||
                finalTop != paddingTop ||
                finalRight != paddingRight ||
                finalBottom != paddingBottom
            ) {
                super.setPadding(finalLeft, finalTop, finalRight, finalBottom)
            }
        }

        override fun setVisibility(visibility: Int) {
            if (visibility == GONE || visibility == INVISIBLE) {
                throw RuntimeException("can not hide container")
            }
            super.setVisibility(visibility)
        }

        override fun onViewAdded(child: View) {
            super.onViewAdded(child)
            if (child !== _contentView) {
                throw RuntimeException("can not add view to container")
            }
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "onContentViewAdded:${child}")
            }
        }

        override fun onViewRemoved(child: View) {
            super.onViewRemoved(child)
            if (child === _contentView) {
                // 外部直接移除内容view的话，关闭窗口
                dismiss()
            }
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "onContentViewRemoved:${child}")
            }
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            super.onLayout(changed, l, t, r, b)
            if (changed) {
                Utils.checkMatchLayoutParams(this)
            }
            startShowAnimator()
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (_state.isShowPart) {
                setTryStartShowAnimator(true)
                startShowAnimator()
            }
        }
    }

    private inner class InternalBackgroundView : View {
        constructor(context: Context) : super(context) {
            setBackgroundColor(Color.TRANSPARENT)
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            super.onLayout(changed, l, t, r, b)
            if (changed) {
                Utils.checkMatchLayoutParams(this)
            }
        }
    }

    private fun showDialog() {
        if (isDebug) {
            Log.e(IDialog::class.java.simpleName, "showDialog")
        }

        val container = ownerActivity.findViewById<ViewGroup>(android.R.id.content)
        container.addView(
            _dialogView,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        setState(State.Shown)
    }

    private fun dismissDialog(isAnimator: Boolean) {
        if (isDebug) {
            Log.e(IDialog::class.java.simpleName, "dismissDialog by animator:${isAnimator}")
        }

        try {
            val container = ownerActivity.findViewById<ViewGroup>(android.R.id.content)
            container.removeView(_dialogView)
        } catch (e: Exception) {
            e.printStackTrace()
            if (isDebug) {
                Log.e(IDialog::class.java.simpleName, "dismissDialog error:${e}")
            }
        } finally {
            setState(State.Dismissed)
        }
    }

    private fun notifyStart() {
        if (isDebug) {
            Log.i(IDialog::class.java.simpleName, "notifyStart")
        }

        activityLifecycleCallbacks.register(true)
        FDialogHolder.addDialog(this@FDialog)

        onStart()
        if (_targetDialog != null) {
            _targetDialog!!.onStart()
        }

        setLockDialog(false)
        setDefaultConfigBeforeShow()

        _dialogHandler.post {
            if (_onShowListener != null) {
                _onShowListener!!.onShow(this@FDialog)
            }
        }
    }

    private fun notifyStop() {
        if (isDebug) {
            Log.i(IDialog::class.java.simpleName, "notifyStop")
        }

        activityLifecycleCallbacks.register(false)
        FDialogHolder.removeDialog(this@FDialog)

        stopDismissRunnable()
        onStop()
        if (_targetDialog != null) {
            _targetDialog!!.onStop()
        }

        if (_isAnimatorCreatorModifiedInternal) {
            animatorCreator = null
        }

        _dialogHandler.post {
            if (_onDismissListener != null) {
                _onDismissListener!!.onDismiss(this@FDialog)
            }
        }
    }

    private var mActivityLifecycleCallbacks: InternalActivityLifecycleCallbacks? = null
    private val activityLifecycleCallbacks: InternalActivityLifecycleCallbacks
        private get() {
            if (mActivityLifecycleCallbacks == null) {
                mActivityLifecycleCallbacks = InternalActivityLifecycleCallbacks()
            }
            return mActivityLifecycleCallbacks!!
        }

    private inner class InternalActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
        fun register(register: Boolean) {
            val application = ownerActivity.application
            application.unregisterActivityLifecycleCallbacks(this)
            if (register) {
                application.registerActivityLifecycleCallbacks(this)
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            if (activity === ownerActivity) {
                if (isDebug) {
                    Log.e(IDialog::class.java.simpleName, "onActivityDestroyed")
                }
                FDialogHolder.remove(ownerActivity)
                dismiss()
            }
        }
    }

    companion object {
        /**
         * 返回Activity的所有窗口
         */
        fun getAll(activity: Activity?): List<FDialog>? {
            return FDialogHolder[activity!!]
        }

        /**
         * 关闭指定Activity的所有窗口
         */
        fun dismissAll(activity: Activity) {
            if (activity.isFinishing) {
                return
            }
            val list = getAll(activity)
            if (list == null || list.isEmpty()) {
                return
            }
            for (item in list) {
                item.dismiss()
            }
        }
    }
}

private enum class State {
    TryShow, Shown, TryDismiss, Dismissed;

    val isShowPart: Boolean
        get() = this == Shown || this == TryShow

    val isDismissPart: Boolean
        get() = this == Dismissed || this == TryDismiss
}