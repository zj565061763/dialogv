package com.sd.demo.dialog;

import android.app.Activity;
import android.view.View;

import com.sd.demo.dialog.databinding.DialogPositionBinding;
import com.sd.lib.dialog.ITargetDialog;
import com.sd.lib.dialog.impl.FDialog;

public class PositionDialog extends FDialog implements View.OnClickListener {
    private final View mTargetView;
    private final DialogPositionBinding _binding;

    public PositionDialog(Activity activity, View targetView) {
        super(activity);
        mTargetView = targetView;

        setPadding(0, 0, 0, 0);
        setContentView(R.layout.dialog_position);
        _binding = DialogPositionBinding.bind(getContentView());

        _binding.btnLeftTop.setOnClickListener(this);
        _binding.btnLeftCenter.setOnClickListener(this);
        _binding.btnLeftBottom.setOnClickListener(this);

        _binding.btnRightTop.setOnClickListener(this);
        _binding.btnRightCenter.setOnClickListener(this);
        _binding.btnRightBottom.setOnClickListener(this);

        _binding.btnTopLeft.setOnClickListener(this);
        _binding.btnTopCenter.setOnClickListener(this);
        _binding.btnTopRight.setOnClickListener(this);

        _binding.btnBottomLeft.setOnClickListener(this);
        _binding.btnBottomCenter.setOnClickListener(this);
        _binding.btnBottomRight.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        final PopDialog popDialog = new PopDialog((Activity) getContext());

        switch (v.getId()) {
            case R.id.btn_left_top:
                popDialog.target().show(mTargetView, ITargetDialog.Position.LeftOutsideTop);
                break;
            case R.id.btn_left_center:
                popDialog.target().show(mTargetView, ITargetDialog.Position.LeftOutsideCenter);
                break;
            case R.id.btn_left_bottom:
                popDialog.target().show(mTargetView, ITargetDialog.Position.LeftOutsideBottom);
                break;

            case R.id.btn_right_top:
                popDialog.target().show(mTargetView, ITargetDialog.Position.RightOutsideTop);
                break;
            case R.id.btn_right_center:
                popDialog.target().show(mTargetView, ITargetDialog.Position.RightOutsideCenter);
                break;
            case R.id.btn_right_bottom:
                popDialog.target().show(mTargetView, ITargetDialog.Position.RightOutsideBottom);
                break;

            case R.id.btn_top_left:
                popDialog.target().show(mTargetView, ITargetDialog.Position.TopOutsideLeft);
                break;
            case R.id.btn_top_center:
                popDialog.target().show(mTargetView, ITargetDialog.Position.TopOutsideCenter);
                break;
            case R.id.btn_top_right:
                popDialog.target().show(mTargetView, ITargetDialog.Position.TopOutsideRight);
                break;

            case R.id.btn_bottom_left:
                popDialog.target().show(mTargetView, ITargetDialog.Position.BottomOutsideLeft);
                break;
            case R.id.btn_bottom_center:
                popDialog.target().show(mTargetView, ITargetDialog.Position.BottomOutsideCenter);
                break;
            case R.id.btn_bottom_right:
                popDialog.target().show(mTargetView, ITargetDialog.Position.BottomOutsideRight);
                break;
        }
    }
}
