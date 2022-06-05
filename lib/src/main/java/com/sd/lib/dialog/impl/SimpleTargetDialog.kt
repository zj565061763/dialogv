package com.sd.lib.dialog.impl

import android.util.Log
import android.view.View
import com.sd.lib.dialog.IDialog
import com.sd.lib.dialog.ITargetDialog
import com.sd.lib.dialog.animator.AnimatorCreator
import com.sd.lib.dialog.animator.PivotPercentCreator
import com.sd.lib.dialog.animator.ScaleXYCreator
import com.sd.lib.vtrack.tracker.FViewTracker
import com.sd.lib.vtrack.tracker.ViewTracker
import com.sd.lib.vtrack.updater.ViewUpdater
import com.sd.lib.vtrack.updater.impl.OnGlobalLayoutChangeUpdater

internal class SimpleTargetDialog(private val _dialog: IDialog) : ITargetDialog {
    private var _position: ITargetDialog.Position? = null
    private var _marginX = 0
    private var _marginY = 0
    private var _translateBackground = false

    private val _dialogBackup by lazy { DialogBackup() }
    private var _modifyAnimatorCreator: AnimatorCreator? = null

    private var _targetLocationInfo: ViewTracker.LocationInfo? = null

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
        if (target == null) {
            if (_viewTracker.target != null) {
                _dialog.dismiss()
            }
        }
        _viewTracker.target = target
        return this
    }

    override fun setTargetLocationInfo(locationInfo: ViewTracker.LocationInfo?): ITargetDialog {
        if (locationInfo == null) {
            if (_targetLocationInfo != null) {
                _dialog.dismiss()
            }
        }
        _viewTracker.setTargetLocationInfo(locationInfo)
        return this
    }

    override fun show(position: ITargetDialog.Position) {
        _position = position
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
                Log.i(SimpleTargetDialog::class.java.simpleName, "ViewUpdater onStateChanged ${started}")
            }
        }.apply {
            this.setUpdatable {
                _viewTracker.update()
            }
        }
    }

    private val _viewTracker: ViewTracker by lazy {
        FViewTracker().apply {
            this.setCallback(object : ViewTracker.Callback() {
                override fun onUpdate(x: Int, y: Int, source: View, target: View) {
                    var finalX = x + _marginX
                    var finalY = y + _marginY
                    var direction: Direction? = null

                    when (_position) {
                        ITargetDialog.Position.LeftOutside,
                        ITargetDialog.Position.LeftOutsideTop,
                        ITargetDialog.Position.LeftOutsideCenter,
                        ITargetDialog.Position.LeftOutsideBottom,
                        -> {
                            finalX -= source.width
                            direction = Direction.Left
                        }

                        ITargetDialog.Position.RightOutside,
                        ITargetDialog.Position.RightOutsideTop,
                        ITargetDialog.Position.RightOutsideCenter,
                        ITargetDialog.Position.RightOutsideBottom,
                        -> {
                            finalX += source.width
                            direction = Direction.Right
                        }

                        ITargetDialog.Position.TopOutside,
                        ITargetDialog.Position.TopOutsideLeft,
                        ITargetDialog.Position.TopOutsideCenter,
                        ITargetDialog.Position.TopOutsideRight,
                        -> {
                            finalY -= source.height
                            direction = Direction.Top
                        }

                        ITargetDialog.Position.BottomOutside,
                        ITargetDialog.Position.BottomOutsideLeft,
                        ITargetDialog.Position.BottomOutsideCenter,
                        ITargetDialog.Position.BottomOutsideRight,
                        -> {
                            finalY += source.height
                            direction = Direction.Bottom
                        }
                        else -> {}
                    }

                    val sourceParent = source.parent as View
                    val left = finalX
                    val top = finalY
                    val right = sourceParent.width - finalX - source.width
                    val bottom = sourceParent.height - finalY - source.height

                    Log.i(SimpleTargetDialog::class.java.simpleName, "${left},${top},${right},${bottom}")

                    _dialog.setPadding(left, top, right, bottom)
                    source.offsetLeftAndRight(finalX - source.left)
                    source.offsetTopAndBottom(finalY - source.top)

                    checkTranslateBackground(direction, source)
                }
            })
        }
    }

    private fun checkTranslateBackground(direction: Direction?, source: View) {
        if (direction == null) return
        if (!_translateBackground) return
        if (_dialog !is FDialog) return

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
        val position = _position ?: return
        if (_viewUpdater.view == null) return
        if (_viewTracker.source == null || _viewTracker.target == null) return

        when (position) {
            ITargetDialog.Position.LeftOutside -> {
                _viewTracker.setPosition(ViewTracker.Position.Left)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 1.0f, 0.5f))
            }
            ITargetDialog.Position.LeftOutsideTop -> {
                _viewTracker.setPosition(ViewTracker.Position.TopLeft)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 1.0f, 0.0f))
            }
            ITargetDialog.Position.LeftOutsideCenter -> {
                _viewTracker.setPosition(ViewTracker.Position.LeftCenter)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 1.0f, 0.5f))
            }
            ITargetDialog.Position.LeftOutsideBottom -> {
                _viewTracker.setPosition(ViewTracker.Position.BottomLeft)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 1.0f, 1.0f))
            }
            ITargetDialog.Position.TopOutside -> {
                _viewTracker.setPosition(ViewTracker.Position.Top)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.5f, 1.0f))
            }
            ITargetDialog.Position.TopOutsideLeft -> {
                _viewTracker.setPosition(ViewTracker.Position.TopLeft)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.0f, 1.0f))
            }
            ITargetDialog.Position.TopOutsideCenter -> {
                _viewTracker.setPosition(ViewTracker.Position.TopCenter)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.5f, 1.0f))
            }
            ITargetDialog.Position.TopOutsideRight -> {
                _viewTracker.setPosition(ViewTracker.Position.TopRight)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 1.0f, 1.0f))
            }
            ITargetDialog.Position.RightOutside -> {
                _viewTracker.setPosition(ViewTracker.Position.Right)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.0f, 0.5f))
            }
            ITargetDialog.Position.RightOutsideTop -> {
                _viewTracker.setPosition(ViewTracker.Position.TopRight)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.0f, 0.0f))
            }
            ITargetDialog.Position.RightOutsideCenter -> {
                _viewTracker.setPosition(ViewTracker.Position.RightCenter)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.0f, 0.5f))
            }
            ITargetDialog.Position.RightOutsideBottom -> {
                _viewTracker.setPosition(ViewTracker.Position.BottomRight)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.0f, 1.0f))
            }
            ITargetDialog.Position.BottomOutside -> {
                _viewTracker.setPosition(ViewTracker.Position.Bottom)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.5f, 0.0f))
            }
            ITargetDialog.Position.BottomOutsideLeft -> {
                _viewTracker.setPosition(ViewTracker.Position.BottomLeft)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.0f, 0.0f))
            }
            ITargetDialog.Position.BottomOutsideCenter -> {
                _viewTracker.setPosition(ViewTracker.Position.BottomCenter)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 0.5f, 0.0f))
            }
            ITargetDialog.Position.BottomOutsideRight -> {
                _viewTracker.setPosition(ViewTracker.Position.BottomRight)
                setDefaultAnimator(PivotPercentCreator(ScaleXYCreator(), 1.0f, 0.0f))
            }
        }

        _dialogBackup.backup(_dialog)
        _viewUpdater.start()
    }

    fun onStop() {
        _viewUpdater.stop()
        _viewUpdater.view = null

        _viewTracker.source = null
        _viewTracker.target = null

        _position = null
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