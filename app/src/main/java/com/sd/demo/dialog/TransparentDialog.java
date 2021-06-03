package com.sd.demo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

public class TransparentDialog extends Dialog {
    public TransparentDialog(@NonNull Context context) {
        super(context, R.style.transparent_dialog);
        getWindow().getAttributes().width = 1;
        getWindow().getAttributes().height = 1;
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        return false;
    }
}
