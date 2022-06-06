package com.sd.demo.dialog

import android.app.Activity
import android.view.View
import com.sd.demo.dialog.databinding.DialogPositionBinding
import com.sd.lib.dialog.ITargetDialog
import com.sd.lib.dialog.impl.FDialog

class PositionDialog : FDialog, View.OnClickListener {
    private val _binding: DialogPositionBinding
    private val _targetView: View

    constructor(activity: Activity, targetView: View) : super(activity) {
        _targetView = targetView
        setPadding(0, 0, 0, 0)
        setContentView(R.layout.dialog_position)
        _binding = DialogPositionBinding.bind(contentView!!)
        _binding.btnLeftTop.setOnClickListener(this)
        _binding.btnLeftCenter.setOnClickListener(this)
        _binding.btnLeftBottom.setOnClickListener(this)
        _binding.btnRightTop.setOnClickListener(this)
        _binding.btnRightCenter.setOnClickListener(this)
        _binding.btnRightBottom.setOnClickListener(this)
        _binding.btnTopLeft.setOnClickListener(this)
        _binding.btnTopCenter.setOnClickListener(this)
        _binding.btnTopRight.setOnClickListener(this)
        _binding.btnBottomLeft.setOnClickListener(this)
        _binding.btnBottomCenter.setOnClickListener(this)
        _binding.btnBottomRight.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        dismiss()

        val popDialog = PopDialog((context as Activity)).apply {
            // 半透明背景（默认true）
            isBackgroundDim = true

            // 偏移半透明背景，仅当半透明开启之后才有效（默认true）
            target().setTranslateBackground(true)

            // 设置目标
            target().setTarget(_targetView)
        }

        when (v) {
            _binding.btnLeftTop -> {
                popDialog.target()
                    .setPosition(ITargetDialog.Position.LeftOutsideTop)
                    .show()
            }
            _binding.btnLeftCenter -> {
                popDialog.target()
                    .setPosition(ITargetDialog.Position.LeftOutsideCenter)
                    .show()
            }
            _binding.btnLeftBottom -> {
                popDialog.target()
                    .setPosition(ITargetDialog.Position.LeftOutsideBottom)
                    .show()
            }
            _binding.btnRightTop -> {
                popDialog.target()
                    .setPosition(ITargetDialog.Position.RightOutsideTop)
                    .show()
            }
            _binding.btnRightCenter -> {
                popDialog.target()
                    .setPosition(ITargetDialog.Position.RightOutsideCenter)
                    .show()
            }
            _binding.btnRightBottom -> {
                popDialog.target()
                    .setPosition(ITargetDialog.Position.RightOutsideBottom)
                    .show()
            }
            _binding.btnTopLeft -> {
                popDialog.target()
                    .setPosition(ITargetDialog.Position.TopOutsideLeft)
                    .show()
            }
            _binding.btnTopCenter -> {
                popDialog.target()
                    .setPosition(ITargetDialog.Position.TopOutsideCenter)
                    .show()
            }
            _binding.btnTopRight -> {
                popDialog.target()
                    .setPosition(ITargetDialog.Position.TopOutsideRight)
                    .show()
            }
            _binding.btnBottomLeft -> {
                popDialog.target()
                    .setPosition(ITargetDialog.Position.BottomOutsideLeft)
                    .show()
            }
            _binding.btnBottomCenter -> {
                popDialog.target()
                    .setPosition(ITargetDialog.Position.BottomOutsideCenter)
                    .show()
            }
            _binding.btnBottomRight -> {
                popDialog.target()
                    .setPosition(ITargetDialog.Position.BottomOutsideRight)
                    .show()
            }
        }
    }
}