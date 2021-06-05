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
        val popDialog = PopDialog((context as Activity))
        when (v) {
            _binding.btnLeftTop -> {
                popDialog.target().show(_targetView, ITargetDialog.Position.LeftOutsideTop)
            }
            _binding.btnLeftCenter -> {
                popDialog.target().show(_targetView, ITargetDialog.Position.LeftOutsideCenter)
            }
            _binding.btnLeftBottom -> {
                popDialog.target().show(_targetView, ITargetDialog.Position.LeftOutsideBottom)
            }
            _binding.btnRightTop -> {
                popDialog.target().show(_targetView, ITargetDialog.Position.RightOutsideTop)
            }
            _binding.btnRightCenter -> {
                popDialog.target().show(_targetView, ITargetDialog.Position.RightOutsideCenter)
            }
            _binding.btnRightBottom -> {
                popDialog.target().show(_targetView, ITargetDialog.Position.RightOutsideBottom)
            }
            _binding.btnTopLeft -> {
                popDialog.target().show(_targetView, ITargetDialog.Position.TopOutsideLeft)
            }
            _binding.btnTopCenter -> {
                popDialog.target().show(_targetView, ITargetDialog.Position.TopOutsideCenter)
            }
            _binding.btnTopRight -> {
                popDialog.target().show(_targetView, ITargetDialog.Position.TopOutsideRight)
            }
            _binding.btnBottomLeft -> {
                popDialog.target().show(_targetView, ITargetDialog.Position.BottomOutsideLeft)
            }
            _binding.btnBottomCenter -> {
                popDialog.target().show(_targetView, ITargetDialog.Position.BottomOutsideCenter)
            }
            _binding.btnBottomRight -> {
                popDialog.target().show(_targetView, ITargetDialog.Position.BottomOutsideRight)
            }
        }
    }
}