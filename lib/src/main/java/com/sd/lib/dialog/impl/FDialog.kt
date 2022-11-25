package com.sd.lib.dialog.impl

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.sd.lib.dialog.IDialog
import com.sd.lib.dialog.ITargetDialog
import com.sd.lib.dialog.R
import com.sd.lib.dialog.animator.*
import com.sd.lib.dialog.display.ActivityDisplay
import kotlin.properties.Delegates

open class FDialog(activity: Activity) : IDialog {
    private val _activity = activity
    private val _dialogView = InternalDialogView(activity)

    private var _contentView: View? = null
    internal val backgroundView get() = _dialogView.backgroundView
    private val containerView get() = _dialogView.containerView

    private var _state = State.Dismissed
    private var _cancelable = true
    private var _canceledOnTouchOutside = true

    private var _lockTouch by Delegates.observable(false) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "_lockTouch $newValue ${this@FDialog}")
            }
        }
    }

    private var _showAnimatorFlag by Delegates.observable(false) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "_showAnimatorFlag $newValue ${this@FDialog}")
            }
        }
    }

    private var _isCover = false
    private var _isAnimatorCreatorModifiedInternal = false

    private var _isCreated = false
    private var _isStarted = false
    private var _isCanceled = false

    private var _onDismissListener: IDialog.OnDismissListener? = null
    private var _onShowListener: IDialog.OnShowListener? = null
    private var _onCancelListener: IDialog.OnCancelListener? = null

    private val _mainHandler by lazy { Handler(Looper.getMainLooper()) }

    override var isDebug: Boolean = false

    override val context: Context get() = _activity

    override val ownerActivity: Activity get() = _activity

    override val contentView: View? get() = _contentView

    override var display: IDialog.Display = ActivityDisplay()

    override fun setContentView(layoutId: Int) {
        val view = if (layoutId == 0) {
            null
        } else {
            LayoutInflater.from(context).inflate(layoutId, containerView, false)
        }
        setContentView(view)
    }

    override fun setContentView(view: View?) {
        val old = _contentView
        if (old === view) return

        _contentView = view

        if (old != null) {
            containerView.removeView(old)
        }

        if (view != null) {
            val p = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.layoutParams?.let {
                p.width = it.width
                p.height = it.height
            }
            containerView.addView(view, p)
        }

        onContentViewChanged(old, view)
    }

    override fun <T : View> findViewById(id: Int): T? {
        return _contentView?.findViewById(id)
    }

    override fun setCancelable(cancel: Boolean) {
        _cancelable = cancel
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        if (cancel) {
            _cancelable = true
        }
        _canceledOnTouchOutside = cancel
    }

    override fun setOnDismissListener(listener: IDialog.OnDismissListener?) {
        _onDismissListener = listener
    }

    override fun setOnShowListener(listener: IDialog.OnShowListener?) {
        _onShowListener = listener
    }

    override fun setOnCancelListener(listener: IDialog.OnCancelListener?) {
        _onCancelListener = listener
    }

    override var animatorDuration: Long = 0

    final override var animatorCreator: AnimatorCreator? by Delegates.observable(null) { _, _, _ ->
        _isAnimatorCreatorModifiedInternal = false
    }

    final override var gravity: Int by Delegates.observable(Gravity.NO_GRAVITY) { _, _, newValue ->
        containerView.gravity = newValue
    }

    final override var isBackgroundDim: Boolean by Delegates.observable(false) { _, _, newValue ->
        if (newValue) {
            val color = context.resources.getColor(R.color.lib_dialogv_background_dim)
            backgroundView.setBackgroundColor(color)
        } else {
            backgroundView.setBackgroundColor(0)
        }
    }

    final override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        containerView.setPadding(left, top, right, bottom)
    }

    override val paddingLeft: Int get() = containerView.paddingLeft

    override val paddingTop: Int get() = containerView.paddingTop

    override val paddingRight: Int get() = containerView.paddingRight

    override val paddingBottom: Int get() = containerView.paddingBottom

    override val isShowing: Boolean get() = _state == State.Shown

    override fun show() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _showRunnable.run()
        } else {
            _mainHandler.removeCallbacks(_showRunnable)
            _mainHandler.post(_showRunnable)
        }
    }

    override fun dismiss() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _dismissRunnable.run()
        } else {
            _mainHandler.removeCallbacks(_dismissRunnable)
            _mainHandler.post(_dismissRunnable)
        }
    }

    override fun cancel() {
        _isCanceled = true
        dismiss()
    }

    private val _showRunnable = Runnable {
        if (ownerActivity.isFinishing) return@Runnable
        if (_state.isShowPart) return@Runnable

        if (isDebug) {
            Log.i(IDialog::class.java.simpleName, "+++++ try show state:$_state ${this@FDialog}")
        }

        if (_animatorHandler.isHideAnimatorStarted) {
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "cancel dismiss animator before show ${this@FDialog}")
            }
            _animatorHandler.cancelHideAnimator()
        }

        setState(State.TryShow)
        showDialog()
    }

    private val _dismissRunnable = Runnable {
        val isFinishing = ownerActivity.isFinishing
        if (isDebug) {
            Log.i(IDialog::class.java.simpleName, "----- try dismiss state:$_state isFinishing:${isFinishing} ${this@FDialog}")
        }

        if (isFinishing) {
            _animatorHandler.cancelShowAnimator()
            _animatorHandler.cancelHideAnimator()
            dismissDialog()
            return@Runnable
        }

        if (_state.isDismissPart) {
            return@Runnable
        }

        if (_animatorHandler.isShowAnimatorStarted) {
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "cancel show animator before dismiss ${this@FDialog}")
            }
            _animatorHandler.cancelShowAnimator()
        }

        setState(State.TryDismiss)
        _animatorHandler.setHideAnimator(createAnimator(false))
        if (_animatorHandler.startHideAnimator()) {
            // 等待动画结束后让窗口消失
        } else {
            dismissDialog()
        }
    }

    private fun setState(state: State) {
        if (_state != state) {
            _state = state
            if (isDebug) {
                Log.e(IDialog::class.java.simpleName, "setState:${state} ${this@FDialog}")
            }

            if (state.isDismissPart) {
                stopDismissRunnable()
                _showAnimatorFlag = false
                _lockTouch = true
            }

            when (state) {
                State.Shown -> {
                    _lockTouch = false
                    _mainHandler.post {
                        _onShowListener?.onShow(this@FDialog)
                    }
                }
                State.Dismissed -> {
                    if (_isCanceled) {
                        _isCanceled = false
                        _mainHandler.post {
                            _onCancelListener?.onCancel(this@FDialog)
                        }
                    }
                    _mainHandler.post {
                        _onDismissListener?.onDismiss(this@FDialog)
                    }
                }
                else -> {}
            }
        }
    }

    override fun startDismissRunnable(delay: Long) {
        stopDismissRunnable()
        if (_state.isShowPart) {
            _mainHandler.postDelayed(_delayedDismissRunnable, delay)
        }
    }

    override fun stopDismissRunnable() {
        _mainHandler.removeCallbacks(_delayedDismissRunnable)
    }

    private val _delayedDismissRunnable = Runnable { dismiss() }

    private fun setDefaultConfigBeforeShow() {
        if (animatorCreator == null) {
            when (gravity) {
                Gravity.CENTER -> {
                    animatorCreator = AlphaCreator()
                    _isAnimatorCreatorModifiedInternal = true
                }
                Gravity.LEFT,
                Gravity.LEFT or Gravity.CENTER,
                -> {
                    animatorCreator = SlideRightLeftParentCreator()
                    _isAnimatorCreatorModifiedInternal = true
                }
                Gravity.TOP,
                Gravity.TOP or Gravity.CENTER,
                -> {
                    animatorCreator = SlideBottomTopParentCreator()
                    _isAnimatorCreatorModifiedInternal = true
                }
                Gravity.RIGHT,
                Gravity.RIGHT or Gravity.CENTER,
                -> {
                    animatorCreator = SlideLeftRightParentCreator()
                    _isAnimatorCreatorModifiedInternal = true
                }
                Gravity.BOTTOM,
                Gravity.BOTTOM or Gravity.CENTER,
                -> {
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
                        Log.i(IDialog::class.java.simpleName, "animator show onAnimationStart ${this@FDialog}")
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    if (isDebug) {
                        Log.i(IDialog::class.java.simpleName, "animator show onAnimationCancel ${this@FDialog}")
                    }
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (isDebug) {
                        Log.i(IDialog::class.java.simpleName, "animator show onAnimationEnd ${this@FDialog}")
                    }
                }
            })

            it.setHideAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    if (isDebug) {
                        Log.i(IDialog::class.java.simpleName, "animator dismiss onAnimationStart ${this@FDialog}")
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    if (isDebug) {
                        Log.i(IDialog::class.java.simpleName, "animator dismiss onAnimationCancel ${this@FDialog}")
                    }
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (isDebug) {
                        Log.i(IDialog::class.java.simpleName, "animator dismiss onAnimationEnd ${this@FDialog}")
                    }
                    dismissDialog(true)
                }
            })
        }
    }

    private fun createAnimator(show: Boolean): Animator? {
        // 背景View动画
        val animatorBackground = if (isBackgroundDim) {
            _backgroundViewAnimatorCreator.createAnimator(show, backgroundView)
        } else {
            null
        }

        // 内容View动画
        val creator = animatorCreator
        val view = _contentView
        val animatorContent = if (creator == null || view == null) {
            null
        } else {
            creator.createAnimator(show, view)
        }

        val animator = if (animatorContent != null && animatorBackground != null) {
            val duration = getAnimatorDuration(animatorContent)
            if (duration < 0) error("Illegal duration:${duration}")
            animatorBackground.duration = duration

            AnimatorSet().apply {
                this.play(animatorBackground).with(animatorContent)
            }
        } else {
            animatorContent ?: animatorBackground
        }

        if (animatorDuration > 0) {
            animator?.duration = animatorDuration
        }

        if (isDebug) {
            val textIsShow = if (show) "show" else "dismiss"
            val textIsNull = if (animator == null) "null" else "not null"
            Log.i(IDialog::class.java.simpleName, "animator $textIsShow create $textIsNull ${this@FDialog}")
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

    /**
     * [contentView]变化回调
     */
    protected open fun onContentViewChanged(oldView: View?, newView: View?) {}

    /**
     * 窗口第一次显示之前回调
     */
    protected open fun onCreate() {}

    /**
     * 窗口显示之前回调
     */
    protected open fun onStart() {}

    /**
     * 窗口消失之后回调
     */
    protected open fun onStop() {}

    /**
     * 触摸回调
     *
     * @param isTouchOutside true-触摸[contentView]外部，false-触摸[contentView]内部
     * @return true-消费掉事件，不会继续分发给[contentView]
     */
    protected open fun onTouchEvent(event: MotionEvent, isTouchOutside: Boolean): Boolean {
        return false
    }

    protected open fun onBackPressed() {
        if (_cancelable) {
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "onBackPressed try cancel ${this@FDialog}")
            }
            cancel()
        }
    }

    private var _targetDialog: SimpleTargetDialog? = null

    private val _targetDialogLazy: SimpleTargetDialog by lazy {
        SimpleTargetDialog(this).also {
            _targetDialog = it
        }
    }

    override fun target(): ITargetDialog {
        return _targetDialogLazy
    }

    private fun showDialog() {
        check(_state == State.TryShow) { "Illegal state $_state" }
        if (isDebug) {
            Log.e(IDialog::class.java.simpleName, "showDialog ${this@FDialog}")
        }

        _activityLifecycleCallbacks.register(true)
        FDialogHolder.addDialog(this@FDialog)

        notifyCreate()
        if (_state.isDismissPart) {
            if (isDebug) {
                Log.e(IDialog::class.java.simpleName, "showDialog state changed to $_state when notify onCreate ${this@FDialog}")
            }
            return
        }

        _isStarted = true
        if (isDebug) {
            Log.i(IDialog::class.java.simpleName, "notify onStart ${this@FDialog}")
        }
        onStart()
        if (_state.isDismissPart) {
            if (isDebug) {
                Log.e(IDialog::class.java.simpleName, "showDialog state changed to $_state when notify onStart ${this@FDialog}")
            }
            return
        }

        _targetDialog?.onStart()
        setDefaultConfigBeforeShow()

        if (isDebug) {
            Log.i(IDialog::class.java.simpleName, "display showDialog ${this@FDialog}")
        }
        display.showDialog(_dialogView)
        setState(State.Shown)
    }

    private fun dismissDialog(isAnimator: Boolean = false) {
        if (isDebug) {
            Log.e(IDialog::class.java.simpleName, "dismissDialog state:$_state isAnimator:${isAnimator} ${this@FDialog}")
        }

        _activityLifecycleCallbacks.register(false)
        FDialogHolder.removeDialog(this@FDialog)

        if (_dialogView.isAttachedToWindow) {
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "display dismissDialog ${this@FDialog}")
            }
            display.dismissDialog(_dialogView)
        }

        if (_isStarted) {
            _isStarted = false
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "notify onStop ${this@FDialog}")
            }
            onStop()
        }

        _targetDialog?.onStop()
        if (_isAnimatorCreatorModifiedInternal) {
            animatorCreator = null
        }

        setState(State.Dismissed)
    }

    private fun notifyCreate() {
        if (!_isCreated) {
            _isCreated = true
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "notify onCreate ${this@FDialog}")
            }
            onCreate()
        }
    }

    internal fun notifyCover() {
        if (!_isCover) {
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "notifyCover ${this@FDialog}")
            }
            _isCover = true
            _dialogView.checkFocus(false)
        }
    }

    internal fun notifyCoverRemove() {
        if (_isCover) {
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "notifyCoverRemove ${this@FDialog}")
            }
            _isCover = false
            _dialogView.checkFocus(true)
        }
    }

    private inner class InternalDialogView(context: Context) : FrameLayout(context) {
        val backgroundView: View = View(context)
        val containerView: LinearLayout = InternalContainerView(context)

        init {
            addView(
                backgroundView,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
            addView(
                containerView,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }

        fun checkFocus(check: Boolean) {
            removeCallbacks(_checkFocusRunnable)
            if (check) {
                post(_checkFocusRunnable)
            }
        }

        private val _checkFocusRunnable = object : Runnable {
            override fun run() {
                if (!this@InternalDialogView.isAttachedToWindow) return
                if (_state.isShowPart && FDialogHolder.getLast(_activity) == this@FDialog) {
                    if (!hasFocus()) {
                        if (isDebug) {
                            Log.i(IDialog::class.java.simpleName, "requestFocus ${this@FDialog}")
                        }
                        requestChildFocus(containerView, containerView)
                    }
                }

                postDelayed(this, 1000L)
            }
        }

        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            if (super.dispatchKeyEvent(event)) return true
            return event.dispatch(_keyEventCallback, keyDispatcherState, this)
        }

        private val _keyEventCallback = object : KeyEvent.Callback {
            override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    event.startTracking()
                    return true
                }
                return false
            }

            override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
                if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)
                    && !event.isCanceled
                    && event.isTracking
                ) {
                    onBackPressed()
                    return true
                }
                return false
            }

            override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
                return false
            }

            override fun onKeyMultiple(keyCode: Int, count: Int, event: KeyEvent?): Boolean {
                return false
            }
        }

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            return if (_lockTouch) {
                true
            } else {
                super.onInterceptTouchEvent(ev)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (_lockTouch) {
                return true
            }

            val isTouchOutside = !isViewUnder(_contentView, event.x.toInt(), event.y.toInt())
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (isTouchOutside) {
                    if (_cancelable && _canceledOnTouchOutside) {
                        if (isDebug) {
                            Log.i(IDialog::class.java.simpleName, "touch outside try cancel ${this@FDialog}")
                        }
                        cancel()
                        return true
                    }
                }
            }

            if (_lockTouch) {
                return true
            }

            if (this@FDialog.onTouchEvent(event, isTouchOutside)) {
                return true
            }

            super.onTouchEvent(event)
            return true
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "dialog onAttachedToWindow ${this@FDialog}")
            }
            checkFocus(true)
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "dialog onDetachedFromWindow ${this@FDialog}")
            }
            checkFocus(false)
        }

        override fun onViewAdded(child: View) {
            super.onViewAdded(child)
            if (child !== backgroundView && child !== containerView) {
                error("Can not add $child to dialog view.")
            }
        }

        override fun onViewRemoved(child: View) {
            super.onViewRemoved(child)
            if (child === backgroundView || child === containerView) {
                error("Can not remove child from dialog view.")
            }
        }
    }

    private inner class InternalContainerView(context: Context) : LinearLayout(context) {
        override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
            val finalLeft = if (left < 0) paddingLeft else left
            val finalTop = if (top < 0) paddingTop else top
            val finalRight = if (right < 0) paddingRight else right
            val finalBottom = if (bottom < 0) paddingBottom else bottom

            if (finalLeft != paddingLeft
                || finalTop != paddingTop
                || finalRight != paddingRight
                || finalBottom != paddingBottom
            ) {
                super.setPadding(finalLeft, finalTop, finalRight, finalBottom)
            }
        }

        override fun onViewAdded(child: View) {
            super.onViewAdded(child)
            if (child !== _contentView) {
                error("Can not add view to container.")
            }
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "onContentViewAdded:${child} ${this@FDialog}")
            }
        }

        override fun onViewRemoved(child: View) {
            super.onViewRemoved(child)
            if (child === _contentView) {
                // 外部直接移除内容view的话，关闭窗口
                dismiss()
            }
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "onContentViewRemoved:${child} ${this@FDialog}")
            }
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            super.onLayout(changed, l, t, r, b)
            startShowAnimator()
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "container onAttachedToWindow state:$_state ${this@FDialog}")
            }
            if (_state.isShowPart) {
                _showAnimatorFlag = true
                startShowAnimator()
            }
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            if (isDebug) {
                Log.i(IDialog::class.java.simpleName, "container onDetachedFromWindow state:$_state ${this@FDialog}")
            }
        }

        private fun startShowAnimator() {
            if (_showAnimatorFlag) {
                val width = containerView.width
                val height = containerView.height
                if (width > 0 && height > 0) {
                    if (isDebug) {
                        Log.i(IDialog::class.java.simpleName, "startShowAnimator width:${width} height:${height} ${this@FDialog}")
                    }
                    _showAnimatorFlag = false
                    _animatorHandler.setShowAnimator(createAnimator(true))
                    _animatorHandler.startShowAnimator()
                }
            }
        }
    }

    private val _activityLifecycleCallbacks by lazy {
        InternalActivityLifecycleCallbacks()
    }

    private inner class InternalActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
        fun register(register: Boolean) {
            val application = context.applicationContext as Application
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
            if (activity === _activity) {
                if (isDebug) {
                    Log.e(IDialog::class.java.simpleName, "onActivityDestroyed ${this@FDialog}")
                }
                FDialogHolder.remove(activity)
                dismiss()
            }
        }
    }

    init {
        gravity = Gravity.CENTER
        isBackgroundDim = true

        (activity.resources.displayMetrics.widthPixels * 0.1f).toInt().let { padding ->
            setPadding(padding, 0, padding, 0)
        }
    }

    companion object {
        /**
         * 返回Activity的所有窗口
         */
        @JvmStatic
        fun getAll(activity: Activity?): List<FDialog>? {
            if (activity == null) return null
            return FDialogHolder.get(activity)
        }

        /**
         * 关闭指定Activity的所有窗口
         */
        @JvmStatic
        fun dismissAll(activity: Activity?) {
            if (activity == null) return
            if (activity.isFinishing) return

            val list = getAll(activity) ?: return
            for (item in list) {
                item.dismiss()
            }
        }
    }
}

