package com.sd.demo.dialog

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.dialog.databinding.ActivityMainBinding
import com.sd.lib.dialog.animator.SlideTopBottomParentCreator
import com.sd.lib.dialog.impl.FDialog

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = MainActivity::class.java.simpleName

    private lateinit var _binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)
    }

    private fun createSimpleDialog(): FDialog {
        val dialog: FDialog = object : FDialog(this@MainActivity) {
            override fun onContentViewChanged(oldView: View?, newView: View?) {
                super.onContentViewChanged(oldView, newView)
                Log.i(TAG, "onContentViewChanged oldView:${oldView} newView:${contentView}")
            }

            override fun onCreate() {
                super.onCreate()
                Log.i(TAG, "onCreate")
            }

            override fun onStart() {
                super.onStart()
                Log.i(TAG, "onStart")
            }

            override fun onStop() {
                super.onStop()
                Log.i(TAG, "onStop")
            }
        }
        /**
         * 设置填充
         */
        dialog.setPadding(0, 0, 0, 0)
        /**
         * 设置调试模式，内部会输出日志，日志tag：IDialog
         */
        dialog.isDebug = true
        /**
         * 设置窗口要显示的内容
         */
        dialog.setContentView(Button(this).apply {
            this.layoutParams = ViewGroup.LayoutParams(300, 100)
        })
        /**
         * 设置窗口关闭监听
         */
        dialog.setOnDismissListener {
            Log.i(TAG, "onDismiss")
        }
        /**
         * 设置窗口显示监听
         */
        dialog.setOnShowListener {
            Log.i(TAG, "onShow")
        }
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
        dialog.animatorCreator = SlideTopBottomParentCreator()
        /**
         * 设置动画时长
         */
        dialog.setAnimatorDuration(1000)
        return dialog
    }

    override fun onClick(v: View) {
        when (v) {
            _binding.btnSimple -> {
                createSimpleDialog().show()
            }
            _binding.btnSimpleTwo -> {
                createSimpleDialog().show()
                createSimpleDialog().show()
            }
            _binding.btnTarget -> {
                PositionDialog(this, v).show()
            }
        }
    }
}