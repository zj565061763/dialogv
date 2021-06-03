package com.sd.demo.dialog;

import android.app.Activity;
import android.view.Gravity;

import com.sd.lib.dialog.impl.FDialog;
import com.sd.lib.systemui.statusbar.FStatusBar;

public class SystemUIDialog extends FDialog implements FStatusBar.Config {
    public SystemUIDialog(Activity activity) {
        super(activity);
        setPadding(0, 0, 0, 0);
        setGravity(Gravity.TOP);
        setContentView(R.layout.dialog_system_ui);
    }

    @Override
    public FStatusBar.Brightness getStatusBarBrightness() {
        return FStatusBar.Brightness.dark;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FStatusBar.of(getOwnerActivity()).applyConfig(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FStatusBar.of(getOwnerActivity()).removeConfig(this);
    }
}
