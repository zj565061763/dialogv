package com.sd.demo.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.sd.demo.dialog.databinding.ActivityMainBinding;
import com.sd.lib.dialog.IDialog;
import com.sd.lib.dialog.animator.SlideTopBottomParentCreator;
import com.sd.lib.dialog.impl.FDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding _binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_binding.getRoot());
    }

    private void showSimpleDemo() {
        final FDialog dialog = new FDialog(this);

        /**
         * 设置填充
         */
        dialog.setPadding(0, 0, 0, 0);

        /**
         * 设置调试模式，内部会输出日志，日志tag：Dialoger
         */
        dialog.setDebug(true);
        /**
         * 设置窗口要显示的内容
         */
        dialog.setContentView(new Button(this));

        /**
         * 设置窗口关闭监听
         */
        dialog.setOnDismissListener(new IDialog.OnDismissListener() {
            @Override
            public void onDismiss(IDialog dialog) {
                Log.i(TAG, "onDismiss:" + dialog);
            }
        });

        /**
         * 设置窗口显示监听
         */
        dialog.setOnShowListener(new IDialog.OnShowListener() {
            @Override
            public void onShow(IDialog dialog) {
                Log.i(TAG, "onShow:" + dialog);
            }
        });

        /**
         * 设置窗口内容view动画创建对象，此处设置为透明度变化，可以实现AnimatorCreator接口来实现自定义动画
         *
         * 默认规则:
         * Gravity.CENTER:                      AlphaCreator 透明度
         *
         * Gravity.LEFT:                        SlideRightLeftParentCreator 向右滑入，向左滑出
         *
         * Gravity.TOP:                         SlideBottomTopParentCreator 向下滑入，向上滑出
         *
         * Gravity.RIGHT:                       SlideLeftRightParentCreator 向左滑入，向右滑出
         *
         * Gravity.BOTTOM:                      SlideTopBottomParentCreator 向上滑入，向下滑出
         *
         */
        dialog.setAnimatorCreator(new SlideTopBottomParentCreator());

        /**
         * 设置动画时长
         */
        dialog.setAnimatorDuration(1000);

        /**
         * 显示窗口
         */
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        if (v == _binding.btnSimpleDemo) {
            showSimpleDemo();
        } else if (v == _binding.btnTarget) {
            new PositionDialog(this, v).show();
        }
    }
}