package com.sd.lib.dialog.impl

import android.util.Log
import android.view.View
import com.sd.lib.dialog.IDialog
import com.sd.lib.dialog.ITargetDialog
import com.sd.lib.dialog.ITargetDialog.Position
import com.sd.lib.dialog.animator.AnimatorCreator
import com.sd.lib.dialog.animator.PivotPercentCreator
import com.sd.lib.dialog.animator.ScaleXYCreator
import com.sd.lib.vtrack.tracker.FViewTracker
import com.sd.lib.vtrack.tracker.ViewTracker
import com.sd.lib.vtrack.updater.ViewUpdater
import com.sd.lib.vtrack.updater.impl.OnGlobalLayoutChangeUpdater

internal class SimpleTargetDialog(
    dialog: IDialog,
) : ITargetDialog {

    private val _dialog = dialog
    private var _position: Position = Position.BottomOutside
    private var _marginX = 0
    private var _marginY = 0
    private var _translateBackground = true

    private val _dialogBackup by lazy { DialogBackup() }
    private var _modifyAnimatorCreator: AnimatorCreator? = null

    override fun setMarginX(margin: Int): ITargetDialog {
        _marginX = margin
        return this
    }

    override fun setMarginY(margin: Int): ITargetDialog {
        _marginY = margin
        return this
    }

    override fun setTranslateBackground(translate: Boolean): ITargetDialog {
        _translateBackground = translate
        return this
    }

    override fun setTarget(target: View?): ITargetDialog {
        _viewTracker.target = target
        update()
        return this
    }

    override fun setTargetLocationInfo(locationInfo: ViewTracker.LocationInfo?): ITargetDialog {
        _viewTracker.targetLocationInfo = locationInfo
        update()
        return this
    }

    override fun setPosition(position: Position): ITargetDialog {
        _position = position
        return this
    }

    override fun update() {
        _viewUpdater.notifyUpdatable()
    }

    override fun show() {
        _dialog.contentView.let {
            _viewUpdater.view = it
            _viewTracker.source = it
        }
        _dialog.show()
    }

    private val _viewUpdater: ViewUpdater by lazy {
        object : OnGlobalLayoutChangeUpdater() {
            override fun onStateChanged(started: Boolean) {
                super.onStateChanged(started)
                Log.i(SimpleTargetDialog::class.java.simpleName, "ViewUpdater onStateChanged $started")
            }
        }.apply {
            this.setUpdatable {
                _viewTracker.update()
            }
        }
    }

    private val _viewTracker: ViewTracker by lazy {
        FViewTracker().apply {
            this.callback = object : ViewTracker.Callback() {
                override fun onUpdate(x: Int?, y: Int?, source: ViewTracker.SourceLocationInfo, target: ViewTracker.LocationInfo) {
                    if (!_viewUpdater.isStarted) return
                    if (source !is ViewTracker.ViewLocationInfo) return

                    val sourceView = source.view!!
                    val xInt = x ?: sourceView.left
                    val yInt = y ?: sourceView.top

                    var finalX = xInt + _marginX
                    var finalY = yInt + _marginY
                    var direction: Direction? = null

                    when (_position) {
                        Position.LeftOutside,
                        Position.LeftOutsideTop,
                        Position.LeftOutsideCenter,
                        Position.LeftOutsideBottom,
                        -> {
                            finalX -= sourceView.width
                            direction = Direction.Left
                        }

                        Position.RightOutside,
                        Position.RightOutsideTop,
                        Position.RightOutsideCenter,
                        Position.RightOutsideBottom,
                        -> {
                            finalX += sourceView.width
                            direction = Direction.Right
                        }

                        Position.TopOutside,
                        Position.TopOutsideLeft,
                        Position.TopOutsideCenter,
                        Position.TopOutsideRight,
                        -> {
                            finalY -= sourceView.height
                            direction = Direction.Top
                        }

                        Position.BottomOutside,
                        Position.BottomOutsideLeft,
                        Position.BottomOutsideCenter,
                        Position.BottomOutsideRight,
                        -> {
                            finalY += sourceView.height
                            direction = Direction.Bottom
                        }
                    }

                    val sourceParent = sourceView.parent as View
                    val left = finalX
                    val top = finalY
                    val right = sourceParent.width - finalX - sourceView.width
                    val bottom = sourceParent.height - finalY - sourceView.height

                    Log.i(SimpleTargetDialog::class.java.simpleName, "${left},${top},${right},${bottom}")

                    _dialog.setPadding(left, top, right, bottom)
                    sourceView.offsetLeftAndRight(finalX - sourceView.left)
                    sourceView.offsetTopAndBottom(finalY - sourceView.top)

                    checkTranslateBackground(direction, sourceView)
                }
            }
        }
    }

    private fun checkTranslateBackground(direction: Direction?, source: View) {
        if (direction == null) return
        if (!_translateBackground) return
        if (_dialog !is FDialog) return
        if (!_dialog.isBackgroundDim) return

        val backgroundView = _dialog.backgroundView
        when (direction) {
            Direction.Left -> {
                backgroundView.layout(
                    0, 0,
                    source.right, backgroundView.measuredHeight
                )
            }
            Direction.Top -> {
                backgroundView.layout(
                    0, 0,
                    backgroundView.measuredWidth, source.bottom
                )
            }
            Direction.Right -> {
                backgroundView.layout(
                    source.left, 0,
                    backgroundView.measuredWidth, backgroundView.measuredHeight
                )
            }
            Direction.Bottom -> {
                backgroundView.layout(
                    0, source.top,
                    backgroundView.measuredWidth, backgroundView.measuredHeight
                )
            }
        }
    }

    fun onStart() {
        if (_viewUpdater.view == null) return
        if (_viewTracker.sourceLocationInfo == null) return
        if (_viewTracker.targetLocationInfo == null) return

        when (_position) {
            Position.LeftOutside -> {
                _viewTracker.position = ViewTracker.Position.Left
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 1.0f, 0.5f))
            }
            Position.LeftOutsideTop -> {
                _viewTracker.position = ViewTracker.Position.TopLeft
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 1.0f, 0.0f))
            }
            Position.LeftOutsideCenter -> {
                _viewTracker.position = ViewTracker.Position.LeftCenter
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 1.0f, 0.5f))
            }
            Position.LeftOutsideBottom -> {
                _viewTracker.position = ViewTracker.Position.BottomLeft
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 1.0f, 1.0f))
            }
            Position.TopOutside -> {
                _viewTracker.position = ViewTracker.Position.Top
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.5f, 1.0f))
            }
            Position.TopOutsideLeft -> {
                _viewTracker.position = ViewTracker.Position.TopLeft
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.0f, 1.0f))
            }
            Position.TopOutsideCenter -> {
                _viewTracker.position = ViewTracker.Position.TopCenter
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.5f, 1.0f))
            }
            Position.TopOutsideRight -> {
                _viewTracker.position = ViewTracker.Position.TopRight
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 1.0f, 1.0f))
            }
            Position.RightOutside -> {
                _viewTracker.position = ViewTracker.Position.Right
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.0f, 0.5f))
            }
            Position.RightOutsideTop -> {
                _viewTracker.position = ViewTracker.Position.TopRight
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.0f, 0.0f))
            }
            Position.RightOutsideCenter -> {
                _viewTracker.position = ViewTracker.Position.RightCenter
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.0f, 0.5f))
            }
            Position.RightOutsideBottom -> {
                _viewTracker.position = ViewTracker.Position.BottomRight
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.0f, 1.0f))
            }
            Position.BottomOutside -> {
                _viewTracker.position = ViewTracker.Position.Bottom
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.5f, 0.0f))
            }
            Position.BottomOutsideLeft -> {
                _viewTracker.position = ViewTracker.Position.BottomLeft
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.0f, 0.0f))
            }
            Position.BottomOutsideCenter -> {
                _viewTracker.position = ViewTracker.Position.BottomCenter
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.5f, 0.0f))
            }
            Position.BottomOutsideRight -> {
                _viewTracker.position = ViewTracker.Position.BottomRight
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 1.0f, 0.0f))
            }
        }

        _dialogBackup.backup(_dialog)
        _viewUpdater.start()
    }

    fun onStop() {
        _viewUpdater.stop()
        _viewUpdater.view = null
        _viewTracker.sourceLocationInfo = null

        restoreAnimator()
        _dialogBackup.restore(_dialog)
    }

    private fun setDefaultAnimator(creator: AnimatorCreator) {
        if (_dialog.animatorCreator == null) {
            _dialog.animatorCreator = creator
            _modifyAnimatorCreator = creator
        }
    }

    private fun restoreAnimator() {
        if (_modifyAnimatorCreator != null && _modifyAnimatorCreator === _dialog.animatorCreator) {
            _dialog.animatorCreator = null
        }
    }
}

private class DialogBackup {
    private var _paddingLeft = 0
    private var _paddingTop = 0
    private var _paddingRight = 0
    private var _paddingBottom = 0
    private var _gravity = 0

    private var _hasBackup = false

    fun backup(dialog: IDialog) {
        if (!_hasBackup) {
            _paddingLeft = dialog.paddingLeft
            _paddingTop = dialog.paddingTop
            _paddingRight = dialog.paddingRight
            _paddingBottom = dialog.paddingBottom
            _gravity = dialog.gravity

            _hasBackup = true
            Log.i(SimpleTargetDialog::class.java.simpleName, "DialogBackup backup ${_paddingLeft},${_paddingTop},${_paddingRight},${_paddingBottom}")
        }
    }

    fun restore(dialog: IDialog) {
        if (_hasBackup) {
            dialog.setPadding(_paddingLeft, _paddingTop, _paddingRight, _paddingBottom)
            dialog.gravity = _gravity

            _hasBackup = false
            Log.i(SimpleTargetDialog::class.java.simpleName, "DialogBackup restore")
        }
    }
}

private enum class Direction {
    Left,
    Top,
    Right,
    Bottom
}