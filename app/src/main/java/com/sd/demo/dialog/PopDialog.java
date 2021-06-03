package com.sd.demo.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.sd.lib.dialog.impl.FDialog;

public class PopDialog extends FDialog {
    public PopDialog(Activity activity) {
        super(activity);
        setDebug(true);
        setPadding(0, 0, 0, 0);
        setBackgroundDim(false);
        setContentView(R.layout.dialog_pop);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
                /**
                 * 关闭窗口
                 */
                dismiss();
            }
        });
    }
}