private enum class State {
    TryShow,
    Shown,

    TryDismiss,
    Dismissed;

    val isShowPart: Boolean
        get() = this == Shown || this == TryShow

    val isDismissPart: Boolean
        get() = this == Dismissed || this == TryDismiss
}

private fun isViewUnder(view: View?, x: Int, y: Int): Boolean {
    return if (view == null) {
        false
    } else {
        x >= view.left && x < view.right && y >= view.top && y < view.bottom
    }
}

private fun getAnimatorDuration(animator: Animator): Long {
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

private object FDialogHolder {
    private val dialogHolder: MutableMap<Activity, MutableList<FDialog>> = hashMapOf()

    fun addDialog(dialog: FDialog) {
        val activity = dialog.ownerActivity
        val holder = dialogHolder[activity] ?: mutableListOf<FDialog>().also {
            dialogHolder[activity] = it
        }

        holder.lastOrNull()?.notifyCover()
        holder.add(dialog)
    }

    fun removeDialog(dialog: FDialog) {
        val activity = dialog.ownerActivity
        val holder = dialogHolder[activity] ?: return

        val remove = holder.remove(dialog)
        if (remove) {
            holder.lastOrNull()?.notifyCoverRemove()
        }

        if (holder.isEmpty()) {
            dialogHolder.remove(activity)
        }
    }

    fun get(activity: Activity): List<FDialog>? {
        return dialogHolder[activity]?.toList()
    }

    fun getLast(activity: Activity): FDialog? {
        return dialogHolder[activity]?.lastOrNull()
    }

    fun remove(activity: Activity) {
        dialogHolder.remove(activity)
    }
}